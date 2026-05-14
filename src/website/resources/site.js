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
  const browserValues = new Map();
  const weburlValues = new Map();

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

  function setJavaValue(id, value) {
    setText(id, value);
    setValueState(id, value === "(empty)" || value === "null" ? "empty" : "value");
  }

  function clearValue(id) {
    const browserName = componentNameFromId(id, "browser-");
    const weburlName = componentNameFromId(id, "weburl-");
    if (componentNames.includes(browserName)) {
      browserValues.delete(browserName);
    }
    if (componentNames.includes(weburlName)) {
      weburlValues.delete(weburlName);
    }
    setText(id, "");
    setValueState(id, "");
    updateComparisonStates();
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
