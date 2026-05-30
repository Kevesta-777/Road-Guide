import type { AdminUser, AuthUser, BusinessPoi, RegistrationRequest } from '../types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1'
export const ADMIN_API_BASE = '/api/v1/admin'

let authToken: string | null = null

export function setAuthToken(token: string | null) {
  authToken = token
}

export function getAuthToken() {
  return authToken
}

export class ApiError extends Error {
  status: number
  constructor(message: string, status: number) {
    super(message)
    this.status = status
    this.name = 'ApiError'
  }
}

async function adminRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path.startsWith('http') ? path : `${ADMIN_API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
      ...(init?.headers ?? {}),
    },
    ...init,
  })
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new ApiError(text || `HTTP ${res.status}`, res.status)
  }
  if (res.status === 204) return undefined as unknown as T
  return (await res.json()) as T
}

export function apiGet<T>(path: string): Promise<T> {
  return adminRequest<T>(path)
}

export function apiPost<T>(path: string, body: unknown): Promise<T> {
  return adminRequest<T>(path, { method: 'POST', body: JSON.stringify(body) })
}

export function apiPatch<T>(path: string, body: unknown): Promise<T> {
  return adminRequest<T>(path, { method: 'PATCH', body: JSON.stringify(body) })
}

export function apiDelete<T>(path: string): Promise<T> {
  return adminRequest<T>(path, { method: 'DELETE' })
}

export async function api<T>(path: string, init?: RequestInit, token?: string): Promise<T> {
  const activeToken = token ?? authToken ?? undefined
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(activeToken ? { Authorization: `Bearer ${activeToken}` } : {}),
      ...(init?.headers ?? {}),
    },
  })
  const data = await response.json()
  if (!response.ok) {
    throw new Error((data as { error?: string }).error ?? 'Request failed')
  }
  return data as T
}

export async function loginAdmin(identifier: string, password: string) {
  const auth = await api<{ token: string; user: AuthUser }>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ identifier, password }),
  })
  if (auth.user.role !== 'admin') {
    throw new Error('Only admin users can access this panel.')
  }
  setAuthToken(auth.token)
  return auth
}

export async function loadDashboardData(token?: string) {
  const activeToken = token ?? authToken
  if (!activeToken) {
    return { requests: [], users: [], pois: [] }
  }
  const [reqResult, userResult, poiResult] = await Promise.all([
    api<{ requests: RegistrationRequest[] }>(
      '/admin/registration-requests?status=pending',
      undefined,
      activeToken,
    ),
    api<{ users: AdminUser[] }>('/admin/users', undefined, activeToken),
    api<{ pois: BusinessPoi[] }>('/admin/business-pois', undefined, activeToken),
  ])
  return {
    requests: reqResult.requests,
    users: userResult.users,
    pois: poiResult.pois,
  }
}

export async function approveRegistrationRequest(
  token: string,
  requestId: string,
  poiId: string,
) {
  await api(
    `/admin/registration-requests/${requestId}/approve`,
    {
      method: 'POST',
      body: JSON.stringify({ assignPoiIds: [poiId] }),
    },
    token,
  )
}

export async function rejectRegistrationRequest(authTokenArg: string, requestId: string) {
  await api(
    `/admin/registration-requests/${requestId}/reject`,
    {
      method: 'POST',
      body: JSON.stringify({ adminNote: 'Rejected by admin.' }),
    },
    authTokenArg,
  )
}

export async function setUserRole(authTokenArg: string, userId: string, role: AuthUser['role']) {
  await api(
    `/admin/users/${userId}/role`,
    {
      method: 'PUT',
      body: JSON.stringify({ role }),
    },
    authTokenArg,
  )
}

export async function setUserAssignments(authTokenArg: string, userId: string, poiIds: string[]) {
  await api(
    `/admin/users/${userId}/assignments`,
    {
      method: 'PUT',
      body: JSON.stringify({ poiIds }),
    },
    authTokenArg,
  )
}

export async function approvePanorama(mediaId: string) {
  await apiPost(`/panoramas/${mediaId}/approve`, {})
}

export async function rejectPanorama(mediaId: string, adminNote: string) {
  await apiPost(`/panoramas/${mediaId}/reject`, { adminNote })
}
