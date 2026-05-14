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

/// TeaVM entry point for the website's live URL viewer.
@NotNullByDefault
public final class WebURLViewer {
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
            "weburl-raw-username",
            "weburl-decoded-username",
            "weburl-raw-password",
            "weburl-decoded-password",
            "weburl-raw-authority",
            "weburl-decoded-authority",
            "weburl-raw-path",
            "weburl-decoded-path",
            "weburl-raw-query",
            "weburl-decoded-query",
            "weburl-raw-fragment",
            "weburl-decoded-fragment",
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
        setText("weburl-href", display(url.href()));
        setText("weburl-origin", display(url.origin()));
        setText("weburl-protocol", display(url.getWebProtocol()));
        setText("weburl-username", display(url.getWebUsername()));
        setText("weburl-password", display(url.getWebPassword()));
        setText("weburl-host", display(url.getWebHost()));
        setText("weburl-hostname", display(url.getWebHostname()));
        setText("weburl-port", display(url.getWebPort()));
        setText("weburl-pathname", display(url.getWebPathname()));
        setText("weburl-search", display(url.getWebSearch()));
        setText("weburl-hash", display(url.getWebHash()));
        setText("weburl-raw-username", displayNullable(url.getRawUsername()));
        setText("weburl-decoded-username", displayNullable(url.getUsername()));
        setText("weburl-raw-password", displayNullable(url.getRawPassword()));
        setText("weburl-decoded-password", displayNullable(url.getPassword()));
        setText("weburl-raw-authority", displayNullable(url.getRawAuthority()));
        setText("weburl-decoded-authority", displayNullable(url.getAuthority()));
        setText("weburl-raw-path", display(url.getRawPath()));
        setText("weburl-decoded-path", display(url.getPath()));
        setText("weburl-raw-query", displayNullable(url.getRawQuery()));
        setText("weburl-decoded-query", displayNullable(url.getQuery()));
        setText("weburl-raw-fragment", displayNullable(url.getRawFragment()));
        setText("weburl-decoded-fragment", displayNullable(url.getFragment()));
    }

    /// Clears a list of result fields.
    ///
    /// @param fieldIds HTML ids to clear
    private static void clearFields(String @Unmodifiable [] fieldIds) {
        for (String fieldId : fieldIds) {
            setText(fieldId, "");
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
        return value == null ? "(absent)" : display(value);
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
