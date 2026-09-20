import { zodResolver } from '@hookform/resolvers/zod'
import {
  AlertTriangle,
  Ban,
  CalendarDays,
  Download,
  FileText,
  Layers3,
  LoaderCircle,
  MessageSquare,
  Paperclip,
  Pencil,
  Save,
  Send,
  ShieldCheck,
  Sparkles,
  Tag,
  TicketCheck,
  Upload,
  UserRound,
  X,
} from 'lucide-react'
import './TicketDetailsModal.css'
import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'

import { ApiRequestError } from '../api/apiClient'
import {
  cancelTicket,
  addTicketComment,
  changeTicketStatus,
  claimTicket,
  getTicket,
  getStaffTicket,
  getTicketActivity,
  getTicketAttachments,
  downloadTicketAttachment,
  uploadTicketAttachment,
  updateTicket,
} from '../api/ticketApi'
import type { TicketActivity, TicketAttachment, TicketDetails } from '../types/ticket'

const updateSchema = z.object({
  title: z
    .string()
    .trim()
    .min(4, 'Provide a clear title.')
    .max(160, 'Title must not exceed 160 characters.'),
  description: z
    .string()
    .trim()
    .min(10, 'Provide at least 10 characters.')
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

type UpdateFormData = z.infer<typeof updateSchema>

interface TicketDetailsModalProps {
  ticketId: string
  accessToken: string
  onClose: () => void
  onChanged: (ticket: TicketDetails) => void
  isStaff?: boolean
  currentUserId: string
}

function formatLabel(value: string): string {
  return value
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/\b\w/g, (character) => character.toUpperCase())
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function formatFileSize(sizeBytes: number): string {
  if (sizeBytes < 1024) return `${sizeBytes} B`
  if (sizeBytes < 1024 * 1024) return `${(sizeBytes / 1024).toFixed(1)} KB`
  return `${(sizeBytes / (1024 * 1024)).toFixed(1)} MB`
}

export function TicketDetailsModal({
  ticketId,
  accessToken,
  onClose,
  onChanged,
  isStaff = false,
  currentUserId,
}: TicketDetailsModalProps) {
  const [ticket, setTicket] = useState<TicketDetails | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [editing, setEditing] = useState(false)
  const [confirmingCancel, setConfirmingCancel] = useState(false)
  const [cancelling, setCancelling] = useState(false)
  const [staffActionRunning, setStaffActionRunning] = useState(false)
  const [activity, setActivity] = useState<TicketActivity[]>([])
  const [commentMessage, setCommentMessage] = useState('')
  const [internalNote, setInternalNote] = useState(false)
  const [commentSubmitting, setCommentSubmitting] = useState(false)
  const [attachments, setAttachments] = useState<TicketAttachment[]>([])
  const [attachmentUploading, setAttachmentUploading] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<UpdateFormData>({
    resolver: zodResolver(updateSchema),
  })

  useEffect(() => {
    let active = true

    const loadTicket = isStaff ? getStaffTicket : getTicket

    Promise.all([
      loadTicket(accessToken, ticketId),
      getTicketActivity(accessToken, ticketId),
      getTicketAttachments(accessToken, ticketId),
    ])
      .then(([loadedTicket, loadedActivity, loadedAttachments]) => {
        if (!active) {
          return
        }

        setTicket(loadedTicket)
        reset({
          title: loadedTicket.title,
          description: loadedTicket.description,
          category: loadedTicket.category,
          priority: loadedTicket.priority,
        })
        setActivity(loadedActivity)
        setAttachments(loadedAttachments)
      })
      .catch(() => {
        if (active) {
          setLoadError('The ticket details could not be loaded.')
        }
      })

    return () => {
      active = false
    }
  }, [accessToken, isStaff, reset, ticketId])

  useEffect(() => {
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
  }, [onClose])

  const saveChanges = async (formData: UpdateFormData) => {
    setActionError(null)

    try {
      const updatedTicket = await updateTicket(
        accessToken,
        ticketId,
        formData,
      )

      setTicket(updatedTicket)
      setEditing(false)
      onChanged(updatedTicket)
      setActivity(await getTicketActivity(accessToken, ticketId))
    } catch (error) {
      if (error instanceof ApiRequestError) {
        setActionError(error.message)

        if (error.fieldErrors.title) {
          setError('title', { message: error.fieldErrors.title })
        }

        if (error.fieldErrors.description) {
          setError('description', {
            message: error.fieldErrors.description,
          })
        }

        return
      }

      setActionError('The changes could not be saved.')
    }
  }

  const confirmCancellation = async () => {
    setCancelling(true)
    setActionError(null)

    try {
      const cancelledTicket = await cancelTicket(
        accessToken,
        ticketId,
      )

      setTicket(cancelledTicket)
      setConfirmingCancel(false)
      onChanged(cancelledTicket)
      setActivity(await getTicketActivity(accessToken, ticketId))
    } catch (error) {
      setActionError(
        error instanceof ApiRequestError
          ? error.message
          : 'The ticket could not be cancelled.',
      )
    } finally {
      setCancelling(false)
    }
  }

  const editable =
    !isStaff && (ticket?.status === 'OPEN' || ticket?.status === 'ASSIGNED')

  const runStaffAction = async (
    action: 'claim' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED',
  ) => {
    setStaffActionRunning(true)
    setActionError(null)

    try {
      const changedTicket =
        action === 'claim'
          ? await claimTicket(accessToken, ticketId)
          : await changeTicketStatus(accessToken, ticketId, action)
      setTicket(changedTicket)
      onChanged(changedTicket)
      setActivity(await getTicketActivity(accessToken, ticketId))
    } catch (error) {
      setActionError(
        error instanceof ApiRequestError
          ? error.message
          : 'The ticket workflow could not be updated.',
      )
    } finally {
      setStaffActionRunning(false)
    }
  }

  const nextStaffAction = (() => {
    if (!ticket || !isStaff) return null
    if (!ticket.assigneeId) {
      return { action: 'claim' as const, label: 'Claim ticket' }
    }
    if (ticket.assigneeId !== currentUserId) return null
    if (ticket.status === 'ASSIGNED') {
      return { action: 'IN_PROGRESS' as const, label: 'Start work' }
    }
    if (ticket.status === 'IN_PROGRESS') {
      return { action: 'RESOLVED' as const, label: 'Mark resolved' }
    }
    if (ticket.status === 'RESOLVED') {
      return { action: 'CLOSED' as const, label: 'Close ticket' }
    }
    return null
  })()

  const submitComment = async (event: FormEvent) => {
    event.preventDefault()
    const message = commentMessage.trim()
    if (!message) return
    setCommentSubmitting(true)
    setActionError(null)
    try {
      const added = await addTicketComment(accessToken, ticketId, message, isStaff && internalNote)
      setActivity((current) => [...current, added])
      setCommentMessage('')
    } catch (error) {
      setActionError(error instanceof ApiRequestError ? error.message : 'The message could not be added.')
    } finally {
      setCommentSubmitting(false)
    }
  }

  const uploadAttachment = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.currentTarget.files?.[0]
    event.currentTarget.value = ''
    if (!file) return

    setAttachmentUploading(true)
    setActionError(null)
    try {
      const added = await uploadTicketAttachment(accessToken, ticketId, file)
      setAttachments((current) => [...current, added])
      setActivity(await getTicketActivity(accessToken, ticketId))
    } catch (error) {
      setActionError(
        error instanceof ApiRequestError
          ? error.message
          : 'The attachment could not be uploaded.',
      )
    } finally {
      setAttachmentUploading(false)
    }
  }

  const downloadAttachment = async (attachment: TicketAttachment) => {
    setActionError(null)
    try {
      await downloadTicketAttachment(accessToken, ticketId, attachment)
    } catch {
      setActionError('The attachment could not be downloaded.')
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
        className="ticket-modal ticket-details-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="ticket-details-title"
      >
        <header className="ticket-modal__header">
          <div className="ticket-modal__heading">
            <span className="ticket-modal__icon">
              <TicketCheck size={21} />
            </span>

            <div>
              <span>{ticket?.referenceCode ?? 'Ticket details'}</span>
              <h2 id="ticket-details-title">
                {editing ? 'Edit ticket' : 'Support request'}
              </h2>
            </div>
          </div>

          <button
            className="modal-close"
            type="button"
            onClick={onClose}
            aria-label="Close ticket details"
          >
            <X size={20} />
          </button>
        </header>

        {!ticket && !loadError && (
          <div className="details-loading">
            <LoaderCircle className="spin" size={25} />
            <p>Loading ticket details…</p>
          </div>
        )}

        {loadError && (
          <div className="details-loading">
            <span className="error-state__icon">
              <AlertTriangle size={23} />
            </span>
            <strong>Unable to load ticket</strong>
            <p>{loadError}</p>
          </div>
        )}

        {ticket && !editing && (
          <div className="ticket-details">
            {actionError && (
              <div className="form-alert" role="alert">
                {actionError}
              </div>
            )}

            <div className="ticket-details__title-row">
              <div>
                <h3>{ticket.title}</h3>
                <p>Created {formatDate(ticket.createdAt)}</p>
              </div>

              <span
                className={`status status--${ticket.status
                  .toLowerCase()
                  .replaceAll('_', '-')}`}
              >
                {formatLabel(ticket.status)}
              </span>
            </div>

            <div className="detail-grid">
              <div className="detail-item">
                <Layers3 size={18} />
                <span>
                  <small>Category</small>
                  <strong>{formatLabel(ticket.category)}</strong>
                </span>
              </div>

              <div className="detail-item">
                <Tag size={18} />
                <span>
                  <small>Priority</small>
                  <strong>{formatLabel(ticket.priority)}</strong>
                </span>
              </div>

              <div className="detail-item">
                <CalendarDays size={18} />
                <span>
                  <small>Last updated</small>
                  <strong>{formatDate(ticket.updatedAt)}</strong>
                </span>
              </div>

              <div className="detail-item">
                <UserRound size={18} />
                <span>
                  <small>Assigned technician</small>
                  <strong>
                    {ticket.assigneeId ? 'Assigned' : 'Not assigned'}
                  </strong>
                </span>
              </div>
            </div>

            <section className="description-panel">
              <div>
                <FileText size={18} />
                <h4>Description</h4>
              </div>
              <p>{ticket.description}</p>
            </section>

            <section className="attachment-panel">
              <div className="attachment-panel__heading">
                <div>
                  <Paperclip size={18} />
                  <h4>Attachments</h4>
                </div>
                <label className="attachment-upload-button">
                  {attachmentUploading ? (
                    <LoaderCircle className="spin" size={15} />
                  ) : (
                    <Upload size={15} />
                  )}
                  {attachmentUploading ? 'Uploading…' : 'Add file'}
                  <input
                    type="file"
                    accept=".pdf,.png,.jpg,.jpeg,.txt,application/pdf,image/png,image/jpeg,text/plain"
                    disabled={attachmentUploading}
                    onChange={uploadAttachment}
                  />
                </label>
              </div>
              <p className="attachment-panel__help">
                PDF, PNG, JPEG, or TXT · maximum 5 MB
              </p>
              {attachments.length === 0 ? (
                <div className="attachment-empty">
                  <Paperclip size={18} />
                  <span>No files attached yet.</span>
                </div>
              ) : (
                <div className="attachment-list">
                  {attachments.map((attachment) => (
                    <article className="attachment-row" key={attachment.id}>
                      <span className="attachment-row__icon">
                        <FileText size={17} />
                      </span>
                      <div>
                        <strong>{attachment.filename}</strong>
                        <small>
                          {formatFileSize(attachment.sizeBytes)} ·{' '}
                          {attachment.uploaderName}
                        </small>
                      </div>
                      <button
                        type="button"
                        onClick={() => downloadAttachment(attachment)}
                        aria-label={`Download ${attachment.filename}`}
                      >
                        <Download size={17} />
                      </button>
                    </article>
                  ))}
                </div>
              )}
            </section>

            <section className="activity-panel">
              <div className="activity-panel__heading">
                <div><MessageSquare size={18} /><h4>Activity & comments</h4></div>
                <span>{activity.length}</span>
              </div>
              <div className="activity-timeline">
                {activity.length === 0 && <p className="activity-empty">No activity recorded yet.</p>}
                {activity.map((item) => (
                  <article className={`activity-entry ${item.internalNote ? 'activity-entry--internal' : ''}`} key={item.id}>
                    <span className="activity-entry__dot" />
                    <div><strong>{item.actorName}</strong><small>{formatLabel(item.type)} · {formatDate(item.createdAt)}</small><p>{item.message}</p></div>
                    {item.internalNote && <span className="internal-note-badge"><ShieldCheck size={12} /> Internal</span>}
                  </article>
                ))}
              </div>
              <form className="comment-form" onSubmit={submitComment}>
                <textarea value={commentMessage} onChange={(event) => setCommentMessage(event.target.value)} maxLength={2000} placeholder={isStaff ? 'Reply to the requester or add an internal note…' : 'Add a comment for the support team…'} aria-label="Ticket comment" />
                <div>
                  {isStaff && <label className="internal-note-toggle"><input type="checkbox" checked={internalNote} onChange={(event) => setInternalNote(event.target.checked)} /> Internal technician note</label>}
                  <button className="primary-button primary-button--compact" type="submit" disabled={commentSubmitting || !commentMessage.trim()}>{commentSubmitting ? <LoaderCircle className="spin" size={16} /> : <Send size={16} />} Send</button>
                </div>
              </form>
            </section>

            {confirmingCancel && (
              <div className="cancel-confirmation">
                <span>
                  <AlertTriangle size={20} />
                </span>
                <div>
                  <strong>Cancel this ticket?</strong>
                  <p>
                    It will remain in your history but cannot be edited.
                  </p>
                </div>
                <button
                  className="danger-button"
                  type="button"
                  disabled={cancelling}
                  onClick={confirmCancellation}
                >
                  {cancelling ? (
                    <LoaderCircle className="spin" size={17} />
                  ) : (
                    <Ban size={17} />
                  )}
                  Confirm
                </button>
              </div>
            )}

            <footer className="ticket-details__footer">
              {editable && (
                <>
                  <button
                    className="danger-text-button"
                    type="button"
                    onClick={() =>
                      setConfirmingCancel((current) => !current)
                    }
                  >
                    <Ban size={17} />
                    Cancel ticket
                  </button>

                  <button
                    className="primary-button primary-button--compact"
                    type="button"
                    onClick={() => setEditing(true)}
                  >
                    <Pencil size={17} />
                    Edit ticket
                  </button>
                </>
              )}

              {!editable && (
                <>
                  <button
                    className="secondary-button"
                    type="button"
                    onClick={onClose}
                  >
                    Close
                  </button>
                  {nextStaffAction && (
                    <button
                      className="primary-button primary-button--compact"
                      type="button"
                      disabled={staffActionRunning}
                      onClick={() => runStaffAction(nextStaffAction.action)}
                    >
                      {staffActionRunning ? (
                        <LoaderCircle className="spin" size={17} />
                      ) : (
                        <Sparkles size={17} />
                      )}
                      {nextStaffAction.label}
                    </button>
                  )}
                </>
              )}
            </footer>
          </div>
        )}

        {ticket && editing && (
          <form
            className="ticket-form"
            onSubmit={handleSubmit(saveChanges)}
            noValidate
          >
            {actionError && (
              <div className="form-alert" role="alert">
                {actionError}
              </div>
            )}

            <div className="form-field">
              <label htmlFor="edit-ticket-title">Title</label>
              <div className="input-shell">
                <Tag size={19} />
                <input
                  id="edit-ticket-title"
                  type="text"
                  {...register('title')}
                />
              </div>
              {errors.title && (
                <p className="field-error">{errors.title.message}</p>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="edit-ticket-description">Description</label>
              <div className="textarea-shell">
                <FileText size={19} />
                <textarea
                  id="edit-ticket-description"
                  rows={5}
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
                <label htmlFor="edit-ticket-category">Category</label>
                <div className="select-shell">
                  <Layers3 size={18} />
                  <select
                    id="edit-ticket-category"
                    {...register('category')}
                  >
                    <option value="HARDWARE">Hardware</option>
                    <option value="SOFTWARE">Software</option>
                    <option value="NETWORK">Network</option>
                    <option value="ACCESS">Access</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="edit-ticket-priority">Priority</label>
                <div className="select-shell">
                  <Tag size={18} />
                  <select
                    id="edit-ticket-priority"
                    {...register('priority')}
                  >
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                    <option value="URGENT">Urgent</option>
                  </select>
                </div>
              </div>
            </div>

            <footer className="ticket-modal__footer">
              <button
                className="secondary-button"
                type="button"
                onClick={() => {
                  setEditing(false)
                  reset({
                    title: ticket.title,
                    description: ticket.description,
                    category: ticket.category,
                    priority: ticket.priority,
                  })
                }}
              >
                Discard
              </button>

              <button
                className="primary-button primary-button--compact"
                type="submit"
                disabled={isSubmitting}
              >
                {isSubmitting ? (
                  <LoaderCircle className="spin" size={18} />
                ) : (
                  <Save size={18} />
                )}
                Save changes
              </button>
            </footer>
          </form>
        )}
      </section>
    </div>
  )
}
