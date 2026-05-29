import { Building2, ClipboardList, MapPin, Users } from 'lucide-react'
import type { DashboardData } from '../types'

type DashboardPageProps = {
  data: DashboardData
  onRefresh: () => void
  busy: boolean
}

export function DashboardPage({ data, onRefresh, busy }: DashboardPageProps) {
  const businessUsers = data.users.filter((user) => user.role === 'business').length
  const pendingRequests = data.requests.length

  const stats = [
    {
      label: 'Pending Requests',
      value: pendingRequests.toString(),
      hint: 'Awaiting admin review',
      icon: ClipboardList,
      color: 'text-chart-1',
    },
    {
      label: 'Total Users',
      value: data.users.length.toString(),
      hint: `${businessUsers} business accounts`,
      icon: Users,
      color: 'text-chart-2',
    },
    {
      label: 'Business POIs',
      value: data.pois.length.toString(),
      hint: 'Registered places',
      icon: MapPin,
      color: 'text-chart-3',
    },
    {
      label: 'Admin Operators',
      value: data.users.filter((user) => user.role === 'admin').length.toString(),
      hint: 'Active admin accounts',
      icon: Building2,
      color: 'text-chart-4',
    },
  ]

  return (
    <div className="p-8 space-y-8">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl mb-2">Dashboard</h2>
          <p className="text-muted-foreground">Overview of business registration workflow and user management</p>
        </div>
        <button
          type="button"
          onClick={onRefresh}
          disabled={busy}
          className="px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors disabled:opacity-60"
        >
          Refresh
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
        {stats.map((stat) => {
          const Icon = stat.icon
          return (
            <div key={stat.label} className="bg-card border border-border rounded-lg p-5">
              <div className="flex items-center justify-between mb-2">
                <h3 className="text-sm">{stat.label}</h3>
                <Icon className={`w-5 h-5 ${stat.color}`} />
              </div>
              <div className="text-2xl mb-1">{stat.value}</div>
              <p className="text-xs text-muted-foreground">{stat.hint}</p>
            </div>
          )
        })}
      </div>

      <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
        <h4 className="text-blue-900 dark:text-blue-100 mb-1">Business registration workflow</h4>
        <p className="text-sm text-blue-700 dark:text-blue-300">
          Review pending requests, approve business users, assign POIs, and manage roles from the sidebar sections.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-4">Recent Registration Requests</h3>
          {data.requests.length === 0 ? (
            <p className="text-sm text-muted-foreground">No pending registration requests.</p>
          ) : (
            <div className="space-y-3">
              {data.requests.slice(0, 5).map((request) => (
                <div key={request.id} className="border border-border rounded-lg p-4">
                  <p className="font-medium">{request.poi.name}</p>
                  <p className="text-sm text-muted-foreground">
                    {request.user.name} ({request.user.email})
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-4">Business Users</h3>
          {businessUsers === 0 ? (
            <p className="text-sm text-muted-foreground">No business users yet.</p>
          ) : (
            <div className="space-y-3">
              {data.users
                .filter((user) => user.role === 'business')
                .slice(0, 5)
                .map((user) => (
                  <div key={user.id} className="border border-border rounded-lg p-4">
                    <p className="font-medium">{user.name}</p>
                    <p className="text-sm text-muted-foreground">{user.email}</p>
                    <p className="text-xs text-muted-foreground mt-1">
                      {user.assignedPoiIds.length} assigned POI(s)
                    </p>
                  </div>
                ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
