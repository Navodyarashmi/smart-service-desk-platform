import {
  CheckCircle2,
  Headphones,
  ShieldCheck,
  Sparkles,
  Zap,
} from 'lucide-react'
import type { ReactNode } from 'react'

interface AuthShellProps {
  children: ReactNode
}

const benefits = [
  {
    icon: Zap,
    title: 'Faster resolutions',
    description: 'Route every request to the right person without delay.',
  },
  {
    icon: ShieldCheck,
    title: 'Secure by design',
    description: 'Role-based access keeps sensitive work protected.',
  },
  {
    icon: CheckCircle2,
    title: 'Clear accountability',
    description: 'Track ownership and progress from request to resolution.',
  },
]

export function AuthShell({ children }: AuthShellProps) {
  return (
    <main className="auth-page">
      <section className="brand-panel" aria-label="HelpHub introduction">
        <div className="brand-panel__glow brand-panel__glow--one" />
        <div className="brand-panel__glow brand-panel__glow--two" />

        <div className="brand">
          <span className="brand__mark">
            <Headphones size={24} strokeWidth={2.2} />
          </span>

          <span className="brand__name">HelpHub</span>
        </div>

        <div className="brand-panel__content">
          <div className="eyebrow">
            <Sparkles size={15} />
            Smart service management
          </div>

          <h1>Support that feels effortless.</h1>

          <p className="brand-panel__lead">
            One calm workspace for reporting issues, coordinating teams,
            and keeping every employee informed.
          </p>

          <div className="benefit-list">
            {benefits.map(({ icon: Icon, title, description }) => (
              <article className="benefit" key={title}>
                <span className="benefit__icon">
                  <Icon size={19} />
                </span>

                <div>
                  <h2>{title}</h2>
                  <p>{description}</p>
                </div>
              </article>
            ))}
          </div>
        </div>

        <p className="brand-panel__footer">
          Built for teams that care about excellent service.
        </p>
      </section>

      <section className="auth-panel">
        <div className="mobile-brand">
          <span className="brand__mark">
            <Headphones size={22} />
          </span>
          <span className="brand__name">HelpHub</span>
        </div>

        {children}
      </section>
    </main>
  )
}