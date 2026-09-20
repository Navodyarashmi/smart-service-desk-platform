import { Mail, ShieldCheck, UserRound, X } from 'lucide-react'

import type { CurrentUser } from '../types/auth'

interface ProfileModalProps {
  user: CurrentUser
  onClose: () => void
}

function formatRole(role: string) {
  return role
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/\b\w/g, (character) => character.toUpperCase())
}

export function ProfileModal({ user, onClose }: ProfileModalProps) {
  const initials = user.fullName
    .split(' ')
    .map((part) => part[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose()
      }}
    >
      <section
        className="profile-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="profile-title"
      >
        <button
          className="modal-close profile-modal__close"
          type="button"
          onClick={onClose}
          aria-label="Close profile"
        >
          <X size={20} />
        </button>
        <div className="profile-modal__avatar">{initials}</div>
        <span className="dashboard-eyebrow">Authenticated profile</span>
        <h2 id="profile-title">{user.fullName}</h2>
        <div className="profile-modal__details">
          <div>
            <Mail size={18} />
            <span><small>Email address</small><strong>{user.email}</strong></span>
          </div>
          <div>
            <ShieldCheck size={18} />
            <span>
              <small>Access level</small>
              <strong>{user.roles.map(formatRole).join(' · ')}</strong>
            </span>
          </div>
          <div>
            <UserRound size={18} />
            <span><small>Account ID</small><strong>{user.id}</strong></span>
          </div>
        </div>
        <button className="primary-button" type="button" onClick={onClose}>
          Done
        </button>
      </section>
    </div>
  )
}
