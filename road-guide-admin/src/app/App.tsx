import { useEffect, useState } from 'react'
import {
  approveRegistrationRequest,
  loadDashboardData,
  loginAdmin,
  rejectRegistrationRequest,
  setAuthToken,
  setUserAssignments,
  setUserRole,
} from './lib/api'
import { AdminSidebar } from './components/AdminSidebar'
import { Dashboard } from './components/Dashboard'
import { SubscriptionManagement } from './components/SubscriptionManagement'
import { PaymentManagement } from './components/PaymentManagement'
import { POIManagement } from './components/POIManagement'
import { Image360Management } from './components/Image360Management'
import { CompanionFinderManagement } from './components/CompanionFinderManagement'
import { AdsManagement } from './components/AdsManagement'
import { GamesManagement } from './components/GamesManagement'
import {
  ChatModeration,
  OTAUpdates,
  AIGuide,
  Analytics,
  Notifications,
  ContentModeration,
  SearchManagement,
  MapContent,
  Settings,
} from './components/AllSections'
import { LoginPage } from './components/LoginPage'
import { RoadGuideBusinessSection } from './components/RoadGuideBusinessSection'
import { RoadGuideUsersSection } from './components/RoadGuideUsersSection'
import type { AdminUser, AuthUser, BusinessPoi, RegistrationRequest, Role } from './types'

export default function App() {
  const [identifier, setIdentifier] = useState('admin')
  const [password, setPassword] = useState('admin1234')
  const [token, setToken] = useState('')
  const [me, setMe] = useState<AuthUser | null>(null)
  const [activeSection, setActiveSection] = useState('dashboard')
  const [requests, setRequests] = useState<RegistrationRequest[]>([])
  const [users, setUsers] = useState<AdminUser[]>([])
  const [pois, setPois] = useState<BusinessPoi[]>([])
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [isDark, setIsDark] = useState(false)

  useEffect(() => {
    const savedTheme = localStorage.getItem('theme')
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    const shouldBeDark = savedTheme === 'dark' || (!savedTheme && prefersDark)
    setIsDark(shouldBeDark)
    if (shouldBeDark) {
      document.documentElement.classList.add('dark')
    }
  }, [])

  const toggleTheme = () => {
    const newIsDark = !isDark
    setIsDark(newIsDark)
    if (newIsDark) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }

  async function refreshDashboard(authToken?: string) {
    const activeToken = authToken ?? token
    if (!activeToken) return
    const data = await loadDashboardData(activeToken)
    setRequests(data.requests)
    setUsers(data.users)
    setPois(data.pois)
  }

  async function login() {
    try {
      setBusy(true)
      setMessage('')
      const auth = await loginAdmin(identifier, password)
      setToken(auth.token)
      setMe(auth.user)
      setAuthToken(auth.token)
      setMessage(`Welcome, ${auth.user.name}`)
      await refreshDashboard(auth.token)
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function approveRequest(request: RegistrationRequest) {
    try {
      setBusy(true)
      await approveRegistrationRequest(token, request.id, request.poiId)
      await refreshDashboard()
      setMessage(`Approved request for ${request.user.identifier}`)
      setActiveSection('business')
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function rejectRequest(request: RegistrationRequest) {
    try {
      setBusy(true)
      await rejectRegistrationRequest(token, request.id)
      await refreshDashboard()
      setMessage(`Rejected request for ${request.user.identifier}`)
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function updateRole(userId: string, role: Role) {
    try {
      setBusy(true)
      await setUserRole(token, userId, role)
      await refreshDashboard()
      setMessage('User role updated.')
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  async function updateAssignments(userId: string, poiIds: string[]) {
    try {
      setBusy(true)
      await setUserAssignments(token, userId, poiIds)
      await refreshDashboard()
      setMessage('POI assignments updated.')
    } catch (error) {
      setMessage((error as Error).message)
    } finally {
      setBusy(false)
    }
  }

  function logout() {
    setMe(null)
    setToken('')
    setAuthToken(null)
    setRequests([])
    setUsers([])
    setPois([])
    setMessage('')
    setActiveSection('dashboard')
  }

  if (!me) {
    return (
      <LoginPage
        identifier={identifier}
        password={password}
        busy={busy}
        message={message}
        onIdentifierChange={setIdentifier}
        onPasswordChange={setPassword}
        onSubmit={() => void login()}
      />
    )
  }

  const renderSection = () => {
    switch (activeSection) {
      case 'dashboard':
        return <Dashboard />
      case 'users':
        return (
          <RoadGuideUsersSection
            users={users}
            pois={pois}
            busy={busy}
            message={message}
            onRefresh={() => void refreshDashboard()}
            onSetRole={(userId, role) => void updateRole(userId, role)}
            onSetAssignments={(userId, poiIds) => void updateAssignments(userId, poiIds)}
          />
        )
      case 'subscriptions':
        return <SubscriptionManagement />
      case 'payments':
        return <PaymentManagement />
      case 'pois':
        return (
          <POIManagement
            pois={pois}
            pendingClaimCount={requests.length}
            busy={busy}
            token={token}
            onRefresh={() => void refreshDashboard()}
          />
        )
      case 'image360':
        return <Image360Management />
      case 'companion':
        return <CompanionFinderManagement />
      case 'ads':
        return <AdsManagement />
      case 'games':
        return <GamesManagement />
      case 'chat':
        return <ChatModeration />
      case 'ota':
        return <OTAUpdates />
      case 'business':
        return (
          <RoadGuideBusinessSection
            requests={requests}
            busy={busy}
            message={message}
            onRefresh={() => void refreshDashboard()}
            onApprove={(request) => void approveRequest(request)}
            onReject={(request) => void rejectRequest(request)}
          />
        )
      case 'ai-guide':
        return <AIGuide />
      case 'analytics':
        return <Analytics />
      case 'notifications':
        return <Notifications />
      case 'moderation':
        return <ContentModeration />
      case 'search':
        return <SearchManagement />
      case 'map-content':
        return <MapContent />
      case 'settings':
        return <Settings />
      default:
        return <Dashboard />
    }
  }

  return (
    <div className="h-screen overflow-hidden bg-background text-foreground">
      <AdminSidebar
        activeSection={activeSection}
        onSectionChange={setActiveSection}
        isDark={isDark}
        onThemeToggle={toggleTheme}
        me={me}
        onLogout={logout}
        pendingClaims={requests.length}
      />
      <main className="ml-64 h-screen overflow-y-auto overscroll-y-contain">
        {renderSection()}
      </main>
    </div>
  )
}
