import { useEffect, useState } from "react";
import {
  createHospitalBranch,
  createHospitalDepartment,
  createStaffInvitation,
  listHospitalBranches,
  listHospitalDepartments,
  listManagedHospitals,
  listStaffInvitations,
  revokeStaffInvitation,
  updateHospitalBranch,
  updateHospitalDepartment,
} from "../../services/hospitalAdminApi.js";

function StatusBadge({ status }) {
  return (
    <span className={`status-badge status-${status.toLowerCase()}`}>
      {status}
    </span>
  );
}

function BranchEditor({ branch, onSave, disabled }) {
  return (
    <details className="resource-editor">
      <summary className="secondary-button">Edit</summary>
      <form
        className="platform-form"
        onSubmit={(event) => {
          const form = new FormData(event.currentTarget);
          onSave(event, branch, {
            name: form.get("name"),
            address: form.get("address"),
            phone: form.get("phone"),
            status: form.get("status"),
          });
        }}
      >
        <label htmlFor={`branch-name-${branch.id}`}>Branch name</label>
        <input
          id={`branch-name-${branch.id}`}
          defaultValue={branch.name}
          key={`${branch.id}-${branch.name}`}
          maxLength={120}
          name="name"
          required
        />
        <label htmlFor={`branch-address-${branch.id}`}>Address</label>
        <textarea
          id={`branch-address-${branch.id}`}
          defaultValue={branch.address}
          key={`${branch.id}-${branch.address}`}
          maxLength={500}
          name="address"
          required
          rows={2}
        />
        <label htmlFor={`branch-phone-${branch.id}`}>Phone</label>
        <input
          id={`branch-phone-${branch.id}`}
          defaultValue={branch.phone || ""}
          key={`${branch.id}-${branch.phone || ""}`}
          maxLength={30}
          name="phone"
        />
        <label htmlFor={`branch-status-${branch.id}`}>Status</label>
        <select
          id={`branch-status-${branch.id}`}
          defaultValue={branch.status}
          key={`${branch.id}-${branch.status}`}
          name="status"
        >
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
        </select>
        <button className="primary-button" type="submit" disabled={disabled}>
          Save branch
        </button>
      </form>
    </details>
  );
}

function DepartmentEditor({ department, onSave, disabled }) {
  return (
    <details className="resource-editor">
      <summary className="secondary-button">Edit</summary>
      <form
        className="platform-form"
        onSubmit={(event) => {
          const form = new FormData(event.currentTarget);
          onSave(event, department, {
            name: form.get("name"),
            description: form.get("description"),
            status: form.get("status"),
          });
        }}
      >
        <label htmlFor={`department-name-${department.id}`}>
          Department name
        </label>
        <input
          id={`department-name-${department.id}`}
          defaultValue={department.name}
          key={`${department.id}-${department.name}`}
          maxLength={120}
          name="name"
          required
        />
        <label htmlFor={`department-description-${department.id}`}>
          Description
        </label>
        <textarea
          id={`department-description-${department.id}`}
          defaultValue={department.description || ""}
          key={`${department.id}-${department.description || ""}`}
          maxLength={500}
          name="description"
          rows={2}
        />
        <label htmlFor={`department-status-${department.id}`}>Status</label>
        <select
          id={`department-status-${department.id}`}
          defaultValue={department.status}
          key={`${department.id}-${department.status}`}
          name="status"
        >
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
        </select>
        <button className="primary-button" type="submit" disabled={disabled}>
          Save department
        </button>
      </form>
    </details>
  );
}

function HospitalAdminPage({
  currentUser,
  notice: appNotice,
  noticeIsError,
  onSignOut,
}) {
  const [hospitals, setHospitals] = useState([]);
  const [selectedHospitalId, setSelectedHospitalId] = useState("");
  const [branches, setBranches] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [branchForm, setBranchForm] = useState({
    name: "",
    address: "",
    phone: "",
  });
  const [departmentForm, setDepartmentForm] = useState({
    name: "",
    description: "",
  });
  const [inviteRole, setInviteRole] = useState("DOCTOR");
  const [createdInvite, setCreatedInvite] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [notice, setNotice] = useState("");

  const selectedHospital = hospitals.find(
    (hospital) => String(hospital.id) === selectedHospitalId,
  );

  useEffect(() => {
    let isMounted = true;
    listManagedHospitals()
      .then((items) => {
        if (isMounted) {
          setHospitals(items);
          if (items.length > 0) {
            setSelectedHospitalId(String(items[0].id));
          }
        }
      })
      .catch((error) => {
        if (isMounted) {
          setErrorMessage(error.message);
        }
      })
      .finally(() => {
        if (isMounted) {
          setIsLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    if (!selectedHospitalId) {
      setBranches([]);
      setDepartments([]);
      setInvitations([]);
      return undefined;
    }

    let isMounted = true;
    setErrorMessage("");
    Promise.all([
      listHospitalBranches(selectedHospitalId),
      listHospitalDepartments(selectedHospitalId),
      listStaffInvitations(selectedHospitalId),
    ])
      .then(([branchItems, departmentItems, invitationItems]) => {
        if (isMounted) {
          setBranches(branchItems);
          setDepartments(departmentItems);
          setInvitations(invitationItems);
        }
      })
      .catch((error) => {
        if (isMounted) {
          setErrorMessage(error.message);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [selectedHospitalId]);

  async function submitAction(event, action, onSuccess, successMessage) {
    event.preventDefault();
    setErrorMessage("");
    setNotice("");
    setIsSubmitting(true);
    try {
      const result = await action();
      onSuccess(result);
      setNotice(successMessage);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  async function runAction(action, onSuccess, successMessage) {
    setErrorMessage("");
    setNotice("");
    setIsSubmitting(true);
    try {
      const result = await action();
      onSuccess(result);
      setNotice(successMessage);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleCreateBranch(event) {
    submitAction(
      event,
      () => createHospitalBranch(selectedHospitalId, branchForm),
      (branch) => {
        setBranches((items) => [...items, branch]);
        setBranchForm({ name: "", address: "", phone: "" });
      },
      "Branch added.",
    );
  }

  function handleUpdateBranch(event, branch, update) {
    submitAction(
      event,
      () => updateHospitalBranch(selectedHospitalId, branch.id, update),
      (updatedBranch) =>
        setBranches((items) =>
          items.map((item) =>
            item.id === updatedBranch.id ? updatedBranch : item,
          ),
        ),
      "Branch updated.",
    );
  }

  function handleCreateDepartment(event) {
    submitAction(
      event,
      () => createHospitalDepartment(selectedHospitalId, departmentForm),
      (department) => {
        setDepartments((items) => [...items, department]);
        setDepartmentForm({ name: "", description: "" });
      },
      "Department added.",
    );
  }

  function handleUpdateDepartment(event, department, update) {
    submitAction(
      event,
      () =>
        updateHospitalDepartment(selectedHospitalId, department.id, update),
      (updatedDepartment) =>
        setDepartments((items) =>
          items.map((item) =>
            item.id === updatedDepartment.id ? updatedDepartment : item,
          ),
        ),
      "Department updated.",
    );
  }

  function handleCreateInvitation(event) {
    submitAction(
      event,
      () => createStaffInvitation(selectedHospitalId, inviteRole),
      (invitation) => {
        setCreatedInvite(invitation);
        setInvitations((items) => [invitation, ...items]);
      },
      "Invitation created. Copy the code and share it privately.",
    );
  }

  function handleRevokeInvitation(invitation) {
    runAction(
      () => revokeStaffInvitation(selectedHospitalId, invitation.id),
      () =>
        setInvitations((items) =>
          items.map((item) =>
            item.id === invitation.id ? { ...item, status: "REVOKED" } : item,
          ),
        ),
      "Invitation revoked.",
    );
  }

  return (
    <section className="platform-page" aria-labelledby="hospital-admin-title">
      <header className="platform-header">
        <div>
          <span className="eyebrow">HOSPITAL ADMINISTRATION</span>
          <h2 id="hospital-admin-title">Manage your hospital</h2>
          <p className="form-description">
            Signed in as {currentUser.username}
          </p>
        </div>
        <button className="secondary-button" type="button" onClick={onSignOut}>
          Sign out
        </button>
      </header>

      {appNotice && (
        <p
          className={`form-message ${noticeIsError ? "error-message" : "success-message"}`}
          role={noticeIsError ? "alert" : "status"}
        >
          {appNotice}
        </p>
      )}
      {errorMessage && (
        <p className="form-message error-message" role="alert">
          {errorMessage}
        </p>
      )}
      {notice && (
        <p className="form-message success-message" role="status">
          {notice}
        </p>
      )}

      {hospitals.length > 1 && (
        <label className="hospital-selector">
          Hospital
          <select
            value={selectedHospitalId}
            onChange={(event) => setSelectedHospitalId(event.target.value)}
          >
            {hospitals.map((hospital) => (
              <option key={hospital.id} value={hospital.id}>
                {hospital.name}
              </option>
            ))}
          </select>
        </label>
      )}

      {isLoading ? (
        <p role="status">Loading your hospital...</p>
      ) : !selectedHospital ? (
        <section className="platform-card">
          <h3>No active hospital access</h3>
          <p className="platform-empty">
            Your account does not currently have an active hospital
            administrator membership.
          </p>
        </section>
      ) : (
        <>
          <section className="platform-card">
            <div className="hospital-summary">
              <div>
                <span className="eyebrow">YOUR HOSPITAL</span>
                <h3>{selectedHospital.name}</h3>
              </div>
              <StatusBadge status={selectedHospital.status} />
            </div>
          </section>

          <div className="platform-grid">
            <section className="platform-card">
              <h3>Branches</h3>
              <form className="platform-form" onSubmit={handleCreateBranch}>
                <label htmlFor="admin-branch-name">Branch name</label>
                <input
                  id="admin-branch-name"
                  maxLength={120}
                  required
                  value={branchForm.name}
                  onChange={(event) =>
                    setBranchForm({ ...branchForm, name: event.target.value })
                  }
                />
                <label htmlFor="admin-branch-address">Address</label>
                <textarea
                  id="admin-branch-address"
                  maxLength={500}
                  required
                  rows={2}
                  value={branchForm.address}
                  onChange={(event) =>
                    setBranchForm({
                      ...branchForm,
                      address: event.target.value,
                    })
                  }
                />
                <label htmlFor="admin-branch-phone">Phone (optional)</label>
                <input
                  id="admin-branch-phone"
                  maxLength={30}
                  value={branchForm.phone}
                  onChange={(event) =>
                    setBranchForm({ ...branchForm, phone: event.target.value })
                  }
                />
                <button
                  className="primary-button"
                  type="submit"
                  disabled={isSubmitting}
                >
                  Add branch
                </button>
              </form>
              <ul className="platform-list">
                {branches.map((branch) => (
                  <li className="resource-list-item" key={branch.id}>
                    <span>
                      <strong>{branch.name}</strong>
                      <br />
                      {branch.address}
                    </span>
                    <StatusBadge status={branch.status} />
                    <BranchEditor
                      branch={branch}
                      disabled={isSubmitting}
                      onSave={handleUpdateBranch}
                    />
                  </li>
                ))}
              </ul>
              {branches.length === 0 && (
                <p className="platform-empty">No branches added.</p>
              )}
            </section>

            <section className="platform-card">
              <h3>Departments</h3>
              <form
                className="platform-form"
                onSubmit={handleCreateDepartment}
              >
                <label htmlFor="admin-department-name">Department name</label>
                <input
                  id="admin-department-name"
                  maxLength={120}
                  required
                  value={departmentForm.name}
                  onChange={(event) =>
                    setDepartmentForm({
                      ...departmentForm,
                      name: event.target.value,
                    })
                  }
                />
                <label htmlFor="admin-department-description">
                  Description (optional)
                </label>
                <textarea
                  id="admin-department-description"
                  maxLength={500}
                  rows={2}
                  value={departmentForm.description}
                  onChange={(event) =>
                    setDepartmentForm({
                      ...departmentForm,
                      description: event.target.value,
                    })
                  }
                />
                <button
                  className="primary-button"
                  type="submit"
                  disabled={isSubmitting}
                >
                  Add department
                </button>
              </form>
              <ul className="platform-list">
                {departments.map((department) => (
                  <li className="resource-list-item" key={department.id}>
                    <span>
                      <strong>{department.name}</strong>
                      {department.description && (
                        <>
                          <br />
                          {department.description}
                        </>
                      )}
                    </span>
                    <StatusBadge status={department.status} />
                    <DepartmentEditor
                      department={department}
                      disabled={isSubmitting}
                      onSave={handleUpdateDepartment}
                    />
                  </li>
                ))}
              </ul>
              {departments.length === 0 && (
                <p className="platform-empty">No departments added.</p>
              )}
            </section>

            <section className="platform-card">
              <h3>Invite hospital staff</h3>
              <p className="form-description">
                Generate a one-time code and share it privately. It expires
                after seven days. The invitee creates an account or signs in,
                then accepts the code from their session.
              </p>
              <form
                className="platform-form"
                onSubmit={handleCreateInvitation}
              >
                <label htmlFor="staff-invite-role">Staff role</label>
                <select
                  id="staff-invite-role"
                  value={inviteRole}
                  onChange={(event) => setInviteRole(event.target.value)}
                >
                  <option value="DOCTOR">Doctor</option>
                  <option value="HOSPITAL_ADMIN">Hospital administrator</option>
                </select>
                <button
                  className="primary-button"
                  type="submit"
                  disabled={isSubmitting}
                >
                  Generate invitation code
                </button>
              </form>
              {createdInvite && (
                <div className="invite-code-panel" role="status">
                  <label htmlFor="created-invite-code">
                    One-time invitation code
                  </label>
                  <textarea
                    id="created-invite-code"
                    readOnly
                    rows={2}
                    value={createdInvite.inviteCode}
                  />
                  <p className="platform-empty">
                    The code is returned only once. Copy it now and share it
                    using a trusted channel.
                  </p>
                </div>
              )}
              <ul className="platform-list">
                {invitations.map((invitation) => (
                  <li className="resource-list-item" key={invitation.id}>
                    <span>
                      <strong>{invitation.role.replace("_", " ")}</strong>
                      <br />
                      Expires {new Date(invitation.expiresAt).toLocaleString()}
                    </span>
                    <StatusBadge status={invitation.status} />
                    {invitation.status === "PENDING" && (
                      <button
                        className="secondary-button"
                        type="button"
                        disabled={isSubmitting}
                        onClick={() => handleRevokeInvitation(invitation)}
                      >
                        Revoke
                      </button>
                    )}
                  </li>
                ))}
              </ul>
              {invitations.length === 0 && (
                <p className="platform-empty">No staff invitations created.</p>
              )}
            </section>
          </div>
        </>
      )}
    </section>
  );
}

export default HospitalAdminPage;
