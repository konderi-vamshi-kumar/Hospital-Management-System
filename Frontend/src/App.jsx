import { useState } from "react";
import SignInPage from "./pages/SignInPage.jsx";
import SignUpPage from "./pages/SignUpPage.jsx";
import { signIn, signUp } from "./services/authApi.js";

function App() {
  const [currentPage, setCurrentPage] = useState("signin");
  const [signedInUsername, setSignedInUsername] = useState("");
  const [notice, setNotice] = useState("");

  async function handleSignUp(username, password) {
    const user = await signUp(username, password);
    setNotice(`Account created for ${user.username}. Sign in to continue.`);
    setCurrentPage("signin");
  }

  async function handleSignIn(username, password) {
    const user = await signIn(username, password);
    setSignedInUsername(user.username);
    setNotice("");
  }

  function handleSignOut() {
    setSignedInUsername("");
    setNotice("");
  }

  return (
    <main className="app-shell">
      <header className="site-header">
        <a
          className="brand"
          href="#home"
          aria-label="Student Hub home"
          onClick={() => {
            setCurrentPage("signin");
            handleSignOut();
          }}
        >
          <span className="brand-mark" aria-hidden="true">
            S
          </span>
          <span>student hub</span>
        </a>
        <span className="header-note">A good place to get started</span>
      </header>

      <section className="welcome-panel" aria-label="Welcome">
        <div className="welcome-copy">
          <span className="eyebrow">YOUR NEXT CHAPTER</span>
          <h1>Small steps.<br />Bright futures.</h1>
          <p>
            Sign in to pick up where you left off, or create an account to
            begin.
          </p>
        </div>
        <div className="sunrise-art" aria-hidden="true">
          <span className="sun" />
          <span className="hill hill-back" />
          <span className="hill hill-front" />
          <span className="art-caption">make room to grow</span>
        </div>
      </section>

      <section className="form-panel" aria-live="polite">
        {currentPage === "signup" ? (
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
            signedInUsername={signedInUsername}
            onSignIn={handleSignIn}
            onSignOut={handleSignOut}
            onShowSignUp={() => {
              setNotice("");
              setCurrentPage("signup");
            }}
          />
        )}
        <p className="demo-note">
          Passwords are protected on the server. Sign-in is a learning demo and
          does not create a persistent session yet.
        </p>
      </section>

      <footer className="site-footer">
        <span>STUDENT HUB</span>
        <span>Made for learning, one step at a time.</span>
      </footer>
    </main>
  );
}

export default App;
