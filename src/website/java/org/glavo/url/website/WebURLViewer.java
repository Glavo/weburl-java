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

package org.glavo.url.website;

import org.glavo.url.WebURL;
import org.glavo.url.WebURLParseException;
import org.glavo.url.WebURLParser;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

import java.net.URISyntaxException;

/// TeaVM entry point for the website's live URL viewer.
@NotNullByDefault
public final class WebURLViewer {
    /// Display text for a java.net.URI component that has no corresponding API.
    private static final String UNSUPPORTED = "(unsupported)";

    /// HTML ids for fields populated from the WebURL result.
    private static final String @Unmodifiable [] WEBURL_FIELD_IDS = {
            "weburl-href",
            "weburl-origin",
            "weburl-protocol",
            "weburl-username",
            "weburl-password",
            "weburl-host",
            "weburl-hostname",
            "weburl-port",
            "weburl-pathname",
            "weburl-search",
            "weburl-hash",
            "weburl-java-serialized-url",
            "weburl-java-display-string",
            "weburl-java-rfc2396-string",
            "weburl-java-scheme",
            "weburl-java-raw-authority",
            "weburl-java-decoded-authority",
            "weburl-java-raw-user-info",
            "weburl-java-decoded-user-info",
            "weburl-java-raw-username",
            "weburl-java-decoded-username",
            "weburl-java-raw-password",
            "weburl-java-decoded-password",
            "weburl-java-host",
            "weburl-java-port",
            "weburl-java-raw-port",
            "weburl-java-raw-path",
            "weburl-java-decoded-path",
            "weburl-java-raw-query",
            "weburl-java-decoded-query",
            "weburl-java-raw-fragment",
            "weburl-java-decoded-fragment",
    };

    /// Parser mode value for the default parser.
    private static final String MODE_DEFAULT = "default";

    /// Parser mode value for the strict parser.
    private static final String MODE_STRICT = "strict";

    /// Parser mode value for browser-input parsing.
    private static final String MODE_BROWSER_INPUT = "browser-input";

    /// Creates no instances.
    private WebURLViewer() {
    }

    /// Starts the live viewer after the WebAssembly module has loaded.
    ///
    /// @param args ignored command-line arguments supplied by the TeaVM runtime
    public static void main(String[] args) {
        addLiveUpdateListener("url-input", WebURLViewer::update);
        addLiveUpdateListener("base-input", WebURLViewer::update);
        addLiveUpdateListener("parser-mode", WebURLViewer::update);
        markReady();
        update();
    }

    /// Recomputes both the native browser URL view and the WebURL view.
    private static void update() {
        String input = readValue("url-input");
        String base = readValue("base-input");
        String mode = readValue("parser-mode");

        renderBrowserURL(input, base);
        renderJavaURIFields(input);
        renderWebURL(input, base, mode);
    }

    /// Parses input with WebURL and renders the result or error details.
    ///
    /// @param input the URL input text
    /// @param base the optional base URL text
    /// @param mode the selected parser mode
    private static void renderWebURL(String input, String base, String mode) {
        try {
            WebURL url = parse(input, base, mode);
            setState("weburl-panel", "ok");
            setText("weburl-status", "Parsed");
            setText("weburl-error", "");
            renderWebURLFields(url);
        } catch (WebURLParseException e) {
            clearFields(WEBURL_FIELD_IDS);
            setState("weburl-panel", "error");
            setText("weburl-status", "Rejected");
            setText("weburl-error", e.getErrorName() + " at index " + e.getIndex() + ": " + e.getReason());
        } catch (RuntimeException e) {
            clearFields(WEBURL_FIELD_IDS);
            setState("weburl-panel", "error");
            setText("weburl-status", "Failed");
            setText("weburl-error", e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage()));
        }
    }

    /// Parses the input according to the selected mode.
    ///
    /// @param input the URL input text
    /// @param base the optional base URL text
    /// @param mode the selected parser mode
    /// @return the parsed URL
    private static WebURL parse(String input, String base, String mode) {
        if (MODE_BROWSER_INPUT.equals(mode)) {
            return WebURL.parseBrowserInput(input);
        }

        WebURLParser parser = MODE_STRICT.equals(mode) ? WebURLParser.getStrict() : WebURLParser.getDefault();
        return base.isEmpty() ? parser.parse(input) : parser.parse(input, base);
    }

    /// Renders all WebURL component fields.
    ///
    /// @param url the parsed URL
    private static void renderWebURLFields(WebURL url) {
        setComparedValue("weburl-href", url.href());
        setComparedValue("weburl-origin", url.origin());
        setComparedValue("weburl-protocol", url.getWebProtocol());
        setComparedValue("weburl-username", url.getWebUsername());
        setComparedValue("weburl-password", url.getWebPassword());
        setComparedValue("weburl-host", url.getWebHost());
        setComparedValue("weburl-hostname", url.getWebHostname());
        setComparedValue("weburl-port", url.getWebPort());
        setComparedValue("weburl-pathname", url.getWebPathname());
        setComparedValue("weburl-search", url.getWebSearch());
        setComparedValue("weburl-hash", url.getWebHash());
        setJavaValue("weburl-java-serialized-url", display(url.toString()));
        setJavaValue("weburl-java-display-string", display(url.toDisplayString()));
        setJavaValue("weburl-java-rfc2396-string", display(url.toRFC2396String()));
        setJavaValue("weburl-java-scheme", display(url.getScheme()));
        setJavaValue("weburl-java-raw-authority", displayNullable(url.getRawAuthority()));
        setJavaValue("weburl-java-decoded-authority", displayNullable(url.getAuthority()));
        setJavaValue("weburl-java-raw-user-info", displayNullable(url.getRawUserInfo()));
        setJavaValue("weburl-java-decoded-user-info", displayNullable(url.getUserInfo()));
        setJavaValue("weburl-java-raw-username", displayNullable(url.getRawUsername()));
        setJavaValue("weburl-java-decoded-username", displayNullable(url.getUsername()));
        setJavaValue("weburl-java-raw-password", displayNullable(url.getRawPassword()));
        setJavaValue("weburl-java-decoded-password", displayNullable(url.getPassword()));
        setJavaValue("weburl-java-host", displayNullable(url.getHost()));
        setJavaValue("weburl-java-port", Integer.toString(url.getPort()));
        setJavaValue("weburl-java-raw-port", displayNullable(url.getRawPort()));
        setJavaValue("weburl-java-raw-path", display(url.getRawPath()));
        setJavaValue("weburl-java-decoded-path", display(url.getPath()));
        setJavaValue("weburl-java-raw-query", displayNullable(url.getRawQuery()));
        setJavaValue("weburl-java-decoded-query", displayNullable(url.getQuery()));
        setJavaValue("weburl-java-raw-fragment", displayNullable(url.getRawFragment()));
        setJavaValue("weburl-java-decoded-fragment", displayNullable(url.getFragment()));
    }

    /// Renders the java.net.URI comparison fields for the original input.
    ///
    /// @param input the original URL input text
    private static void renderJavaURIFields(String input) {
        try {
            JavaURI uri = new JavaURI(input);
            setJavaValue("uri-java-serialized-url", display(uri.toString()));
            setJavaValue("uri-java-display-string", UNSUPPORTED);
            setJavaValue("uri-java-rfc2396-string", display(uri.toASCIIString()));
            setJavaValue("uri-java-scheme", displayNullable(uri.getScheme()));
            setJavaValue("uri-java-raw-authority", displayNullable(uri.getRawAuthority()));
            setJavaValue("uri-java-decoded-authority", displayNullable(uri.getAuthority()));
            setJavaValue("uri-java-raw-user-info", displayNullable(uri.getRawUserInfo()));
            setJavaValue("uri-java-decoded-user-info", displayNullable(uri.getUserInfo()));
            setJavaValue("uri-java-raw-username", UNSUPPORTED);
            setJavaValue("uri-java-decoded-username", UNSUPPORTED);
            setJavaValue("uri-java-raw-password", UNSUPPORTED);
            setJavaValue("uri-java-decoded-password", UNSUPPORTED);
            setJavaValue("uri-java-host", displayNullable(uri.getHost()));
            setJavaValue("uri-java-port", Integer.toString(uri.getPort()));
            setJavaValue("uri-java-raw-port", UNSUPPORTED);
            setJavaValue("uri-java-raw-path", displayNullable(uri.getRawPath()));
            setJavaValue("uri-java-decoded-path", displayNullable(uri.getPath()));
            setJavaValue("uri-java-raw-query", displayNullable(uri.getRawQuery()));
            setJavaValue("uri-java-decoded-query", displayNullable(uri.getQuery()));
            setJavaValue("uri-java-raw-fragment", displayNullable(uri.getRawFragment()));
            setJavaValue("uri-java-decoded-fragment", displayNullable(uri.getFragment()));
        } catch (URISyntaxException e) {
            renderUnavailableURIFields(e);
        }
    }

    /// Renders java.net.URI fields when the original input cannot be represented as a Java URI.
    ///
    /// @param exception the conversion failure
    private static void renderUnavailableURIFields(URISyntaxException exception) {
        setJavaValue("uri-java-serialized-url", "conversion failed: " + exception.getReason());
        setJavaValue("uri-java-display-string", UNSUPPORTED);
        setJavaValue("uri-java-rfc2396-string", UNSUPPORTED);
        setJavaValue("uri-java-scheme", UNSUPPORTED);
        setJavaValue("uri-java-raw-authority", UNSUPPORTED);
        setJavaValue("uri-java-decoded-authority", UNSUPPORTED);
        setJavaValue("uri-java-raw-user-info", UNSUPPORTED);
        setJavaValue("uri-java-decoded-user-info", UNSUPPORTED);
        setJavaValue("uri-java-raw-username", UNSUPPORTED);
        setJavaValue("uri-java-decoded-username", UNSUPPORTED);
        setJavaValue("uri-java-raw-password", UNSUPPORTED);
        setJavaValue("uri-java-decoded-password", UNSUPPORTED);
        setJavaValue("uri-java-host", UNSUPPORTED);
        setJavaValue("uri-java-port", UNSUPPORTED);
        setJavaValue("uri-java-raw-port", UNSUPPORTED);
        setJavaValue("uri-java-raw-path", UNSUPPORTED);
        setJavaValue("uri-java-decoded-path", UNSUPPORTED);
        setJavaValue("uri-java-raw-query", UNSUPPORTED);
        setJavaValue("uri-java-decoded-query", UNSUPPORTED);
        setJavaValue("uri-java-raw-fragment", UNSUPPORTED);
        setJavaValue("uri-java-decoded-fragment", UNSUPPORTED);
    }

    /// Clears a list of result fields.
    ///
    /// @param fieldIds HTML ids to clear
    private static void clearFields(String @Unmodifiable [] fieldIds) {
        for (String fieldId : fieldIds) {
            clearValue(fieldId);
        }
    }

    /// Converts a non-null string to its display value.
    ///
    /// @param value the value to display
    /// @return the display text
    private static String display(String value) {
        return value.isEmpty() ? "(empty)" : value;
    }

    /// Converts a nullable string to its display value.
    ///
    /// @param value the value to display
    /// @return the display text
    private static String displayNullable(@Nullable String value) {
        return value == null ? "null" : display(value);
    }

    /// Reads an input or select value from the page.
    ///
    /// @param id the element id
    /// @return the element value, or the empty string when the element is missing
    @JSBody(params = {"id"}, script = "var e = document.getElementById(id); return e ? e.value : '';")
    private static native String readValue(String id);

    /// Sets an element's text content.
    ///
    /// @param id the element id
    /// @param value the text value
    @JSBody(params = {"id", "value"}, script = "window.WebURLViewer.setText(id, value);")
    private static native void setText(String id, String value);

    /// Sets a WebURL component value that should be compared against the browser result.
    ///
    /// @param id the element id
    /// @param value the raw component value
    @JSBody(params = {"id", "value"}, script = "window.WebURLViewer.setComparedValue(id, value);")
    private static native void setComparedValue(String id, String value);

    /// Sets a Java-style WebURL component display value.
    ///
    /// @param id the element id
    /// @param value the display value
    @JSBody(params = {"id", "value"}, script = "window.WebURLViewer.setJavaValue(id, value);")
    private static native void setJavaValue(String id, String value);

    /// Clears a result field and any attached value state.
    ///
    /// @param id the element id
    @JSBody(params = {"id"}, script = "window.WebURLViewer.clearValue(id);")
    private static native void clearValue(String id);

    /// Sets a panel state attribute used by CSS.
    ///
    /// @param id the element id
    /// @param state the state value
    @JSBody(params = {"id", "state"}, script = "window.WebURLViewer.setState(id, state);")
    private static native void setState(String id, String state);

    /// Renders the browser-native `URL` comparison view.
    ///
    /// @param input the URL input text
    /// @param base the optional base URL text
    @JSBody(params = {"input", "base"}, script = "window.WebURLViewer.renderBrowserURL(input, base);")
    private static native void renderBrowserURL(String input, String base);

    /// Registers live update listeners on a form control.
    ///
    /// @param id the element id
    /// @param listener the listener to call after the control changes
    @JSBody(params = {"id", "listener"}, script = ""
            + "var e = document.getElementById(id);"
            + "if (e) {"
            + "  e.addEventListener('input', function () { listener(); });"
            + "  e.addEventListener('change', function () { listener(); });"
            + "}")
    private static native void addLiveUpdateListener(String id, UpdateListener listener);

    /// Marks the viewer as ready.
    @JSBody(script = "window.WebURLViewer.setReady();")
    private static native void markReady();

    /// Java function type passed to JavaScript as an event callback.
    @JSFunctor
    @NotNullByDefault
    private interface UpdateListener extends JSObject {
        /// Handles an input change event.
        void handle();
    }
}
