import { useState } from 'react'
import type { RegistrationRequest } from '../types'
import { BusinessAccounts } from './AllSections'
import { RegistrationRequestsPage } from './RegistrationRequestsPage'
import { SectionTabs } from './SectionTabs'

type RoadGuideBusinessSectionProps = {
  requests: RegistrationRequest[]
  busy: boolean
  message: string
  onRefresh: () => void
  onApprove: (request: RegistrationRequest) => void
  onReject: (request: RegistrationRequest) => void
}

export function RoadGuideBusinessSection(props: RoadGuideBusinessSectionProps) {
  const [activeTab, setActiveTab] = useState('accounts')

  return (
    <div className="flex flex-col min-h-full">
      <SectionTabs
        tabs={[
          { id: 'accounts', label: 'Business Accounts' },
          { id: 'claims', label: 'Claim Requests' },
        ]}
        activeTab={activeTab}
        onTabChange={setActiveTab}
      />
      {activeTab === 'accounts' ? (
        <BusinessAccounts />
      ) : (
        <RegistrationRequestsPage
          requests={props.requests}
          busy={props.busy}
          message={props.message}
          onRefresh={props.onRefresh}
          onApprove={props.onApprove}
          onReject={props.onReject}
        />
      )}
    </div>
  )
}
