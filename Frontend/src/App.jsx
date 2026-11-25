import { useEffect, useState } from "react";
import SignInPage from "./pages/SignInPage.jsx";
import SignUpPage from "./pages/SignUpPage.jsx";
import PlatformAdminPage from "./features/platform-admin/PlatformAdminPage.jsx";
import {
  getCurrentUser,
  signIn,
  signOut,
  signUp,
} from "./services/authApi.js";

function App() {
  const [currentPage, setCurrentPage] = useState("signin");
  const [currentUser, setCurrentUser] = useState(null);
  const [isCheckingSession, setIsCheckingSession] = useState(true);
  const [notice, setNotice] = useState("");
  const isPlatformAdmin = currentUser?.roles?.includes("SUPER_ADMIN") || false;

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
    setNotice(`Patient account created for ${user.username}. Sign in to continue.`);
    setCurrentPage("signin");
  }

  async function handleSignIn(username, password) {
    const user = await signIn(username, password);
    setCurrentUser(user);
    setNotice("");
  }

  async function handleSignOut() {
    try {
      await signOut();
      setCurrentUser(null);
      setNotice("");
    } catch (error) {
      setNotice(error.message);
    }
  }

  return (
    <main className={`app-shell${isPlatformAdmin ? " app-shell-admin" : ""}`}>
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
          {isPlatformAdmin ? "Platform administration" : "A good place to get started"}
        </span>
      </header>

      {!isPlatformAdmin && <section className="welcome-panel" aria-label="Welcome">
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
            onSignOut={handleSignOut}
          />
        ) : currentPage === "signup" && !currentUser ? (
          <SignUpPage
            onSignUp={handleSignUp}
            onShowSignIn={() => {
              setNotice("");
              setCurrentPage("signin");
            }}
          />
        ) : (
          <SignInPage
            notice={notice}
            signedInUsername={currentUser?.username || ""}
            onSignIn={handleSignIn}
            onSignOut={handleSignOut}
            onShowSignUp={() => {
              setNotice("");
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
