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

export function listManagedHospitals() {
  return apiRequest("/api/hospital-admin/hospitals");
}

export function listHospitalBranches(hospitalId) {
  return apiRequest(`/api/hospital-admin/hospitals/${hospitalId}/branches`);
}

export function createHospitalBranch(hospitalId, branch) {
  return sendJson(
    `/api/hospital-admin/hospitals/${hospitalId}/branches`,
    "POST",
    branch,
  );
}

export function updateHospitalBranch(hospitalId, branchId, update) {
  return sendJson(
    `/api/hospital-admin/hospitals/${hospitalId}/branches/${branchId}`,
    "PUT",
    update,
  );
}

export function listHospitalDepartments(hospitalId) {
  return apiRequest(`/api/hospital-admin/hospitals/${hospitalId}/departments`);
}

export function createHospitalDepartment(hospitalId, department) {
  return sendJson(
    `/api/hospital-admin/hospitals/${hospitalId}/departments`,
    "POST",
    department,
  );
}

export function updateHospitalDepartment(hospitalId, departmentId, update) {
  return sendJson(
    `/api/hospital-admin/hospitals/${hospitalId}/departments/${departmentId}`,
    "PUT",
    update,
  );
}

export function listStaffInvitations(hospitalId) {
  return apiRequest(`/api/hospital-admin/hospitals/${hospitalId}/invitations`);
}

export function createStaffInvitation(hospitalId, role) {
  return sendJson(
    `/api/hospital-admin/hospitals/${hospitalId}/invitations`,
    "POST",
    { role },
  );
}

export function revokeStaffInvitation(hospitalId, invitationId) {
  return apiRequest(
    `/api/hospital-admin/hospitals/${hospitalId}/invitations/${invitationId}`,
    { method: "DELETE" },
  );
}

export function acceptStaffInvitation(inviteCode) {
  return sendJson("/api/invitations/accept", "POST", { inviteCode });
}
