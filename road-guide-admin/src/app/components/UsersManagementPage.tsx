import { RefreshCw, Search } from 'lucide-react'
import { useMemo, useState } from 'react'
import type { AdminUser, BusinessPoi, Role } from '../types'
import { roleTone, StatusBadge } from './StatusBadge'

type UsersManagementPageProps = {
  users: AdminUser[]
  pois: BusinessPoi[]
  busy: boolean
  message: string
  onRefresh: () => void
  onSetRole: (userId: string, role: Role) => void
  onSetAssignments: (userId: string, poiIds: string[]) => void
}

export function UsersManagementPage({
  users,
  pois,
  busy,
  message,
  onRefresh,
  onSetRole,
  onSetAssignments,
}: UsersManagementPageProps) {
  const [searchTerm, setSearchTerm] = useState('')
  const poiById = useMemo(() => new Map(pois.map((poi) => [poi.id, poi])), [pois])

  const filtered = useMemo(() => {
    const query = searchTerm.trim().toLowerCase()
    if (!query) return users
    return users.filter((user) => {
      return (
        user.name.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query) ||
        user.role.toLowerCase().includes(query)
      )
    })
  }, [users, searchTerm])

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl mb-2">Users & Assignments</h2>
          <p className="text-muted-foreground">Manage user roles and assign business POIs</p>
        </div>
        <button
          type="button"
          onClick={onRefresh}
          disabled={busy}
          className="inline-flex items-center gap-2 px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors disabled:opacity-60"
        >
          <RefreshCw className={`w-4 h-4 ${busy ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {message && (
        <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4 text-sm text-blue-800 dark:text-blue-300">
          {message}
        </div>
      )}

      <div className="bg-card border border-border rounded-lg p-6">
        <div className="flex flex-col sm:flex-row gap-4 mb-6">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <input
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="Search users by name, email, or role..."
              className="w-full pl-10 pr-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left py-3 px-4 text-sm text-muted-foreground">User</th>
                <th className="text-left py-3 px-4 text-sm text-muted-foreground">Role</th>
                <th className="text-left py-3 px-4 text-sm text-muted-foreground">Assigned POIs</th>
                <th className="text-left py-3 px-4 text-sm text-muted-foreground min-w-[220px]">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((user) => (
                <tr key={user.id} className="border-b border-border hover:bg-accent/50 transition-colors align-top">
                  <td className="py-4 px-4">
                    <div className="font-medium">{user.name}</div>
                    <div className="text-sm text-muted-foreground">{user.email}</div>
                  </td>
                  <td className="py-4 px-4">
                    <StatusBadge label={user.role} tone={roleTone(user.role)} />
                  </td>
                  <td className="py-4 px-4 text-sm text-muted-foreground">
                    {user.assignedPoiIds.length > 0
                      ? user.assignedPoiIds
                          .map((id) => poiById.get(id)?.name ?? id)
                          .join(', ')
                      : 'none'}
                  </td>
                  <td className="py-4 px-4">
                    <div className="space-y-3 min-w-[220px]">
                      <select
                        value={user.role}
                        disabled={busy}
                        onChange={(event) => onSetRole(user.id, event.target.value as Role)}
                        className="w-full px-3 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                      >
                        <option value="visitor">visitor</option>
                        <option value="business">business</option>
                        <option value="admin">admin</option>
                      </select>
                      <select
                        multiple
                        value={user.assignedPoiIds}
                        disabled={busy}
                        onChange={(event) => {
                          const selected = Array.from(event.target.selectedOptions).map(
                            (option) => option.value,
                          )
                          onSetAssignments(user.id, selected)
                        }}
                        className="w-full min-h-28 px-3 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                      >
                        {pois.map((poi) => (
                          <option key={poi.id} value={poi.id}>
                            {poi.name} ({poi.address})
                          </option>
                        ))}
                      </select>
                      <p className="text-xs text-muted-foreground">Hold Ctrl/Cmd to select multiple POIs</p>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
