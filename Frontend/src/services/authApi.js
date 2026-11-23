const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

async function sendAuthRequest(path, username, password) {
  let response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ username, password }),
    });
  } catch {
    throw new Error(
      "Could not reach the Java server. Check that the backend is running on port 8080.",
    );
  }

  const responseBody = await response.json();

  if (!response.ok) {
    throw new Error(responseBody.message || "The request could not be completed.");
  }

  return responseBody;
}

export function signUp(username, password) {
  return sendAuthRequest("/api/auth/signup", username, password);
}

export function signIn(username, password) {
  return sendAuthRequest("/api/auth/signin", username, password);
}
