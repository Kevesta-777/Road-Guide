import { useState } from 'react'
import type { AdminUser, BusinessPoi, Role } from '../types'
import { SectionTabs } from './SectionTabs'
import { UserManagement } from './UserManagement'
import { UsersManagementPage } from './UsersManagementPage'

type RoadGuideUsersSectionProps = {
  users: AdminUser[]
  pois: BusinessPoi[]
  busy: boolean
  message: string
  onRefresh: () => void
  onSetRole: (userId: string, role: Role) => void
  onSetAssignments: (userId: string, poiIds: string[]) => void
}

export function RoadGuideUsersSection(props: RoadGuideUsersSectionProps) {
  const [activeTab, setActiveTab] = useState('platform')

  return (
    <div className="flex flex-col min-h-full">
      <SectionTabs
        tabs={[
          { id: 'platform', label: 'Platform Users' },
          { id: 'road-guide', label: 'Roles & POI Assignments' },
        ]}
        activeTab={activeTab}
        onTabChange={setActiveTab}
      />
      {activeTab === 'platform' ? (
        <UserManagement />
      ) : (
        <UsersManagementPage {...props} />
      )}
    </div>
  )
}
