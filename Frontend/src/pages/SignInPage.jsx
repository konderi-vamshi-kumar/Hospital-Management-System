import { useState } from "react";
import AuthCard from "../components/AuthCard.jsx";
import FormField from "../components/FormField.jsx";

function SignInPage({
  notice,
  noticeIsError,
  signedInUsername,
  onSignIn,
  onSignOut,
  onAcceptInvitation,
  onShowSignUp,
}) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [inviteCode, setInviteCode] = useState("");
  const [invitationError, setInvitationError] = useState("");
  const [isAcceptingInvitation, setIsAcceptingInvitation] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    try {
      await onSignIn(username.trim(), password);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleAcceptInvitation(event) {
    event.preventDefault();
    setInvitationError("");
    setIsAcceptingInvitation(true);
    try {
      await onAcceptInvitation(inviteCode.trim());
      setInviteCode("");
    } catch (error) {
      setInvitationError(error.message);
    } finally {
      setIsAcceptingInvitation(false);
    }
  }

  if (signedInUsername) {
    return (
      <AuthCard
        eyebrow="YOU'RE SIGNED IN"
        title={`Welcome, ${signedInUsername}!`}
        description="You're all set. Your next chapter starts here."
        footer={
          <button className="text-button" type="button" onClick={onSignOut}>
            Sign out
          </button>
        }
      >
        <div className="success-mark" aria-hidden="true">
          ✓
        </div>
        {notice && (
          <p
            className={`form-message ${noticeIsError ? "error-message" : "success-message"}`}
            role={noticeIsError ? "alert" : "status"}
          >
            {notice}
          </p>
        )}
        <form className="auth-form" onSubmit={handleAcceptInvitation}>
          <p className="form-description">
            Have a staff invitation code? Enter it here to activate your
            hospital access.
          </p>
          <label className="form-field" htmlFor="staff-invite-code">
            <span>Invitation code</span>
            <input
              id="staff-invite-code"
              autoComplete="off"
              maxLength={43}
              minLength={43}
              required
              value={inviteCode}
              onChange={(event) => setInviteCode(event.target.value)}
            />
          </label>
          {invitationError && (
            <p className="form-message error-message" role="alert">
              {invitationError}
            </p>
          )}
          <button
            className="primary-button"
            type="submit"
            disabled={isAcceptingInvitation}
          >
            {isAcceptingInvitation ? "Accepting invitation..." : "Accept invitation"}
          </button>
        </form>
      </AuthCard>
    );
  }

  return (
    <AuthCard
      eyebrow="WELCOME BACK"
      title="Sign in"
      description="It's good to have you back."
      footer={
        <>
          New around here?{" "}
          <button className="text-button" type="button" onClick={onShowSignUp}>
            Create an account
          </button>
        </>
      }
    >
      <form className="auth-form" onSubmit={handleSubmit}>
        {notice && (
          <p
            className={`form-message ${noticeIsError ? "error-message" : "success-message"}`}
            role={noticeIsError ? "alert" : "status"}
          >
            {notice}
          </p>
        )}
        <FormField
          id="signin-username"
          label="Username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          placeholder="Enter your username"
          autoComplete="username"
        />
        <FormField
          id="signin-password"
          label="Password"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="Enter your password"
          autoComplete="current-password"
        />
        {errorMessage && (
          <p className="form-message error-message" role="alert">
            {errorMessage}
          </p>
        )}
        <button className="primary-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Signing in..." : "Sign in"}
          {!isSubmitting && <span aria-hidden="true">→</span>}
        </button>
      </form>
    </AuthCard>
  );
}

export default SignInPage;
