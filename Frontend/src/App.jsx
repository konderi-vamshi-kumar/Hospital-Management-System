import { useEffect, useState } from "react";
import SignInPage from "./pages/SignInPage.jsx";
import SignUpPage from "./pages/SignUpPage.jsx";
import PlatformAdminPage from "./features/platform-admin/PlatformAdminPage.jsx";
import HospitalAdminPage from "./features/hospital-admin/HospitalAdminPage.jsx";
import {
  getCurrentUser,
  signIn,
  signOut,
  signUp,
} from "./services/authApi.js";
import { acceptStaffInvitation } from "./services/hospitalAdminApi.js";

function App() {
  const [currentPage, setCurrentPage] = useState("signin");
  const [currentUser, setCurrentUser] = useState(null);
  const [isCheckingSession, setIsCheckingSession] = useState(true);
  const [notice, setNotice] = useState("");
  const [noticeIsError, setNoticeIsError] = useState(false);
  const isPlatformAdmin = currentUser?.roles?.includes("SUPER_ADMIN") || false;
  const isHospitalAdmin =
    currentUser?.hospitalMemberships?.some(
      (membership) =>
        membership.role === "HOSPITAL_ADMIN" && membership.status === "ACTIVE",
    ) || false;
  const isAdminDashboard = isPlatformAdmin || isHospitalAdmin;

  useEffect(() => {
    let isMounted = true;

    getCurrentUser()
      .then((user) => {
        if (isMounted) {
          setCurrentUser(user);
        }
      })
      .catch((error) => {
        if (isMounted) {
          setNotice(error.message);
          setNoticeIsError(true);
        }
      })
      .finally(() => {
        if (isMounted) {
          setIsCheckingSession(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  async function handleSignUp(username, password) {
    const user = await signUp(username, password);
    setNotice(
      `Account created for ${user.username}. Sign in to continue. If you have a staff invitation, you can accept its code after signing in.`,
    );
    setNoticeIsError(false);
    setCurrentPage("signin");
  }

  async function handleSignIn(username, password) {
    const user = await signIn(username, password);
    setCurrentUser(user);
    setNotice("");
    setNoticeIsError(false);
  }

  async function handleSignOut() {
    try {
      await signOut();
      setCurrentUser(null);
      setNotice("");
      setNoticeIsError(false);
    } catch (error) {
      setNotice(error.message);
      setNoticeIsError(true);
    }
  }

  async function handleAcceptInvitation(inviteCode) {
    const accepted = await acceptStaffInvitation(inviteCode);
    setCurrentUser((user) => {
      if (!user) {
        return user;
      }
      const membership = {
        hospitalId: accepted.hospitalId,
        role: accepted.role,
        status: accepted.status,
      };
      const existingMemberships = user.hospitalMemberships || [];
      const alreadyPresent = existingMemberships.some(
        (item) =>
          item.hospitalId === membership.hospitalId &&
          item.role === membership.role,
      );
      return {
        ...user,
        hospitalMemberships: alreadyPresent
          ? existingMemberships.map((item) =>
              item.hospitalId === membership.hospitalId &&
              item.role === membership.role
                ? membership
                : item,
            )
          : [...existingMemberships, membership],
      };
    });
    setNotice(`Invitation accepted. Your ${accepted.role} access is active.`);
    setNoticeIsError(false);
  }

  return (
    <main className={`app-shell${isAdminDashboard ? " app-shell-admin" : ""}`}>
      <header className="site-header">
        <a
          className="brand"
          href="#home"
          aria-label="Hospital Management System home"
          onClick={() => setCurrentPage("signin")}
        >
          <span className="brand-mark" aria-hidden="true">
            H
          </span>
          <span>Hospital Management System</span>
        </a>
        <span className="header-note">
          {isPlatformAdmin
            ? "Platform administration"
            : isHospitalAdmin
              ? "Hospital administration"
              : "A good place to get started"}
        </span>
      </header>

      {!isAdminDashboard && <section className="welcome-panel" aria-label="Welcome">
        <div className="welcome-copy">
          <span className="eyebrow">YOUR NEXT CHAPTER</span>
          <h1>Small steps.<br />Bright futures.</h1>
          <p>
            Sign in to manage your appointments, or create a patient account to
            get started.
          </p>
        </div>
        <div className="sunrise-art" aria-hidden="true">
          <span className="sun" />
          <span className="hill hill-back" />
          <span className="hill hill-front" />
          <span className="art-caption">make room to grow</span>
        </div>
      </section>}

      <section className="form-panel" aria-live="polite">
        {isCheckingSession ? (
          <p role="status">Checking your sign-in...</p>
        ) : isPlatformAdmin ? (
          <PlatformAdminPage
            currentUser={currentUser}
            notice={notice}
            noticeIsError={noticeIsError}
            onSignOut={handleSignOut}
          />
        ) : isHospitalAdmin ? (
          <HospitalAdminPage
            currentUser={currentUser}
            notice={notice}
            noticeIsError={noticeIsError}
            onSignOut={handleSignOut}
          />
        ) : currentPage === "signup" && !currentUser ? (
          <SignUpPage
            onSignUp={handleSignUp}
            onShowSignIn={() => {
              setNotice("");
              setNoticeIsError(false);
              setCurrentPage("signin");
            }}
          />
        ) : (
          <SignInPage
            notice={notice}
            noticeIsError={noticeIsError}
            signedInUsername={currentUser?.username || ""}
            onSignIn={handleSignIn}
            onSignOut={handleSignOut}
            onAcceptInvitation={handleAcceptInvitation}
            onShowSignUp={() => {
              setNotice("");
              setNoticeIsError(false);
              setCurrentPage("signup");
            }}
          />
        )}
        <p className="demo-note">
          Passwords are protected on the server. Your sign-in is maintained
          with a secure session cookie.
        </p>
      </section>

      <footer className="site-footer">
        <span>HOSPITAL MANAGEMENT SYSTEM</span>
        <span>Made for learning, one step at a time.</span>
      </footer>
    </main>
  );
}

export default App;
