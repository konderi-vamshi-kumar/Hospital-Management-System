import { apiRequest, clearCsrfToken } from "./apiClient.js";

async function sendAuthRequest(path, username, password) {
  return apiRequest(path, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
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
    return await apiRequest("/api/auth/me");
  } catch (error) {
    if (error.status === 401) {
      return null;
    }
    throw error;
  }
}

export async function signOut() {
  await apiRequest("/api/auth/signout", {
    method: "POST",
  });
  clearCsrfToken();
}
