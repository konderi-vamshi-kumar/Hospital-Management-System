const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

let csrfToken;

async function sendRequest(path, options = {}) {
  let response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      credentials: "include",
    });
  } catch {
    throw new Error(
      "Could not reach the Java server. Check that the backend is running on port 8080.",
    );
  }

  const responseText = await response.text();
  let responseBody = null;
  if (responseText) {
    try {
      responseBody = JSON.parse(responseText);
    } catch {
      throw new Error("The Java server returned an unreadable response.");
    }
  }

  if (!response.ok) {
    const error = new Error(
      responseBody?.message || "The request could not be completed.",
    );
    error.status = response.status;
    throw error;
  }

  return responseBody;
}

async function getCsrfToken() {
  const response = await sendRequest("/api/auth/csrf");
  csrfToken = response.token;
  return csrfToken;
}

async function sendAuthRequest(path, username, password) {
  const token = await getCsrfToken();
  return sendRequest(path, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-XSRF-TOKEN": token,
    },
    body: JSON.stringify({ username, password }),
  });
}

export function signUp(username, password) {
  return sendAuthRequest("/api/auth/signup", username, password);
}

export function signIn(username, password) {
  return sendAuthRequest("/api/auth/signin", username, password);
}

export async function getCurrentUser() {
  try {
    return await sendRequest("/api/auth/me");
  } catch (error) {
    if (error.status === 401) {
      return null;
    }
    throw error;
  }
}

export async function signOut() {
  const token = await getCsrfToken();
  await sendRequest("/api/auth/signout", {
    method: "POST",
    headers: {
      "X-XSRF-TOKEN": token,
    },
  });
  csrfToken = undefined;
}
