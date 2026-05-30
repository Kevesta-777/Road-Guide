export type Role = 'visitor' | 'business' | 'admin'

export type AuthUser = {
  id: string
  identifier: string
  name: string
  role: Role
}

export type BusinessPoi = {
  id: string
  name: string
  address: string
}

export type RegistrationRequest = {
  id: string
  userId: string
  poiId: string
  status: string
  message: string
  adminNote: string
  user: { identifier: string; name: string }
  poi: { name: string }
}

export type AdminUser = AuthUser & {
  assignedPoiIds: string[]
}

export type PanoramaImage = {
  id: string
  title: string
  poiName: string
  poiId: string
  uploadedBy: string
  userId: string
  status: 'Pending' | 'Approved' | 'Rejected'
  uploadedAt: string
  views: number
  location: string
  thumbnail: string
  imageUrl: string
  fileSizeBytes: number
  rejectionReason?: string
}

export type AdminSection = 'dashboard' | 'requests' | 'users'

export type DashboardData = {
  requests: RegistrationRequest[]
  users: AdminUser[]
  pois: BusinessPoi[]
}
