import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowRight,
  Eye,
  EyeOff,
  LoaderCircle,
  LockKeyhole,
  Mail,
} from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'

import {
  ApiRequestError,
  loginUser,
  saveAccessToken,
} from '../api/authApi'
import { AuthShell } from '../components/AuthShell'

const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Enter your email address.')
    .email('Enter a valid email address.'),
  password: z
    .string()
    .min(1, 'Enter your password.')
    .min(12, 'Password must contain at least 12 characters.'),
})

type LoginFormData = z.infer<typeof loginSchema>

export function LoginPage() {
  const navigate = useNavigate()
  const [showPassword, setShowPassword] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  })

  const submitLogin = async (formData: LoginFormData) => {
    setServerError(null)

    try {
      const response = await loginUser(formData)
      saveAccessToken(response.accessToken)
      navigate('/dashboard', { replace: true })
    } catch (error) {
      if (error instanceof ApiRequestError) {
        setServerError(error.message)

        if (error.fieldErrors.email) {
          setError('email', { message: error.fieldErrors.email })
        }

        if (error.fieldErrors.password) {
          setError('password', { message: error.fieldErrors.password })
        }

        return
      }

      setServerError(
        'We could not reach HelpHub. Check the server and try again.',
      )
    }
  }

  return (
    <AuthShell>
      <div className="auth-card">
        <header className="auth-card__header">
          <span className="auth-card__kicker">Secure account access</span>
            <h2>Welcome to HelpHub</h2>
            <p>Sign in to continue to your personalized workspace.</p>
        </header>

        {serverError && (
          <div className="form-alert" role="alert">
            {serverError}
          </div>
        )}

        <form
          className="auth-form"
          onSubmit={handleSubmit(submitLogin)}
          noValidate
        >
          <div className="form-field">
            <label htmlFor="email">Work email</label>

            <div
              className={`input-shell ${
                errors.email ? 'input-shell--error' : ''
              }`}
            >
              <Mail size={19} aria-hidden="true" />
              <input
                id="email"
                type="email"
                autoComplete="email"
                placeholder="you@company.com"
                aria-invalid={Boolean(errors.email)}
                {...register('email')}
              />
            </div>

            {errors.email && (
              <p className="field-error">{errors.email.message}</p>
            )}
          </div>

          <div className="form-field">
            <div className="label-row">
              <label htmlFor="password">Password</label>
              <span className="label-row__hint">Minimum 12 characters</span>
            </div>

            <div
              className={`input-shell ${
                errors.password ? 'input-shell--error' : ''
              }`}
            >
              <LockKeyhole size={19} aria-hidden="true" />

              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                placeholder="Enter your password"
                aria-invalid={Boolean(errors.password)}
                {...register('password')}
              />

              <button
                className="password-toggle"
                type="button"
                onClick={() => setShowPassword((current) => !current)}
                aria-label={
                  showPassword ? 'Hide password' : 'Show password'
                }
              >
                {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
              </button>
            </div>

            {errors.password && (
              <p className="field-error">{errors.password.message}</p>
            )}
          </div>

          <button
            className="primary-button"
            type="submit"
            disabled={isSubmitting}
          >
            {isSubmitting ? (
              <>
                <LoaderCircle className="spin" size={19} />
                Signing in…
              </>
            ) : (
              <>
                Sign in securely
                <ArrowRight size={19} />
              </>
            )}
          </button>
        </form>

        <p className="auth-card__switch">
          New to HelpHub? <Link to="/register">Create an account</Link>
        </p>

        <p className="security-note">
          <LockKeyhole size={14} />
          Your connection is protected and your password is never stored
          in plain text.
        </p>
      </div>
    </AuthShell>
  )
}