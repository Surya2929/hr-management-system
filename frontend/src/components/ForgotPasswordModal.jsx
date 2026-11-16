/**
 * "Forgot password?" help.
 * No email service is connected, so a reset link could never reach the employee.
 * The real way back in is a new temporary password from HR + "Activate Account".
 */
export default function ForgotPasswordModal({ onClose, onActivate }) {
  return (
    <div className="fixed inset-0 bg-neutral-900/50 flex items-center justify-center p-4 z-50" onClick={onClose}>
      <div className="bg-white rounded-3xl shadow-2xl max-w-sm w-full max-h-full overflow-y-auto p-6"
        onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true" aria-labelledby="forgotTitle">
        <h3 id="forgotTitle" className="text-2xl font-light text-neutral-900 mb-3">Forgot Password</h3>

        <ol className="space-y-3 text-sm text-neutral-600 list-decimal pl-5">
          <li>Ask your <span className="font-medium text-neutral-900">HR team</span> for a new temporary password
            (Employees page → “New temp password”).</li>
          <li>Open <span className="font-medium text-neutral-900">Activate Account</span> and enter your email
            and that temporary password.</li>
          <li>Set your own new password. You are signed in straight away.</li>
        </ol>

        <p className="text-xs text-neutral-400 mt-4">HR admin password is managed on the server.</p>

        <div className="flex gap-3 mt-5">
          {onActivate && (
            <button type="button" onClick={onActivate} className="btn-primary flex-1 text-sm">
              Go to Activate Account
            </button>
          )}
          <button type="button" onClick={onClose} className="btn-secondary flex-1 text-sm">Close</button>
        </div>
      </div>
    </div>
  );
}
