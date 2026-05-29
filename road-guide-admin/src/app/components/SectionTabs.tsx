type SectionTab = {
  id: string
  label: string
}

type SectionTabsProps = {
  tabs: SectionTab[]
  activeTab: string
  onTabChange: (id: string) => void
}

export function SectionTabs({ tabs, activeTab, onTabChange }: SectionTabsProps) {
  return (
    <div className="border-b border-border px-8 pt-6">
      <div className="flex gap-2">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => onTabChange(tab.id)}
            className={`px-4 py-2 text-sm rounded-t-lg transition-colors ${
              activeTab === tab.id
                ? 'bg-background text-foreground border border-b-0 border-border -mb-px'
                : 'text-muted-foreground hover:text-foreground hover:bg-accent/50'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>
    </div>
  )
}
