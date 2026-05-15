(function () {
  const jsdomURLPromise = import("./jsdom-url.js").then(() => {
    if (!window.WebURLJsdomURL || typeof window.WebURLJsdomURL.parse !== "function") {
      throw new Error("jsdom whatwg-url parser is not available.");
    }
    return window.WebURLJsdomURL;
  });
  const referenceFieldIds = [
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
    "ascii-string",
    "normalized-string",
    "display-string",
    "rfc2396-string",
    "scheme",
    "decoded-authority",
    "raw-authority",
    "decoded-user-info",
    "raw-user-info",
    "decoded-username",
    "raw-username",
    "decoded-password",
    "raw-password",
    "host",
    "port",
    "raw-port",
    "decoded-path",
    "raw-path",
    "decoded-query",
    "raw-query",
    "decoded-fragment",
    "raw-fragment"
  ];
  const referenceValues = new Map();
  const weburlValues = new Map();
  const weburlJavaValues = new Map();
  const uriJavaValues = new Map();
  let referenceRenderId = 0;

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
      const hasBrowserValue = referenceValues.has(name);
      const hasWebURLValue = weburlValues.has(name);
      const browserValue = referenceValues.get(name);
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

  function setReferenceComponent(name, value) {
    referenceValues.set(name, value);
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

  function javaValueRecord(text, kind) {
    const normalizedKind = kind === "empty" || kind === "null" || kind === "unsupported" ? kind : "value";
    const isEmptyLike = normalizedKind === "empty" || normalizedKind === "null";
    return {
      key: `${normalizedKind}\u0000${text}`,
      state: normalizedKind === "unsupported" ? "unsupported" : isEmptyLike ? "empty" : "value"
    };
  }

  function setJavaValue(id, value, kind) {
    const field = javaFieldFromId(id);
    const text = value == null ? "" : value;
    const record = javaValueRecord(text, kind);
    if (!field || !javaFieldNames.includes(field.name)) {
      setText(id, text);
      setValueState(id, record.state);
      return;
    }

    if (field.side === "weburl") {
      weburlJavaValues.set(field.name, record);
    } else {
      uriJavaValues.set(field.name, record);
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
    if (value.state === "unsupported") {
      return "unsupported";
    }
    if (value.state === "empty") {
      return "empty";
    }
    if (!hasCounterpart || counterpart.state === "unsupported") {
      return "value";
    }
    return value.key === counterpart.key ? "match" : "mismatch";
  }

  function clearValue(id) {
    const browserName = componentNameFromId(id, "browser-");
    const weburlName = componentNameFromId(id, "weburl-");
    const javaField = javaFieldFromId(id);
    if (componentNames.includes(browserName)) {
      referenceValues.delete(browserName);
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

  function clearReferenceFields() {
    for (const id of referenceFieldIds) {
      clearValue(id);
    }
  }

  function renderJsdomFailure(error, status) {
    clearReferenceFields();
    setState("browser-panel", "error");
    setText("browser-status", status);
    setText("browser-error", error && error.message ? error.message : String(error));
  }

  function renderJsdomURL(input, base) {
    const renderId = ++referenceRenderId;
    clearReferenceFields();
    setState("browser-panel", "loading");
    setText("browser-status", "Loading");
    setText("browser-error", "");

    jsdomURLPromise.then(
      parser => {
        if (renderId !== referenceRenderId) {
          return;
        }

        try {
          const url = parser.parse(input, base);
          setState("browser-panel", "ok");
          setText("browser-status", "Parsed");
          setText("browser-error", "");
          setReferenceComponent("href", url.href);
          setReferenceComponent("origin", url.origin);
          setReferenceComponent("protocol", url.protocol);
          setReferenceComponent("username", url.username);
          setReferenceComponent("password", url.password);
          setReferenceComponent("host", url.host);
          setReferenceComponent("hostname", url.hostname);
          setReferenceComponent("port", url.port);
          setReferenceComponent("pathname", url.pathname);
          setReferenceComponent("search", url.search);
          setReferenceComponent("hash", url.hash);
        } catch (error) {
          renderJsdomFailure(error, "Rejected");
        }
      },
      error => {
        if (renderId === referenceRenderId) {
          renderJsdomFailure(error, "Failed");
        }
      }
    );
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
      const message = error && error.message ? error.message : String(error);
      setText("runtime-status", message);
      for (const panel of [
        ["browser-panel", "browser-status", "browser-error"],
        ["weburl-panel", "weburl-status", "weburl-error"],
        ["weburl-java-panel", "weburl-java-status", "weburl-java-error"],
        ["uri-java-panel", "uri-java-status", "uri-java-error"]
      ]) {
        setState(panel[0], "error");
        setText(panel[1], "Failed");
        setText(panel[2], message);
      }
    }
  }

  window.WebURLViewer = {
    setText,
    setState,
    setComparedValue,
    setJavaValue,
    clearValue,
    renderJsdomURL,
    setReady() {
      setText("runtime-status", "WebAssembly ready");
    }
  };

  loadTeaVM();
})();
