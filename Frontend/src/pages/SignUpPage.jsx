import { useState } from "react";
import AuthCard from "../components/AuthCard.jsx";
import FormField from "../components/FormField.jsx";

function SignUpPage({ onSignUp, onShowSignIn }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();

    if (password !== confirmPassword) {
      setErrorMessage("Your passwords do not match. Please try again.");
      return;
    }

    setErrorMessage("");
    setIsSubmitting(true);

    try {
      await onSignUp(username.trim(), password);
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <AuthCard
      eyebrow="CREATE YOUR ACCOUNT"
      title="Join Student Hub"
      description="A few details and you're ready to go."
      footer={
        <>
          Already have an account?{" "}
          <button className="text-button" type="button" onClick={onShowSignIn}>
            Sign in
          </button>
        </>
      }
    >
      <form className="auth-form" onSubmit={handleSubmit}>
        <FormField
          id="signup-username"
          label="Username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          placeholder="Choose a username"
          autoComplete="username"
        />
        <FormField
          id="signup-password"
          label="Password"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="Create a password"
          autoComplete="new-password"
        />
        <FormField
          id="confirm-password"
          label="Confirm password"
          type="password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          placeholder="Type your password again"
          autoComplete="new-password"
        />
        {errorMessage && (
          <p className="form-message error-message" role="alert">
            {errorMessage}
          </p>
        )}
        <button className="primary-button" type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Creating account..." : "Create account"}
          {!isSubmitting && <span aria-hidden="true">→</span>}
        </button>
      </form>
    </AuthCard>
  );
}

export default SignUpPage;
