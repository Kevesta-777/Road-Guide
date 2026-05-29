type StatusBadgeProps = {
  label: string
  tone: 'green' | 'yellow' | 'red' | 'blue' | 'gray'
}

const toneClasses: Record<StatusBadgeProps['tone'], string> = {
  green: 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300',
  yellow: 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300',
  red: 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300',
  blue: 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300',
  gray: 'bg-gray-100 dark:bg-gray-900/30 text-gray-800 dark:text-gray-300',
}

export function StatusBadge({ label, tone }: StatusBadgeProps) {
  return (
    <span className={`px-3 py-1 rounded-full text-sm w-fit ${toneClasses[tone]}`}>
      {label}
    </span>
  )
}

export function roleTone(role: string): StatusBadgeProps['tone'] {
  switch (role) {
    case 'admin':
      return 'blue'
    case 'business':
      return 'green'
    default:
      return 'gray'
  }
}
