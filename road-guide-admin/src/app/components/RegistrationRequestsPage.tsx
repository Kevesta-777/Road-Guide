import { CheckCircle, RefreshCw, Search, XCircle } from 'lucide-react'
import { useMemo, useState } from 'react'
import type { RegistrationRequest } from '../types'

type RegistrationRequestsPageProps = {
  requests: RegistrationRequest[]
  busy: boolean
  message: string
  onRefresh: () => void
  onApprove: (request: RegistrationRequest) => void
  onReject: (request: RegistrationRequest) => void
}

export function RegistrationRequestsPage({
  requests,
  busy,
  message,
  onRefresh,
  onApprove,
  onReject,
}: RegistrationRequestsPageProps) {
  const [searchTerm, setSearchTerm] = useState('')

  const filtered = useMemo(() => {
    const query = searchTerm.trim().toLowerCase()
    if (!query) return requests
    return requests.filter((request) => {
      return (
        request.poi.name.toLowerCase().includes(query) ||
        request.user.name.toLowerCase().includes(query) ||
        request.user.identifier.toLowerCase().includes(query) ||
        request.message.toLowerCase().includes(query)
      )
    })
  }, [requests, searchTerm])

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl mb-2">Registration Requests</h2>
          <p className="text-muted-foreground">Review and approve business registration requests</p>
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
        <div className="bg-green-50 dark:bg-green-900/10 border border-green-200 dark:border-green-800 rounded-lg p-4 text-sm text-green-800 dark:text-green-300">
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
              placeholder="Search by business, user, or message..."
              className="w-full pl-10 pr-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
        </div>

        {filtered.length === 0 ? (
          <p className="text-sm text-muted-foreground py-8 text-center">No pending requests.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-3 px-4 text-sm text-muted-foreground">Business POI</th>
                  <th className="text-left py-3 px-4 text-sm text-muted-foreground">Requested By</th>
                  <th className="text-left py-3 px-4 text-sm text-muted-foreground">Message</th>
                  <th className="text-right py-3 px-4 text-sm text-muted-foreground">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((request) => (
                  <tr key={request.id} className="border-b border-border hover:bg-accent/50 transition-colors">
                    <td className="py-4 px-4">
                      <div className="font-medium">{request.poi.name}</div>
                    </td>
                    <td className="py-4 px-4">
                      <div className="font-medium">{request.user.name}</div>
                      <div className="text-sm text-muted-foreground">{request.user.identifier}</div>
                    </td>
                    <td className="py-4 px-4 text-sm text-muted-foreground max-w-xs">
                      {request.message || '—'}
                    </td>
                    <td className="py-4 px-4">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          type="button"
                          disabled={busy}
                          onClick={() => onApprove(request)}
                          className="inline-flex items-center gap-1 px-3 py-1.5 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm disabled:opacity-60"
                        >
                          <CheckCircle className="w-4 h-4" />
                          Approve
                        </button>
                        <button
                          type="button"
                          disabled={busy}
                          onClick={() => onReject(request)}
                          className="inline-flex items-center gap-1 px-3 py-1.5 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm disabled:opacity-60"
                        >
                          <XCircle className="w-4 h-4" />
                          Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
