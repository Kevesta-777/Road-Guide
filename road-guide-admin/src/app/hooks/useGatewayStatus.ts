import { useEffect, useState } from 'react'

export type ServiceState = 'loading' | 'ok' | 'down'

export type BackendStatus = {
  state: ServiceState
  message: string
}

const INITIAL: BackendStatus = { state: 'loading', message: '' }

/**
 * Polls the Road Guide API health endpoint (`GET /healthz`).
 * In dev, Vite proxies `/healthz` to the Go backend (see vite.config.ts).
 */
export function useBackendStatus(refreshMs = 15_000): BackendStatus {
  const [status, setStatus] = useState<BackendStatus>(INITIAL)

  useEffect(() => {
    let cancelled = false

    const tick = async () => {
      try {
        const response = await fetch('/healthz', {
          headers: { Accept: 'application/json' },
        })
        const contentType = response.headers.get('content-type') ?? ''
        if (!contentType.includes('application/json')) {
          throw new Error('unexpected response')
        }
        const body = (await response.json()) as { status?: string }
        if (!cancelled) {
          const ok = response.ok && body.status === 'ok'
          setStatus({
            state: ok ? 'ok' : 'down',
            message: ok ? 'connected' : `HTTP ${response.status}`,
          })
        }
      } catch (error) {
        if (!cancelled) {
          setStatus({
            state: 'down',
            message: error instanceof Error ? error.message : 'unreachable',
          })
        }
      }
    }

    tick()
    const id = window.setInterval(tick, refreshMs)
    return () => {
      cancelled = true
      window.clearInterval(id)
    }
  }, [refreshMs])

  return status
}
