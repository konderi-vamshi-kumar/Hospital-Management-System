import { useEffect, useState } from "react";
import {
  assignAdministrator,
  createBranch,
  createDepartment,
  createHospital,
  listAdministrators,
  listBranches,
  listDepartments,
  listHospitals,
  updateBranch,
  updateDepartment,
  updateHospital,
} from "../../services/platformApi.js";

function StatusBadge({ status }) {
  return (
    <span className={`status-badge status-${status.toLowerCase()}`}>
      {status}
    </span>
  );
}

function PlatformAdminPage({ currentUser, notice: appNotice, onSignOut }) {
  const [hospitals, setHospitals] = useState([]);
  const [selectedHospitalId, setSelectedHospitalId] = useState("");
  const [branches, setBranches] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [administrators, setAdministrators] = useState([]);
  const [hospitalName, setHospitalName] = useState("");
  const [branchForm, setBranchForm] = useState({
    name: "",
    address: "",
    phone: "",
  });
  const [departmentForm, setDepartmentForm] = useState({
    name: "",
    description: "",
  });
  const [administratorUsername, setAdministratorUsername] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [notice, setNotice] = useState("");

  const selectedHospital = hospitals.find(
    (hospital) => String(hospital.id) === selectedHospitalId,
  );

  useEffect(() => {
    let isMounted = true;
    listHospitals()
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
      setAdministrators([]);
      return undefined;
    }

    let isMounted = true;
    setErrorMessage("");
    Promise.all([
      listBranches(selectedHospitalId),
      listDepartments(selectedHospitalId),
      listAdministrators(selectedHospitalId),
    ])
      .then(([branchItems, departmentItems, administratorItems]) => {
        if (isMounted) {
          setBranches(branchItems);
          setDepartments(departmentItems);
          setAdministrators(administratorItems);
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

  function handleCreateHospital(event) {
    submitAction(
      event,
      () => createHospital(hospitalName),
      (hospital) => {
        setHospitals((items) => [...items, hospital]);
        setSelectedHospitalId(String(hospital.id));
        setHospitalName("");
      },
      "Hospital created.",
    );
  }

  function handleUpdateHospital(event) {
    if (!selectedHospital) {
      return;
    }
    const formData = new FormData(event.currentTarget);
    submitAction(
      event,
      () =>
        updateHospital(selectedHospital.id, {
          name: formData.get("name"),
          status: formData.get("status"),
        }),
      (hospital) => {
        setHospitals((items) =>
          items.map((item) => (item.id === hospital.id ? hospital : item)),
        );
      },
      "Hospital details updated.",
    );
  }

  function handleCreateBranch(event) {
    submitAction(
      event,
      () => createBranch(selectedHospitalId, branchForm),
      (branch) => {
        setBranches((items) => [...items, branch]);
        setBranchForm({ name: "", address: "", phone: "" });
      },
      "Branch added.",
    );
  }

  function handleCreateDepartment(event) {
    submitAction(
      event,
      () => createDepartment(selectedHospitalId, departmentForm),
      (department) => {
        setDepartments((items) => [...items, department]);
        setDepartmentForm({ name: "", description: "" });
      },
      "Department added.",
    );
  }

  function handleAssignAdministrator(event) {
    submitAction(
      event,
      () => assignAdministrator(selectedHospitalId, administratorUsername),
      (administrator) => {
        setAdministrators((items) => {
          const exists = items.some(
            (item) => item.userId === administrator.userId,
          );
          return exists
            ? items.map((item) =>
                item.userId === administrator.userId ? administrator : item,
              )
            : [...items, administrator];
        });
        setAdministratorUsername("");
      },
      "Hospital administrator assigned.",
    );
  }

  function handleBranchStatus(branch) {
    runAction(
      () =>
        updateBranch(selectedHospitalId, branch.id, {
          name: branch.name,
          address: branch.address,
          phone: branch.phone,
          status: branch.status === "ACTIVE" ? "INACTIVE" : "ACTIVE",
        }),
      (updatedBranch) =>
        setBranches((items) =>
          items.map((item) =>
            item.id === updatedBranch.id ? updatedBranch : item,
          ),
        ),
      "Branch status updated.",
    );
  }

  function handleDepartmentStatus(department) {
    runAction(
      () =>
        updateDepartment(selectedHospitalId, department.id, {
          name: department.name,
          description: department.description,
          status: department.status === "ACTIVE" ? "INACTIVE" : "ACTIVE",
        }),
      (updatedDepartment) =>
        setDepartments((items) =>
          items.map((item) =>
            item.id === updatedDepartment.id ? updatedDepartment : item,
          ),
        ),
      "Department status updated.",
    );
  }

  return (
    <section className="platform-page" aria-labelledby="platform-title">
      <header className="platform-header">
        <div>
          <span className="eyebrow">PLATFORM SUPER ADMIN</span>
          <h2 id="platform-title">Hospital onboarding</h2>
          <p className="form-description">
            Signed in as {currentUser.username}
          </p>
        </div>
        <button className="secondary-button" type="button" onClick={onSignOut}>
          Sign out
        </button>
      </header>

      {errorMessage && (
        <p className="form-message error-message" role="alert">
          {errorMessage}
        </p>
      )}
      {appNotice && (
        <p className="form-message error-message" role="alert">
          {appNotice}
        </p>
      )}
      {notice && (
        <p className="form-message success-message" role="status">
          {notice}
        </p>
      )}

      <div className="platform-layout">
        <aside className="platform-detail">
          <section className="platform-card">
            <h3>Add a hospital</h3>
            <form className="platform-form" onSubmit={handleCreateHospital}>
              <label htmlFor="new-hospital-name">Hospital name</label>
              <input
                id="new-hospital-name"
                maxLength={120}
                required
                value={hospitalName}
                onChange={(event) => setHospitalName(event.target.value)}
              />
              <button
                className="primary-button"
                type="submit"
                disabled={isSubmitting}
              >
                Create hospital
              </button>
            </form>
          </section>

          <section className="platform-card">
            <h3>Hospitals</h3>
            {isLoading ? (
              <p className="platform-empty" role="status">Loading hospitals...</p>
            ) : hospitals.length === 0 ? (
              <p className="platform-empty">No hospitals onboarded yet.</p>
            ) : (
              <ul className="platform-list">
                {hospitals.map((hospital) => (
                  <li key={hospital.id}>
                    <button
                      className={`platform-list-button${selectedHospitalId === String(hospital.id) ? " is-selected" : ""}`}
                      type="button"
                      onClick={() => setSelectedHospitalId(String(hospital.id))}
                    >
                      <span>{hospital.name}</span>
                      <StatusBadge status={hospital.status} />
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </aside>

        <div className="platform-grid">
          {!selectedHospital ? (
            <section className="platform-card platform-empty-state">
              <h3>Select a hospital</h3>
              <p className="platform-empty">
                Create a hospital or select one to manage its branches,
                departments, and administrators.
              </p>
            </section>
          ) : (
            <>
              <section className="platform-card" style={{ gridColumn: "1 / -1" }}>
                <div className="hospital-summary">
                  <div>
                    <span className="eyebrow">SELECTED HOSPITAL</span>
                    <h3>{selectedHospital.name}</h3>
                  </div>
                  <StatusBadge status={selectedHospital.status} />
                </div>
                <form
                  className="hospital-edit-form"
                  onSubmit={handleUpdateHospital}
                >
                  <input
                    aria-label="Hospital name"
                    defaultValue={selectedHospital.name}
                    key={`${selectedHospital.id}-${selectedHospital.name}`}
                    maxLength={120}
                    name="name"
                    required
                  />
                  <select
                    aria-label="Hospital status"
                    defaultValue={selectedHospital.status}
                    key={`${selectedHospital.id}-${selectedHospital.status}`}
                    name="status"
                  >
                    <option value="ACTIVE">Active</option>
                    <option value="INACTIVE">Inactive</option>
                  </select>
                  <button
                    className="secondary-button"
                    type="submit"
                    disabled={isSubmitting}
                  >
                    Save hospital
                  </button>
                </form>
              </section>

              <section className="platform-card">
                <h3>Branches</h3>
                <form className="platform-form" onSubmit={handleCreateBranch}>
                  <label htmlFor="branch-name">Branch name</label>
                  <input
                    id="branch-name"
                    maxLength={120}
                    required
                    value={branchForm.name}
                    onChange={(event) =>
                      setBranchForm({ ...branchForm, name: event.target.value })
                    }
                  />
                  <label htmlFor="branch-address">Address</label>
                  <textarea
                    id="branch-address"
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
                  <label htmlFor="branch-phone">Phone (optional)</label>
                  <input
                    id="branch-phone"
                    maxLength={30}
                    value={branchForm.phone}
                    onChange={(event) =>
                      setBranchForm({
                        ...branchForm,
                        phone: event.target.value,
                      })
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
                      <button
                        className="secondary-button"
                        type="button"
                        onClick={() => handleBranchStatus(branch)}
                      >
                        {branch.status === "ACTIVE" ? "Deactivate" : "Activate"}
                      </button>
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
                  <label htmlFor="department-name">Department name</label>
                  <input
                    id="department-name"
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
                  <label htmlFor="department-description">
                    Description (optional)
                  </label>
                  <textarea
                    id="department-description"
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
                      <button
                        className="secondary-button"
                        type="button"
                        onClick={() => handleDepartmentStatus(department)}
                      >
                        {department.status === "ACTIVE"
                          ? "Deactivate"
                          : "Activate"}
                      </button>
                    </li>
                  ))}
                </ul>
                {departments.length === 0 && (
                  <p className="platform-empty">No departments added.</p>
                )}
              </section>

              <section className="platform-card">
                <h3>Hospital administrators</h3>
                <p className="form-description">
                  Assign an existing account by username. Invitations are not
                  included in this phase.
                </p>
                <form
                  className="platform-form admin-assignment-form"
                  onSubmit={handleAssignAdministrator}
                >
                  <label htmlFor="administrator-username">Username</label>
                  <input
                    id="administrator-username"
                    autoComplete="off"
                    maxLength={30}
                    minLength={3}
                    pattern="[A-Za-z0-9_]+"
                    required
                    value={administratorUsername}
                    onChange={(event) =>
                      setAdministratorUsername(event.target.value)
                    }
                  />
                  <button
                    className="primary-button"
                    type="submit"
                    disabled={isSubmitting}
                  >
                    Assign administrator
                  </button>
                </form>
                <ul className="platform-list">
                  {administrators.map((administrator) => (
                    <li
                      className="resource-list-item"
                      key={administrator.membershipId}
                    >
                      <span>{administrator.username}</span>
                      <StatusBadge status={administrator.status} />
                    </li>
                  ))}
                </ul>
                {administrators.length === 0 && (
                  <p className="platform-empty">
                    No administrators assigned.
                  </p>
                )}
              </section>
            </>
          )}
        </div>
      </div>
    </section>
  );
}

export default PlatformAdminPage;
