import { AlertTriangle, CheckCircle2, Loader2 } from 'lucide-react'
import { useBackendStatus } from '../hooks/useGatewayStatus'

/** Live Road Guide API status shown above the dashboard header. */
export function GatewayStatusBanner() {
  const status = useBackendStatus()

  const label =
    status.state === 'loading'
      ? 'API: checking…'
      : status.state === 'ok'
        ? 'API: connected'
        : `API: unavailable (${status.message})`

  const tone =
    status.state === 'loading'
      ? 'text-muted-foreground'
      : status.state === 'ok'
        ? 'text-green-600 dark:text-green-400'
        : 'text-yellow-600 dark:text-yellow-400'

  const Icon =
    status.state === 'loading' ? Loader2 : status.state === 'ok' ? CheckCircle2 : AlertTriangle

  return (
    <div className="flex flex-wrap items-center gap-2 text-xs">
      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-card border border-border">
        <Icon className={`w-4 h-4 ${tone} ${status.state === 'loading' ? 'animate-spin' : ''}`} />
        <span className="text-muted-foreground">{label}</span>
      </span>
    </div>
  )
}
