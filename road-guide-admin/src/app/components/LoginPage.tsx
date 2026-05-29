import { Building2 } from 'lucide-react'

type LoginPageProps = {
  email: string
  password: string
  busy: boolean
  message: string
  onEmailChange: (value: string) => void
  onPasswordChange: (value: string) => void
  onSubmit: () => void
}

export function LoginPage({
  email,
  password,
  busy,
  message,
  onEmailChange,
  onPasswordChange,
  onSubmit,
}: LoginPageProps) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-6">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex w-14 h-14 rounded-xl bg-gradient-to-br from-chart-1 to-chart-3 items-center justify-center mb-4">
            <Building2 className="w-7 h-7 text-white" />
          </div>
          <h1 className="text-2xl mb-2">Road Guide Admin</h1>
          <p className="text-muted-foreground">Sign in to manage business registrations and users</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-6 space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm mb-2">
              Email
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => onEmailChange(event.target.value)}
              className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
              placeholder="admin@roadguide.local"
            />
          </div>
          <div>
            <label htmlFor="password" className="block text-sm mb-2">
              Password
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => onPasswordChange(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') onSubmit()
              }}
              className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          {message && (
            <p className="text-sm text-destructive">{message}</p>
          )}
          <button
            type="button"
            onClick={onSubmit}
            disabled={busy}
            className="w-full px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-60"
          >
            {busy ? 'Signing in...' : 'Login'}
          </button>
        </div>
      </div>
    </div>
  )
}
