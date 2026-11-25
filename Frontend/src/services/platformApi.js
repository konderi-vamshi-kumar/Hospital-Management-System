import { apiRequest } from "./apiClient.js";

function sendJson(path, method, body) {
  return apiRequest(path, {
    method,
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });
}

export function listHospitals() {
  return apiRequest("/api/platform/hospitals");
}

export function createHospital(name) {
  return sendJson("/api/platform/hospitals", "POST", { name });
}

export function updateHospital(hospitalId, update) {
  return sendJson(`/api/platform/hospitals/${hospitalId}`, "PUT", update);
}

export function listBranches(hospitalId) {
  return apiRequest(`/api/platform/hospitals/${hospitalId}/branches`);
}

export function createBranch(hospitalId, branch) {
  return sendJson(
    `/api/platform/hospitals/${hospitalId}/branches`,
    "POST",
    branch,
  );
}

export function updateBranch(hospitalId, branchId, update) {
  return sendJson(
    `/api/platform/hospitals/${hospitalId}/branches/${branchId}`,
    "PUT",
    update,
  );
}

export function listDepartments(hospitalId) {
  return apiRequest(`/api/platform/hospitals/${hospitalId}/departments`);
}

export function createDepartment(hospitalId, department) {
  return sendJson(
    `/api/platform/hospitals/${hospitalId}/departments`,
    "POST",
    department,
  );
}

export function updateDepartment(hospitalId, departmentId, update) {
  return sendJson(
    `/api/platform/hospitals/${hospitalId}/departments/${departmentId}`,
    "PUT",
    update,
  );
}

export function listAdministrators(hospitalId) {
  return apiRequest(`/api/platform/hospitals/${hospitalId}/administrators`);
}

export function assignAdministrator(hospitalId, username) {
  return sendJson(
    `/api/platform/hospitals/${hospitalId}/administrators`,
    "POST",
    { username },
  );
}
