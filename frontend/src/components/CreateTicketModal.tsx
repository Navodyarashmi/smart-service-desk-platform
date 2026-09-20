import { zodResolver } from '@hookform/resolvers/zod'
import {
  FileText,
  Layers3,
  LoaderCircle,
  Send,
  Sparkles,
  Tag,
  X,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'

import { ApiRequestError } from '../api/apiClient'
import { createTicket } from '../api/ticketApi'
import type { TicketResponse } from '../types/ticket'

const ticketSchema = z.object({
  title: z
    .string()
    .trim()
    .min(4, 'Provide a clear title.')
    .max(160, 'Title must not exceed 160 characters.'),
  description: z
    .string()
    .trim()
    .min(10, 'Describe the issue using at least 10 characters.')
    .max(4000, 'Description must not exceed 4000 characters.'),
  category: z.enum([
    'HARDWARE',
    'SOFTWARE',
    'NETWORK',
    'ACCESS',
    'OTHER',
  ]),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH', 'URGENT']),
})

type TicketFormData = z.infer<typeof ticketSchema>

interface CreateTicketModalProps {
  open: boolean
  accessToken: string
  onClose: () => void
  onCreated: (ticket: TicketResponse) => void
}

export function CreateTicketModal({
  open,
  accessToken,
  onClose,
  onCreated,
}: CreateTicketModalProps) {
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<TicketFormData>({
    resolver: zodResolver(ticketSchema),
    defaultValues: {
      title: '',
      description: '',
      category: 'HARDWARE',
      priority: 'MEDIUM',
    },
  })

  useEffect(() => {
    if (!open) {
      return
    }

    document.body.style.overflow = 'hidden'

    const closeWithEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    window.addEventListener('keydown', closeWithEscape)

    return () => {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', closeWithEscape)
    }
  }, [open, onClose])

  if (!open) {
    return null
  }

  const submitTicket = async (formData: TicketFormData) => {
    setServerError(null)

    try {
      const createdTicket = await createTicket(accessToken, formData)
      onCreated(createdTicket)
      reset()
      onClose()
    } catch (error) {
      if (error instanceof ApiRequestError) {
        setServerError(error.message)

        if (error.fieldErrors.title) {
          setError('title', { message: error.fieldErrors.title })
        }

        if (error.fieldErrors.description) {
          setError('description', {
            message: error.fieldErrors.description,
          })
        }

        if (error.fieldErrors.category) {
          setError('category', {
            message: error.fieldErrors.category,
          })
        }

        if (error.fieldErrors.priority) {
          setError('priority', {
            message: error.fieldErrors.priority,
          })
        }

        return
      }

      setServerError(
        'The ticket could not be created. Please try again.',
      )
    }
  }

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose()
        }
      }}
    >
      <section
        className="ticket-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="create-ticket-title"
      >
        <header className="ticket-modal__header">
          <div className="ticket-modal__heading">
            <span className="ticket-modal__icon">
              <Sparkles size={21} />
            </span>

            <div>
              <span>New support request</span>
              <h2 id="create-ticket-title">Create a ticket</h2>
            </div>
          </div>

          <button
            className="modal-close"
            type="button"
            onClick={onClose}
            aria-label="Close create-ticket dialog"
          >
            <X size={20} />
          </button>
        </header>

        <form
          className="ticket-form"
          onSubmit={handleSubmit(submitTicket)}
          noValidate
        >
          {serverError && (
            <div className="form-alert" role="alert">
              {serverError}
            </div>
          )}

          <div className="form-field">
            <label htmlFor="ticket-title">What do you need help with?</label>

            <div
              className={`input-shell ${
                errors.title ? 'input-shell--error' : ''
              }`}
            >
              <Tag size={19} aria-hidden="true" />
              <input
                id="ticket-title"
                type="text"
                placeholder="Example: Office Wi-Fi is unavailable"
                aria-invalid={Boolean(errors.title)}
                {...register('title')}
              />
            </div>

            {errors.title && (
              <p className="field-error">{errors.title.message}</p>
            )}
          </div>

          <div className="form-field">
            <label htmlFor="ticket-description">Description</label>

            <div
              className={`textarea-shell ${
                errors.description ? 'input-shell--error' : ''
              }`}
            >
              <FileText size={19} aria-hidden="true" />
              <textarea
                id="ticket-description"
                rows={5}
                placeholder="Explain what happened, when it started, and how it affects your work."
                aria-invalid={Boolean(errors.description)}
                {...register('description')}
              />
            </div>

            {errors.description && (
              <p className="field-error">
                {errors.description.message}
              </p>
            )}
          </div>

          <div className="ticket-form__row">
            <div className="form-field">
              <label htmlFor="ticket-category">Category</label>

              <div className="select-shell">
                <Layers3 size={18} aria-hidden="true" />
                <select
                  id="ticket-category"
                  {...register('category')}
                >
                  <option value="HARDWARE">Hardware</option>
                  <option value="SOFTWARE">Software</option>
                  <option value="NETWORK">Network</option>
                  <option value="ACCESS">Access</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              {errors.category && (
                <p className="field-error">
                  {errors.category.message}
                </p>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="ticket-priority">Priority</label>

              <div className="select-shell">
                <Tag size={18} aria-hidden="true" />
                <select
                  id="ticket-priority"
                  {...register('priority')}
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="URGENT">Urgent</option>
                </select>
              </div>

              {errors.priority && (
                <p className="field-error">
                  {errors.priority.message}
                </p>
              )}
            </div>
          </div>

          <footer className="ticket-modal__footer">
            <button
              className="secondary-button"
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Cancel
            </button>

            <button
              className="primary-button primary-button--compact"
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <LoaderCircle className="spin" size={18} />
                  Creating…
                </>
              ) : (
                <>
                  <Send size={18} />
                  Submit ticket
                </>
              )}
            </button>
          </footer>
        </form>
      </section>
    </div>
  )
}