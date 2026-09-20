import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowRight,
  Eye,
  EyeOff,
  LoaderCircle,
  LockKeyhole,
  Mail,
  UserRound,
} from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'

import {
  ApiRequestError,
  loginUser,
  registerUser,
  saveAccessToken,
} from '../api/authApi'
import { AuthShell } from '../components/AuthShell'

const registrationSchema = z
  .object({
    fullName: z
      .string()
      .trim()
      .min(2, 'Enter your full name.')
      .max(150, 'Full name must not exceed 150 characters.'),
    email: z
      .string()
      .min(1, 'Enter your email address.')
      .email('Enter a valid email address.'),
    password: z
      .string()
      .min(12, 'Use at least 12 characters.')
      .max(64, 'Password must not exceed 64 characters.'),
    confirmPassword: z.string().min(1, 'Confirm your password.'),
  })
  .refine((values) => values.password === values.confirmPassword, {
    message: 'Passwords do not match.',
    path: ['confirmPassword'],
  })

type RegistrationFormData = z.infer<typeof registrationSchema>

export function RegisterPage() {
  const navigate = useNavigate()
  const [showPassword, setShowPassword] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<RegistrationFormData>({
    resolver: zodResolver(registrationSchema),
    defaultValues: {
      fullName: '',
      email: '',
      password: '',
      confirmPassword: '',
    },
  })

  const submitRegistration = async (
    formData: RegistrationFormData,
  ) => {
    setServerError(null)

    try {
      await registerUser({
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
      })

      const loginResponse = await loginUser({
        email: formData.email,
        password: formData.password,
      })

      saveAccessToken(loginResponse.accessToken)
      navigate('/dashboard', { replace: true })
    } catch (error) {
      if (error instanceof ApiRequestError) {
        setServerError(error.message)

        if (error.fieldErrors.fullName) {
          setError('fullName', {
            message: error.fieldErrors.fullName,
          })
        }

        if (error.fieldErrors.email) {
          setError('email', {
            message: error.fieldErrors.email,
          })
        }

        if (error.fieldErrors.password) {
          setError('password', {
            message: error.fieldErrors.password,
          })
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
          <span className="auth-card__kicker">Start in minutes</span>
          <h2>Create your HelpHub account</h2>
          <p>Join your team’s smarter support workspace.</p>
        </header>

        {serverError && (
          <div className="form-alert" role="alert">
            {serverError}
          </div>
        )}

        <form
          className="auth-form"
          onSubmit={handleSubmit(submitRegistration)}
          noValidate
        >
          <div className="form-field">
            <label htmlFor="fullName">Full name</label>

            <div
              className={`input-shell ${
                errors.fullName ? 'input-shell--error' : ''
              }`}
            >
              <UserRound size={19} aria-hidden="true" />
              <input
                id="fullName"
                type="text"
                autoComplete="name"
                placeholder="Your full name"
                aria-invalid={Boolean(errors.fullName)}
                {...register('fullName')}
              />
            </div>

            {errors.fullName && (
              <p className="field-error">{errors.fullName.message}</p>
            )}
          </div>

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
              <span className="label-row__hint">
                12–64 characters
              </span>
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
                autoComplete="new-password"
                placeholder="Create a strong password"
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

          <div className="form-field">
            <label htmlFor="confirmPassword">Confirm password</label>

            <div
              className={`input-shell ${
                errors.confirmPassword ? 'input-shell--error' : ''
              }`}
            >
              <LockKeyhole size={19} aria-hidden="true" />
              <input
                id="confirmPassword"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                placeholder="Enter your password again"
                aria-invalid={Boolean(errors.confirmPassword)}
                {...register('confirmPassword')}
              />
            </div>

            {errors.confirmPassword && (
              <p className="field-error">
                {errors.confirmPassword.message}
              </p>
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
                Creating account…
              </>
            ) : (
              <>
                Create account
                <ArrowRight size={19} />
              </>
            )}
          </button>
        </form>

        <p className="auth-card__switch">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>

        <p className="security-note">
          <LockKeyhole size={14} />
          Your password is protected with secure one-way hashing.
        </p>
      </div>
    </AuthShell>
  )
}