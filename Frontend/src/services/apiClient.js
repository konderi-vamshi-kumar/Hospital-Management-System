const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

let csrfToken;
let csrfTokenRequest;

class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

async function sendRequest(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      credentials: "include",
    });
  } catch (error) {
    throw new Error(
      "Could not reach the Java server. Check that the backend is running on port 8080.",
      { cause: error },
    );
  }

  const responseText = await response.text();
  let responseBody = null;
  if (responseText) {
    try {
      responseBody = JSON.parse(responseText);
    } catch (error) {
      throw new Error("The Java server returned an unreadable response.", {
        cause: error,
      });
    }
  }

  if (!response.ok) {
    throw new ApiError(
      responseBody?.message || "The request could not be completed.",
      response.status,
    );
  }

  return responseBody;
}

async function getCsrfToken() {
  if (csrfToken) {
    return csrfToken;
  }
  if (!csrfTokenRequest) {
    csrfTokenRequest = sendRequest("/api/auth/csrf").then((response) => {
      csrfToken = response.token;
      return csrfToken;
    }).finally(() => {
      csrfTokenRequest = undefined;
    });
  }
  return csrfTokenRequest;
}

export async function apiRequest(path, options = {}) {
  const method = (options.method || "GET").toUpperCase();
  const headers = new Headers(options.headers);

  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    headers.set("X-XSRF-TOKEN", await getCsrfToken());
  }

  return sendRequest(path, { ...options, method, headers });
}

export function clearCsrfToken() {
  csrfToken = undefined;
}
