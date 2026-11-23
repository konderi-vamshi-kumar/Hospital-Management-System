function AuthCard({ eyebrow, title, description, children, footer }) {
  return (
    <div className="auth-card">
      <span className="eyebrow">{eyebrow}</span>
      <h2>{title}</h2>
      <p className="form-description">{description}</p>
      {children}
      <div className="auth-footer">{footer}</div>
    </div>
  );
}

export default AuthCard;
