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

  function byId(id) {
    return document.getElementById(id);
  }

  function display(value) {
    return value === "" ? "(empty)" : value;
  }

  function setText(id, value) {
    const element = byId(id);
    if (element) {
      element.textContent = value == null ? "" : value;
    }
  }

  function setState(id, state) {
    const element = byId(id);
    if (element) {
      element.dataset.state = state;
    }
  }

  function clearBrowserFields() {
    for (const id of browserFieldIds) {
      setText(id, "");
    }
  }

  function renderBrowserURL(input, base) {
    try {
      const url = base === "" ? new URL(input) : new URL(input, base);
      setState("browser-panel", "ok");
      setText("browser-status", "Parsed");
      setText("browser-error", "");
      setText("browser-href", display(url.href));
      setText("browser-origin", display(url.origin));
      setText("browser-protocol", display(url.protocol));
      setText("browser-username", display(url.username));
      setText("browser-password", display(url.password));
      setText("browser-host", display(url.host));
      setText("browser-hostname", display(url.hostname));
      setText("browser-port", display(url.port));
      setText("browser-pathname", display(url.pathname));
      setText("browser-search", display(url.search));
      setText("browser-hash", display(url.hash));
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
    renderBrowserURL,
    setReady() {
      setText("runtime-status", "WebAssembly ready");
    }
  };

  loadTeaVM();
})();
