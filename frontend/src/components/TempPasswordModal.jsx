import { useState } from 'react';
import { FiCheck, FiCopy, FiKey } from 'react-icons/fi';

/**
 * Shows the one-time temporary password to HR right after creating an employee
 * (or issuing a new one). It is never shown again - the server keeps only a hash.
 */
export default function TempPasswordModal({ name, email, password, onClose }) {
  const [copied, setCopied] = useState(false);

  const loginUrl = `${window.location.origin}/login`;
  const message =
    `Hi ${name}, your SmartHR account is ready.\n` +
    `Go to ${loginUrl} → "Activate Account"\n` +
    `Email: ${email}\nTemporary password: ${password}\n` +
    `You will set your own password after verification.`;

  const copy = async (text) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // Clipboard can be blocked (e.g. non-HTTPS); the password is still visible to copy by hand
    }
  };

  return (
    <div className="fixed inset-0 bg-neutral-900/50 flex items-center justify-center p-4 z-50" role="dialog" aria-modal="true" aria-labelledby="tempPwTitle">
      <div className="bg-white rounded-3xl shadow-2xl max-w-md w-full max-h-full overflow-y-auto p-6">
        <span className="w-12 h-12 rounded-full bg-brand-500 text-white grid place-items-center mb-4">
          <FiKey className="w-5 h-5" aria-hidden="true" />
        </span>
        <h3 id="tempPwTitle" className="text-2xl font-light text-neutral-900">Temporary password</h3>
        <p className="text-sm text-neutral-500 mt-1">
          Share this with <span className="text-neutral-900 font-medium">{name}</span> ({email}).
          They will use it once on <b>Activate Account</b> and then set their own password.
        </p>

        <div className="mt-5 flex items-center gap-2 p-2 pl-4 rounded-full border border-neutral-300 bg-neutral-50">
          <code className="flex-1 font-mono text-lg tracking-wider text-neutral-900 select-all break-all">{password}</code>
          <button type="button" onClick={() => copy(password)} className="btn-primary text-sm py-2 px-4 shrink-0">
            {copied ? <><FiCheck aria-hidden="true" /> Copied</> : <><FiCopy aria-hidden="true" /> Copy</>}
          </button>
        </div>

        <p className="mt-4 text-xs text-amber-800 bg-amber-50 border border-amber-200 rounded-2xl p-3">
          This password will not be shown again. If it gets lost, use “New temp password” on the Employees page.
        </p>

        <div className="flex flex-col sm:flex-row gap-2 mt-5">
          <button type="button" onClick={() => copy(message)} className="btn-secondary text-sm flex-1">
            <FiCopy aria-hidden="true" /> Copy full message
          </button>
          <button type="button" onClick={onClose} className="btn-primary text-sm flex-1">Done</button>
        </div>
      </div>
    </div>
  );
}
