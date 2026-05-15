if (typeof globalThis.SharedArrayBuffer === "undefined") {
  // whatwg-url's webidl-conversions bundle probes SharedArrayBuffer.prototype at module load time.
  globalThis.SharedArrayBuffer = class SharedArrayBuffer {
    get byteLength() {
      throw new TypeError("SharedArrayBuffer is unavailable.");
    }

    get growable() {
      throw new TypeError("SharedArrayBuffer is unavailable.");
    }
  };
}

const { URL: JsdomURL } = await import("./vendor/whatwg-url.mjs");

export function parseJsdomURL(input, base) {
  const url = base === "" ? new JsdomURL(input) : new JsdomURL(input, base);
  return {
    href: url.href,
    origin: url.origin,
    protocol: url.protocol,
    username: url.username,
    password: url.password,
    host: url.host,
    hostname: url.hostname,
    port: url.port,
    pathname: url.pathname,
    search: url.search,
    hash: url.hash
  };
}

window.WebURLJsdomURL = Object.freeze({
  parse: parseJsdomURL
});
