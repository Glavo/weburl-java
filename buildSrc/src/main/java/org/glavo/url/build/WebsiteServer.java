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

import io.undertow.Undertow;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.MimeMappings;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/// Undertow-backed HTTP server used by the website preview Gradle task.
@NotNullByDefault
public final class WebsiteServer {
    /// MIME mappings required by the static website preview.
    private static final MimeMappings MIME_MAPPINGS = MimeMappings.builder(true)
            .addMapping("html", "text/html; charset=utf-8")
            .addMapping("css", "text/css; charset=utf-8")
            .addMapping("js", "text/javascript; charset=utf-8")
            .addMapping("wasm", "application/wasm")
            .build();

    /// Creates no instances.
    private WebsiteServer() {
    }

    /// Serves the given directory on the loopback interface until the Gradle process is stopped.
    ///
    /// @param root the directory to serve
    /// @param port the TCP port to bind
    /// @throws IOException if the website output directory does not exist
    /// @throws InterruptedException if the preview thread is interrupted
    public static void serve(File root, int port) throws IOException, InterruptedException {
        Path rootPath = root.toPath().toAbsolutePath().normalize();
        if (!Files.isDirectory(rootPath)) {
            throw new IOException("Website output directory does not exist: " + rootPath);
        }

        ResourceHandler handler = new ResourceHandler(new PathResourceManager(rootPath, 1024))
                .setWelcomeFiles("index.html")
                .setMimeMappings(MIME_MAPPINGS)
                .setDirectoryListingEnabled(false);
        Undertow server = Undertow.builder()
                .addHttpListener(port, InetAddress.getLoopbackAddress().getHostAddress())
                .setHandler(handler)
                .build();

        AtomicBoolean stopped = new AtomicBoolean();
        Runnable stopServer = () -> {
            if (stopped.compareAndSet(false, true)) {
                server.stop();
            }
        };
        Thread shutdownHook = new Thread(stopServer, "website-preview-shutdown");
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(shutdownHook);
        try {
            server.start();
            System.out.println("Serving " + rootPath + " at http://127.0.0.1:" + port + "/");
            System.out.println("Press Ctrl+C to stop the preview server.");
            new CountDownLatch(1).await();
        } finally {
            try {
                runtime.removeShutdownHook(shutdownHook);
            } catch (IllegalStateException ignored) {
                // The JVM is already shutting down, so the hook is already being handled.
            }
            stopServer.run();
        }
    }
}
