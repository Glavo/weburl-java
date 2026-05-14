/*
 * Copyright 2026 Glavo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glavo.url.build;

import org.jetbrains.annotations.NotNullByDefault;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/// Minimal blocking HTTP server used by the website preview Gradle task.
///
/// This class intentionally depends only on `java.base` so the preview task works on JDKs where the
/// `jdk.httpserver` module is not available.
@NotNullByDefault
public final class WebsiteServer {
    /// Maximum request line length accepted by the preview server.
    private static final int MAX_REQUEST_LINE_LENGTH = 8192;

    /// Creates no instances.
    private WebsiteServer() {
    }

    /// Serves the given directory on the loopback interface until the Gradle process is stopped.
    ///
    /// @param root the directory to serve
    /// @param port the TCP port to bind
    /// @throws IOException if the server cannot bind or serve files
    public static void serve(File root, int port) throws IOException {
        Path rootPath = root.toPath().toAbsolutePath().normalize();
        if (!Files.isDirectory(rootPath)) {
            throw new IOException("Website output directory does not exist: " + rootPath);
        }

        try (ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
            System.out.println("Serving " + rootPath + " at http://127.0.0.1:" + port + "/");
            System.out.println("Press Ctrl+C to stop the preview server.");

            while (true) {
                Socket socket = server.accept();
                Thread thread = new Thread(() -> handleClient(rootPath, socket), "website-preview-client");
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    /// Handles a single HTTP client connection.
    ///
    /// @param rootPath the normalized website root path
    /// @param socket the accepted client socket
    private static void handleClient(Path rootPath, Socket socket) {
        try (socket) {
            socket.setSoTimeout(10_000);
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            String requestLine = readAsciiLine(input);
            if (requestLine.isEmpty()) {
                return;
            }
            skipHeaders(input);

            boolean head = requestLine.startsWith("HEAD ");
            if (!head && !requestLine.startsWith("GET ")) {
                sendText(output, 405, "Method Not Allowed", "Only GET and HEAD are supported.");
                return;
            }

            int targetStart = requestLine.indexOf(' ') + 1;
            int targetEnd = requestLine.indexOf(' ', targetStart);
            if (targetStart <= 0 || targetEnd <= targetStart) {
                sendText(output, 400, "Bad Request", "Malformed request line.");
                return;
            }

            Path file = resolveTarget(rootPath, requestLine.substring(targetStart, targetEnd));
            if (!file.startsWith(rootPath)) {
                sendText(output, 403, "Forbidden", "Path escapes the website root.");
                return;
            }
            if (Files.isDirectory(file)) {
                file = file.resolve("index.html");
            }
            if (!Files.isRegularFile(file)) {
                sendText(output, 404, "Not Found", "File not found.");
                return;
            }

            long length = Files.size(file);
            writeHeader(output, 200, "OK", contentType(file), length);
            if (!head) {
                try (InputStream fileInput = Files.newInputStream(file)) {
                    fileInput.transferTo(output);
                }
            }
        } catch (IOException e) {
            System.err.println("Website preview request failed: " + e.getMessage());
        }
    }

    /// Resolves an HTTP request target to a normalized file path.
    ///
    /// @param rootPath the normalized website root path
    /// @param target the raw HTTP request target
    /// @return the resolved normalized file path
    private static Path resolveTarget(Path rootPath, String target) {
        int queryIndex = target.indexOf('?');
        String path = queryIndex < 0 ? target : target.substring(0, queryIndex);
        if (path.isEmpty() || path.equals("/")) {
            path = "/index.html";
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return rootPath.resolve(percentDecode(path)).normalize();
    }

    /// Decodes percent-encoded UTF-8 bytes in a request path.
    ///
    /// @param input the request path text
    /// @return the decoded path text
    private static String percentDecode(String input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '%' && i + 2 < input.length()) {
                int hi = hex(input.charAt(i + 1));
                int lo = hex(input.charAt(i + 2));
                if (hi >= 0 && lo >= 0) {
                    output.write((hi << 4) | lo);
                    i += 2;
                    continue;
                }
            }

            String text = String.valueOf(c);
            output.writeBytes(text.getBytes(StandardCharsets.UTF_8));
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    /// Returns the hexadecimal value of an ASCII digit.
    ///
    /// @param c the character to inspect
    /// @return the hexadecimal value, or `-1` when invalid
    private static int hex(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        if ('A' <= c && c <= 'F') {
            return c - 'A' + 10;
        }
        if ('a' <= c && c <= 'f') {
            return c - 'a' + 10;
        }
        return -1;
    }

    /// Reads one ASCII request line.
    ///
    /// @param input the socket input stream
    /// @return the decoded line without CRLF
    /// @throws IOException if the line cannot be read
    private static String readAsciiLine(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while (output.size() < MAX_REQUEST_LINE_LENGTH) {
            int value = input.read();
            if (value < 0) {
                break;
            }
            if (value == '\n') {
                break;
            }
            if (value != '\r') {
                output.write(value);
            }
        }
        return output.toString(StandardCharsets.US_ASCII);
    }

    /// Skips HTTP request headers.
    ///
    /// @param input the socket input stream
    /// @throws IOException if headers cannot be read
    private static void skipHeaders(InputStream input) throws IOException {
        while (!readAsciiLine(input).isEmpty()) {
            // Continue until the empty line that terminates the header block.
        }
    }

    /// Sends a plain-text error response.
    ///
    /// @param output the socket output stream
    /// @param status the HTTP status code
    /// @param reason the HTTP reason phrase
    /// @param message the response body
    /// @throws IOException if the response cannot be written
    private static void sendText(OutputStream output, int status, String reason, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        writeHeader(output, status, reason, "text/plain; charset=utf-8", body.length);
        output.write(body);
    }

    /// Writes an HTTP response header.
    ///
    /// @param output the socket output stream
    /// @param status the HTTP status code
    /// @param reason the HTTP reason phrase
    /// @param contentType the response content type
    /// @param contentLength the response body byte length
    /// @throws IOException if the header cannot be written
    private static void writeHeader(
            OutputStream output,
            int status,
            String reason,
            String contentType,
            long contentLength
    ) throws IOException {
        String header = "HTTP/1.1 " + status + ' ' + reason + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + contentLength + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";
        output.write(header.getBytes(StandardCharsets.US_ASCII));
    }

    /// Returns the content type for a served file.
    ///
    /// @param file the file path
    /// @return the HTTP content type
    private static String contentType(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (name.endsWith(".js")) {
            return "text/javascript; charset=utf-8";
        }
        if (name.endsWith(".wasm")) {
            return "application/wasm";
        }
        if (name.endsWith(".json")) {
            return "application/json; charset=utf-8";
        }
        if (name.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream";
    }
}
