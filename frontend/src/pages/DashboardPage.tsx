import {
  AlertTriangle,
  Bell,
  CheckCircle2,
  ChevronRight,
  Clock3,
  Headphones,
  Inbox,
  LayoutDashboard,
  ListTodo,
  LoaderCircle,
  LockKeyhole,
  LogOut,
  Plus,
  Search,
  ShieldCheck,
  TicketCheck,
  UserCog,
  UsersRound,
  Wrench,
} from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { getAdminUsers, updateAdminUser } from '../api/adminApi'
import { getNotifications, markNotificationRead } from '../api/notificationApi'
import { ApiRequestError } from '../api/apiClient'
import {
  clearAccessToken,
  getAccessToken,
  getCurrentUser,
} from '../api/authApi'
import { getStaffTickets, getTickets } from '../api/ticketApi'
import { CreateTicketModal } from '../components/CreateTicketModal'
import { ProfileModal } from '../components/ProfileModal'
import { TicketDetailsModal } from '../components/TicketDetailsModal'
import type { AdminUser, CurrentUser, Role } from '../types/auth'
import type {
  TicketDetails,
  TicketResponse,
  TicketStatus,
  TicketSummary,
  NotificationItem,
} from '../types/ticket'

type Workspace = 'employee' | 'technician' | 'administrator'
type View = 'overview' | 'tickets' | 'users'

function formatLabel(value: string): string {
  return value
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/\b\w/g, (character) => character.toUpperCase())
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(value))
}

function resolveWorkspace(user: CurrentUser): Workspace {
  if (user.roles.includes('ADMINISTRATOR')) return 'administrator'
  if (user.roles.includes('TECHNICIAN')) return 'technician'
  return 'employee'
}

function primaryRole(roles: Role[]): Role {
  if (roles.includes('ADMINISTRATOR')) return 'ADMINISTRATOR'
  if (roles.includes('TECHNICIAN')) return 'TECHNICIAN'
  return 'EMPLOYEE'
}

export function DashboardPage() {
  const navigate = useNavigate()
  const [accessToken] = useState(() => getAccessToken() ?? '')
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [tickets, setTickets] = useState<TicketSummary[]>([])
  const [adminUsers, setAdminUsers] = useState<AdminUser[]>([])
  const [notifications, setNotifications] = useState<NotificationItem[]>([])
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const [activeView, setActiveView] = useState<View>('overview')
  const [searchQuery, setSearchQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [ticketModalOpen, setTicketModalOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const [selectedTicketId, setSelectedTicketId] = useState<string | null>(null)
  const [updatingUserId, setUpdatingUserId] = useState<string | null>(null)

  useEffect(() => {
    if (!accessToken) {
      navigate('/login', { replace: true })
      return
    }

    getCurrentUser(accessToken)
      .then(async (currentUser) => {
        const workspace = resolveWorkspace(currentUser)
        const ticketRequest =
          workspace === 'employee'
            ? getTickets(accessToken)
            : getStaffTickets(accessToken)
        const userRequest =
          workspace === 'administrator'
            ? getAdminUsers(accessToken)
            : Promise.resolve([])
        const [loadedTickets, loadedUsers, loadedNotifications] = await Promise.all([
          ticketRequest,
          userRequest,
          getNotifications(accessToken),
        ])
        setUser(currentUser)
        setTickets(loadedTickets)
        setAdminUsers(loadedUsers)
        setNotifications(loadedNotifications)
      })
      .catch((error: unknown) => {
        if (error instanceof ApiRequestError && error.status === 401) {
          clearAccessToken()
          navigate('/login', { replace: true })
          return
        }
        setLoadError('We could not load your workspace. Check the backend and try again.')
      })
      .finally(() => setLoading(false))
  }, [accessToken, navigate])

  const filteredTickets = useMemo(() => {
    const query = searchQuery.trim().toLowerCase()
    if (!query) return tickets
    return tickets.filter((ticket) =>
      [
        ticket.referenceCode,
        ticket.title,
        ticket.category,
        ticket.priority,
        ticket.status,
      ].some((value) => value.toLowerCase().includes(query)),
    )
  }, [searchQuery, tickets])

  if (loading) {
    return (
      <main className="loading-screen">
        <span className="brand__mark"><Headphones size={24} /></span>
        <LoaderCircle className="spin" size={24} />
        <p>Preparing your workspace…</p>
      </main>
    )
  }

  if (loadError || !user) {
    return (
      <main className="loading-screen">
        <span className="error-state__icon"><AlertTriangle size={24} /></span>
        <strong>Workspace unavailable</strong>
        <p>{loadError ?? 'Your workspace could not be loaded.'}</p>
        <button className="secondary-button" type="button" onClick={() => window.location.reload()}>
          Try again
        </button>
      </main>
    )
  }

  const workspace = resolveWorkspace(user)
  const isStaff = workspace !== 'employee'
  const initials = user.fullName
    .split(' ')
    .map((part) => part[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()
  const firstName = user.fullName.split(' ')[0]
  const countStatuses = (...statuses: TicketStatus[]) =>
    tickets.filter((ticket) => statuses.includes(ticket.status)).length

  const workspaceCopy = {
    employee: {
      eyebrow: 'Employee workspace',
      title: `Good to see you, ${firstName}.`,
      subtitle: 'Create requests and follow every update from one place.',
    },
    technician: {
      eyebrow: 'Technician operations',
      title: `Ready for the queue, ${firstName}?`,
      subtitle: 'Claim incoming work and move each request toward resolution.',
    },
    administrator: {
      eyebrow: 'Administration control center',
      title: `Welcome back, ${firstName}.`,
      subtitle: 'Manage access, monitor service health, and oversee every request.',
    },
  }[workspace]

  const statistics = workspace === 'administrator'
    ? [
        { label: 'Total users', value: adminUsers.length, detail: 'Registered accounts', icon: UsersRound, tone: 'blue' },
        { label: 'Active users', value: adminUsers.filter((item) => item.enabled && !item.accountLocked).length, detail: 'Can currently sign in', icon: ShieldCheck, tone: 'green' },
        { label: 'Locked users', value: adminUsers.filter((item) => item.accountLocked).length, detail: 'Require administrator review', icon: LockKeyhole, tone: 'violet' },
      ]
    : [
        { label: workspace === 'employee' ? 'Open tickets' : 'Unassigned', value: workspace === 'employee' ? countStatuses('OPEN', 'ASSIGNED') : tickets.filter((ticket) => ticket.status === 'OPEN').length, detail: `${tickets.length} total requests`, icon: TicketCheck, tone: 'blue' },
        { label: 'In progress', value: countStatuses('IN_PROGRESS'), detail: 'Currently being handled', icon: Clock3, tone: 'violet' },
        { label: 'Resolved', value: countStatuses('RESOLVED', 'CLOSED'), detail: 'Successfully completed', icon: CheckCircle2, tone: 'green' },
      ]

  const categoryCounts = ['HARDWARE', 'SOFTWARE', 'NETWORK', 'ACCESS', 'OTHER'].map((category) => ({
    category,
    count: tickets.filter((ticket) => ticket.category === category).length,
  }))
  const maxCategoryCount = Math.max(1, ...categoryCounts.map((item) => item.count))
  const unreadNotifications = notifications.filter((item) => !item.read).length

  const openNotification = async (notification: NotificationItem) => {
    if (!notification.read) {
      try {
        const changed = await markNotificationRead(accessToken, notification.id)
        setNotifications((current) => current.map((item) => item.id === changed.id ? changed : item))
      } catch {
        setActionError('The notification could not be marked as read.')
      }
    }
    if (notification.ticketId) setSelectedTicketId(notification.ticketId)
    setNotificationsOpen(false)
  }

  const signOut = () => {
    clearAccessToken()
    navigate('/login', { replace: true })
  }

  const handleTicketCreated = (ticket: TicketResponse) => {
    setTickets((current) => [{
      id: ticket.id,
      referenceCode: ticket.referenceCode,
      title: ticket.title,
      category: ticket.category,
      priority: ticket.priority,
      status: ticket.status,
      createdAt: ticket.createdAt,
      updatedAt: ticket.createdAt,
    }, ...current])
  }

  const handleTicketChanged = (ticket: TicketDetails) => {
    setTickets((current) => current.map((item) =>
      item.id === ticket.id
        ? { ...item, title: ticket.title, category: ticket.category, priority: ticket.priority, status: ticket.status, updatedAt: ticket.updatedAt }
        : item,
    ))
  }

  const updateUser = async (
    target: AdminUser,
    update: { role?: Role; enabled?: boolean; accountLocked?: boolean },
  ) => {
    setUpdatingUserId(target.id)
    setActionError(null)
    try {
      const changed = await updateAdminUser(accessToken, target.id, {
        role: update.role ?? primaryRole(target.roles),
        enabled: update.enabled ?? target.enabled,
        accountLocked: update.accountLocked ?? target.accountLocked,
      })
      setAdminUsers((current) => current.map((item) => item.id === changed.id ? changed : item))
    } catch (error) {
      setActionError(error instanceof ApiRequestError ? error.message : 'The account could not be updated.')
    } finally {
      setUpdatingUserId(null)
    }
  }

  return (
    <div className={`dashboard-shell dashboard-shell--${workspace}`}>
      <aside className="sidebar">
        <div className="brand sidebar__brand">
          <span className="brand__mark"><Headphones size={22} /></span>
          <span className="brand__name">HelpHub</span>
        </div>

        <nav className="sidebar__nav" aria-label="Workspace navigation">
          <button className={`nav-item ${activeView === 'overview' ? 'nav-item--active' : ''}`} type="button" onClick={() => setActiveView('overview')}>
            <LayoutDashboard size={19} /> Overview
          </button>
          <button className={`nav-item ${activeView === 'tickets' ? 'nav-item--active' : ''}`} type="button" onClick={() => setActiveView('tickets')}>
            {workspace === 'technician' ? <Wrench size={19} /> : <ListTodo size={19} />}
            {workspace === 'employee' ? 'My tickets' : workspace === 'technician' ? 'Operations queue' : 'Ticket oversight'}
            <span className="nav-item__count">{tickets.length}</span>
          </button>
          {workspace === 'administrator' && (
            <button className={`nav-item ${activeView === 'users' ? 'nav-item--active' : ''}`} type="button" onClick={() => setActiveView('users')}>
              <UserCog size={19} /> User management
              <span className="nav-item__count">{adminUsers.length}</span>
            </button>
          )}
        </nav>

        <div className="sidebar__workspace-badge">
          {workspace === 'employee' ? <TicketCheck size={20} /> : workspace === 'technician' ? <Wrench size={20} /> : <ShieldCheck size={20} />}
          <span><small>Current workspace</small><strong>{formatLabel(workspace)}</strong></span>
        </div>
        <button className="sign-out-button" type="button" onClick={signOut}><LogOut size={18} /> Sign out</button>
      </aside>

      <main className="dashboard-main">
        <header className="topbar">
          <div className="topbar__search">
            <Search size={19} />
            <input type="search" value={searchQuery} onChange={(event) => setSearchQuery(event.target.value)} placeholder="Search tickets" aria-label="Search tickets" />
          </div>
          <div className="topbar__actions">
            <div className="notification-center">
              <button className="icon-button" type="button" onClick={() => setNotificationsOpen((current) => !current)} aria-label="Notifications">
                <Bell size={19} />
                {unreadNotifications > 0 && <span className="notification-count">{unreadNotifications}</span>}
              </button>
              {notificationsOpen && (
                <div className="notification-menu">
                  <header><strong>Notifications</strong><span>{unreadNotifications} unread</span></header>
                  <div>
                    {notifications.length === 0 && <p className="notification-menu__empty">You are all caught up.</p>}
                    {notifications.slice(0, 8).map((notification) => (
                      <button className={notification.read ? '' : 'notification-item--unread'} type="button" key={notification.id} onClick={() => openNotification(notification)}>
                        <span>{notification.message}</span><small>{formatDate(notification.createdAt)}</small>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
            <button className="user-menu user-menu--button" type="button" onClick={() => setProfileOpen(true)}>
              <span className="user-avatar">{initials}</span>
              <span className="user-menu__details"><strong>{user.fullName}</strong><small>{user.roles.map(formatLabel).join(' · ')}</small></span>
              <ChevronRight size={18} />
            </button>
          </div>
        </header>

        <div className="dashboard-content">
          <section className="welcome-row">
            <div>
              <p className="dashboard-eyebrow">{workspaceCopy.eyebrow}</p>
              <h1>{workspaceCopy.title}</h1>
              <p>{workspaceCopy.subtitle}</p>
            </div>
            {workspace === 'employee' && (
              <button className="primary-button primary-button--compact" type="button" onClick={() => setTicketModalOpen(true)}><Plus size={19} /> Create ticket</button>
            )}
          </section>

          {activeView === 'overview' && (
            <><section className="stats-grid" aria-label="Workspace statistics">
              {statistics.map(({ label, value, detail, icon: Icon, tone }) => (
                <article className="stat-card" key={label}>
                  <span className={`stat-card__icon stat-card__icon--${tone}`}><Icon size={21} /></span>
                  <div className="stat-card__value">{String(value).padStart(2, '0')}</div>
                  <h2>{label}</h2><p>{detail}</p>
                </article>
              ))}
            </section>
            <section className="analytics-panel" aria-label="Ticket categories">
              <div><span className="dashboard-eyebrow">Live distribution</span><h2>Requests by category</h2><p>Current workload across service areas.</p></div>
              <div className="category-bars">
                {categoryCounts.map((item) => (
                  <div className="category-bar" key={item.category}>
                    <span>{formatLabel(item.category)}</span><div><i style={{ width: `${(item.count / maxCategoryCount) * 100}%` }} /></div><strong>{item.count}</strong>
                  </div>
                ))}
              </div>
            </section></>
          )}

          {actionError && <div className="form-alert workspace-alert" role="alert">{actionError}</div>}

          {workspace === 'administrator' && activeView === 'users' ? (
            <section className="admin-panel">
              <header className="ticket-panel__header">
                <div><h2>User management</h2><p>Assign access levels and protect user accounts.</p></div>
                <span className="ticket-result-count">{adminUsers.length} users</span>
              </header>
              <div className="admin-user-list">
                {adminUsers.map((account) => (
                  <article className="admin-user-row" key={account.id}>
                    <span className="user-avatar">{account.fullName.split(' ').map((part) => part[0]).slice(0, 2).join('').toUpperCase()}</span>
                    <div className="admin-user-row__identity"><strong>{account.fullName}</strong><span>{account.email}</span></div>
                    <select value={primaryRole(account.roles)} disabled={account.id === user.id || updatingUserId === account.id} onChange={(event) => updateUser(account, { role: event.target.value as Role })} aria-label={`Role for ${account.fullName}`}>
                      <option value="EMPLOYEE">Employee</option>
                      <option value="TECHNICIAN">Technician</option>
                      <option value="ADMINISTRATOR">Administrator</option>
                    </select>
                    <span className={`account-state ${account.enabled && !account.accountLocked ? 'account-state--active' : 'account-state--restricted'}`}>{account.accountLocked ? 'Locked' : account.enabled ? 'Active' : 'Disabled'}</span>
                    <div className="admin-user-row__actions">
                      <button type="button" disabled={account.id === user.id || updatingUserId === account.id} onClick={() => updateUser(account, { accountLocked: !account.accountLocked })}>{account.accountLocked ? 'Unlock' : 'Lock'}</button>
                      <button type="button" disabled={account.id === user.id || updatingUserId === account.id} onClick={() => updateUser(account, { enabled: !account.enabled })}>{account.enabled ? 'Disable' : 'Enable'}</button>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          ) : (
            <TicketPanel
              tickets={filteredTickets}
              searchQuery={searchQuery}
              workspace={workspace}
              onCreate={() => setTicketModalOpen(true)}
              onOpen={setSelectedTicketId}
            />
          )}
        </div>
      </main>

      <CreateTicketModal open={ticketModalOpen} accessToken={accessToken} onClose={() => setTicketModalOpen(false)} onCreated={handleTicketCreated} />
      {selectedTicketId && <TicketDetailsModal key={selectedTicketId} ticketId={selectedTicketId} accessToken={accessToken} onClose={() => setSelectedTicketId(null)} onChanged={handleTicketChanged} isStaff={isStaff} currentUserId={user.id} />}
      {profileOpen && <ProfileModal user={user} onClose={() => setProfileOpen(false)} />}
    </div>
  )
}

function TicketPanel({ tickets, searchQuery, workspace, onCreate, onOpen }: {
  tickets: TicketSummary[]
  searchQuery: string
  workspace: Workspace
  onCreate: () => void
  onOpen: (id: string) => void
}) {
  const title = workspace === 'employee' ? 'Your tickets' : workspace === 'technician' ? 'Operations queue' : 'Ticket oversight'
  const description = workspace === 'employee' ? 'Requests visible only to your account.' : 'Organization-wide requests ordered by latest activity.'
  return (
    <section className="ticket-panel" id="tickets">
      <header className="ticket-panel__header"><div><h2>{title}</h2><p>{description}</p></div><span className="ticket-result-count">{tickets.length} {tickets.length === 1 ? 'ticket' : 'tickets'}</span></header>
      {tickets.length === 0 ? (
        <div className="empty-tickets">
          <span className="empty-tickets__icon"><Inbox size={25} /></span>
          <h3>{searchQuery ? 'No matching tickets' : workspace === 'employee' ? 'No tickets created yet' : 'The queue is clear'}</h3>
          <p>{searchQuery ? 'Try another search term.' : workspace === 'employee' ? 'Create your first request and track it here.' : 'New support requests will appear here.'}</p>
          {!searchQuery && workspace === 'employee' && <button className="text-button" type="button" onClick={onCreate}>Create your first ticket <ChevronRight size={17} /></button>}
        </div>
      ) : (
        <div className="ticket-table">
          {tickets.map((ticket) => (
            <article className="ticket-row" key={ticket.id}>
              <div className="ticket-row__identity"><span className="ticket-row__icon"><TicketCheck size={19} /></span><div><strong>{ticket.title}</strong><span>{ticket.referenceCode} · {formatLabel(ticket.category)} · {formatDate(ticket.createdAt)}</span></div></div>
              <span className={`priority priority--${ticket.priority.toLowerCase()}`}>{formatLabel(ticket.priority)}</span>
              <span className={`status status--${ticket.status.toLowerCase().replaceAll('_', '-')}`}>{formatLabel(ticket.status)}</span>
              <button className="row-action" type="button" onClick={() => onOpen(ticket.id)} aria-label={`Open ${ticket.referenceCode}`}><ChevronRight size={19} /></button>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
