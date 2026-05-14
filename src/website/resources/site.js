(function () {
  const browserFieldIds = [
    "browser-href",
    "browser-origin",
    "browser-protocol",
    "browser-username",
    "browser-password",
    "browser-host",
    "browser-hostname",
    "browser-port",
    "browser-pathname",
    "browser-search",
    "browser-hash"
  ];
  const componentNames = [
    "href",
    "origin",
    "protocol",
    "username",
    "password",
    "host",
    "hostname",
    "port",
    "pathname",
    "search",
    "hash"
  ];
  const javaFieldNames = [
    "serialized-url",
    "display-string",
    "rfc2396-string",
    "scheme",
    "raw-authority",
    "decoded-authority",
    "raw-user-info",
    "decoded-user-info",
    "raw-username",
    "decoded-username",
    "raw-password",
    "decoded-password",
    "host",
    "port",
    "raw-port",
    "raw-path",
    "decoded-path",
    "raw-query",
    "decoded-query",
    "raw-fragment",
    "decoded-fragment"
  ];
  const browserValues = new Map();
  const weburlValues = new Map();
  const weburlJavaValues = new Map();
  const uriJavaValues = new Map();

  function byId(id) {
    return document.getElementById(id);
  }

  function display(value) {
    return value === "" ? "(empty)" : value;
  }

  function setValueState(id, state) {
    const element = byId(id);
    if (element) {
      if (state) {
        element.dataset.valueState = state;
      } else {
        delete element.dataset.valueState;
      }
    }
  }

  function setText(id, value) {
    const element = byId(id);
    if (element) {
      element.textContent = value == null ? "" : value;
    }
  }

  function componentNameFromId(id, prefix) {
    return id.startsWith(prefix) ? id.slice(prefix.length) : "";
  }

  function setDisplayedComponent(id, value) {
    setText(id, display(value));
  }

  function updateComparisonStates() {
    for (const name of componentNames) {
      const browserId = `browser-${name}`;
      const weburlId = `weburl-${name}`;
      const hasBrowserValue = browserValues.has(name);
      const hasWebURLValue = weburlValues.has(name);
      const browserValue = browserValues.get(name);
      const weburlValue = weburlValues.get(name);
      const valuesMatch = hasBrowserValue && hasWebURLValue && browserValue === weburlValue;

      setValueState(browserId, comparisonState(browserValue, hasBrowserValue, hasWebURLValue, valuesMatch));
      setValueState(weburlId, comparisonState(weburlValue, hasWebURLValue, hasBrowserValue, valuesMatch));
    }
  }

  function comparisonState(value, hasValue, hasCounterpart, valuesMatch) {
    if (!hasValue) {
      return "";
    }
    if (value === "") {
      return "empty";
    }
    return hasCounterpart && valuesMatch ? "match" : "mismatch";
  }

  function setBrowserComponent(name, value) {
    browserValues.set(name, value);
    setDisplayedComponent(`browser-${name}`, value);
    updateComparisonStates();
  }

  function setComparedValue(id, value) {
    const name = componentNameFromId(id, "weburl-");
    if (componentNames.includes(name)) {
      weburlValues.set(name, value);
      setDisplayedComponent(id, value);
      updateComparisonStates();
    } else {
      setText(id, value);
    }
  }

  function javaFieldFromId(id) {
    if (id.startsWith("weburl-java-")) {
      return { side: "weburl", name: id.slice("weburl-java-".length) };
    }
    if (id.startsWith("uri-java-")) {
      return { side: "uri", name: id.slice("uri-java-".length) };
    }
    return null;
  }

  function setJavaValue(id, value) {
    const field = javaFieldFromId(id);
    const text = value == null ? "" : value;
    if (!field || !javaFieldNames.includes(field.name)) {
      setText(id, text);
      setValueState(id, text === "(empty)" || text === "null" || text === "(unsupported)" ? "empty" : "value");
      return;
    }

    if (field.side === "weburl") {
      weburlJavaValues.set(field.name, text);
    } else {
      uriJavaValues.set(field.name, text);
    }
    setText(id, text);
    updateJavaComparisonStates();
  }

  function updateJavaComparisonStates() {
    for (const name of javaFieldNames) {
      setValueState(
        `weburl-java-${name}`,
        javaComparisonState(weburlJavaValues.get(name), weburlJavaValues.has(name), uriJavaValues.get(name), uriJavaValues.has(name))
      );
      setValueState(
        `uri-java-${name}`,
        javaComparisonState(uriJavaValues.get(name), uriJavaValues.has(name), weburlJavaValues.get(name), weburlJavaValues.has(name))
      );
    }
  }

  function javaComparisonState(value, hasValue, counterpart, hasCounterpart) {
    if (!hasValue) {
      return "";
    }
    if (value === "(unsupported)") {
      return "unsupported";
    }
    if (value === "(empty)" || value === "null") {
      return "empty";
    }
    if (!hasCounterpart || counterpart === "(unsupported)") {
      return "value";
    }
    return value === counterpart ? "match" : "mismatch";
  }

  function clearValue(id) {
    const browserName = componentNameFromId(id, "browser-");
    const weburlName = componentNameFromId(id, "weburl-");
    const javaField = javaFieldFromId(id);
    if (componentNames.includes(browserName)) {
      browserValues.delete(browserName);
    }
    if (componentNames.includes(weburlName)) {
      weburlValues.delete(weburlName);
    }
    if (javaField && javaFieldNames.includes(javaField.name)) {
      if (javaField.side === "weburl") {
        weburlJavaValues.delete(javaField.name);
      } else {
        uriJavaValues.delete(javaField.name);
      }
    }
    setText(id, "");
    setValueState(id, "");
    updateComparisonStates();
    updateJavaComparisonStates();
  }

  function setState(id, state) {
    const element = byId(id);
    if (element) {
      element.dataset.state = state;
    }
  }

  function clearBrowserFields() {
    for (const id of browserFieldIds) {
      clearValue(id);
    }
  }

  function renderBrowserURL(input, base) {
    try {
      const url = base === "" ? new URL(input) : new URL(input, base);
      setState("browser-panel", "ok");
      setText("browser-status", "Parsed");
      setText("browser-error", "");
      setBrowserComponent("href", url.href);
      setBrowserComponent("origin", url.origin);
      setBrowserComponent("protocol", url.protocol);
      setBrowserComponent("username", url.username);
      setBrowserComponent("password", url.password);
      setBrowserComponent("host", url.host);
      setBrowserComponent("hostname", url.hostname);
      setBrowserComponent("port", url.port);
      setBrowserComponent("pathname", url.pathname);
      setBrowserComponent("search", url.search);
      setBrowserComponent("hash", url.hash);
    } catch (error) {
      clearBrowserFields();
      setState("browser-panel", "error");
      setText("browser-status", "Rejected");
      setText("browser-error", error && error.message ? error.message : String(error));
    }
  }

  async function loadTeaVM() {
    try {
      if (!window.TeaVM || !window.TeaVM.wasmGC) {
        throw new Error("TeaVM WasmGC runtime is not available.");
      }

      let teavm;
      teavm = await window.TeaVM.wasmGC.load("wasm-gc/weburl-viewer.wasm", {
        installImports(imports) {
          imports.weburlViewer = {
            normalize(input, form) {
              const stringToJs = teavm.instance.exports["teavm.stringToJs"];
              const inputText = typeof input === "string" ? input : stringToJs(input);
              const formText = typeof form === "string" ? form : stringToJs(form);
              return inputText.normalize(formText);
            }
          };
        }
      });
      teavm.exports.main([]);
    } catch (error) {
      setText("runtime-status", error && error.message ? error.message : String(error));
      setState("browser-panel", "error");
      setState("weburl-panel", "error");
    }
  }

  window.WebURLViewer = {
    setText,
    setState,
    setComparedValue,
    setJavaValue,
    clearValue,
    renderBrowserURL,
    setReady() {
      setText("runtime-status", "WebAssembly ready");
    }
  };

  loadTeaVM();
})();
