import { Users, MapPin, TrendingUp, Activity, type LucideIcon } from "lucide-react";
import { LineChart, Line, AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { GatewayStatusBanner } from "./GatewayStatusBanner";
import { useAdmin } from "../hooks/useAdminResource";

/**
 * Dashboard is fully driven by `GET /api/v1/admin/dashboard/overview`. The
 * fallback below mirrors the API response so the screen still renders if the
 * gateway is unreachable (during early local development, etc).
 */

type StatTile = { label: string; value: string; change: string; icon: string; color: string };
type UserGrowth = { month: string; users: number; active: number };
type PoiCategory = { name: string; value: number; color: string };
type RevenueDay = { date: string; ads: number; subscriptions: number };
type ActivityItem = { id: number; tone: string; text: string; time: string };
type Health = { label: string; status: string; tone: "green" | "yellow" | "red"; percent: number };
type Quick = { label: string; value: number; tone: "yellow" | "blue" | "red" | "purple" };

type DashboardOverview = {
  stats: StatTile[];
  userGrowth: UserGrowth[];
  poiCategories: PoiCategory[];
  revenue: RevenueDay[];
  recentActivities: ActivityItem[];
  systemHealth: Health[];
  quickStats: Quick[];
};

// Map API `icon: "users"` strings to actual lucide-react components. Keeping
// this on the client lets the API stay free of UI concerns.
const ICONS: Record<string, LucideIcon> = {
  users: Users,
  "map-pin": MapPin,
  activity: Activity,
  "trending-up": TrendingUp,
};

const HEALTH_TONES: Record<Health["tone"], { text: string; bar: string }> = {
  green: { text: "text-green-600 dark:text-green-400", bar: "bg-green-600" },
  yellow: { text: "text-yellow-600 dark:text-yellow-400", bar: "bg-yellow-600" },
  red: { text: "text-red-600 dark:text-red-400", bar: "bg-red-600" },
};

const PILL_TONES: Record<Quick["tone"], string> = {
  yellow: "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300",
  blue: "bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300",
  red: "bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300",
  purple: "bg-purple-100 dark:bg-purple-900/30 text-purple-800 dark:text-purple-300",
};

const ACTIVITY_DOTS: Record<string, string> = {
  "chart-1": "bg-chart-1",
  "chart-2": "bg-chart-2",
  "chart-3": "bg-chart-3",
  "chart-4": "bg-chart-4",
  "chart-5": "bg-chart-5",
};

const FALLBACK: DashboardOverview = {
  stats: [
    { label: "Total Users", value: "124,583", change: "+12.5%", icon: "users", color: "text-chart-1" },
    { label: "Active POIs", value: "8,429", change: "+8.2%", icon: "map-pin", color: "text-chart-2" },
    { label: "Daily Active Users", value: "45,892", change: "+15.3%", icon: "activity", color: "text-chart-3" },
    { label: "Revenue", value: "$89,234", change: "+23.1%", icon: "trending-up", color: "text-chart-4" },
  ],
  userGrowth: [
    { month: "Jan", users: 45000, active: 32000 },
    { month: "Feb", users: 52000, active: 38000 },
    { month: "Mar", users: 61000, active: 45000 },
    { month: "Apr", users: 73000, active: 54000 },
    { month: "May", users: 89000, active: 67000 },
    { month: "Jun", users: 105000, active: 79000 },
  ],
  poiCategories: [
    { name: "Restaurants", value: 2340, color: "var(--chart-1)" },
    { name: "Shops", value: 1890, color: "var(--chart-2)" },
    { name: "Entertainment", value: 1520, color: "var(--chart-3)" },
    { name: "Services", value: 980, color: "var(--chart-4)" },
    { name: "Other", value: 1699, color: "var(--chart-5)" },
  ],
  revenue: [
    { date: "Mon", ads: 4200, subscriptions: 2800 },
    { date: "Tue", ads: 3800, subscriptions: 3100 },
    { date: "Wed", ads: 5100, subscriptions: 2900 },
    { date: "Thu", ads: 4600, subscriptions: 3400 },
    { date: "Fri", ads: 5800, subscriptions: 3600 },
    { date: "Sat", ads: 6200, subscriptions: 3200 },
    { date: "Sun", ads: 5400, subscriptions: 2700 },
  ],
  recentActivities: [
    { id: 1, tone: "chart-1", text: "New 360° image uploaded", time: "2 minutes ago" },
    { id: 2, tone: "chart-2", text: "Business account approved", time: "15 minutes ago" },
    { id: 3, tone: "chart-3", text: "POI claim request submitted", time: "45 minutes ago" },
    { id: 4, tone: "chart-4", text: "New ad campaign started", time: "1 hour ago" },
    { id: 5, tone: "chart-5", text: "OTA update deployed", time: "3 hours ago" },
  ],
  systemHealth: [
    { label: "API Response Time", status: "Good", tone: "green", percent: 85 },
    { label: "Server Load", status: "Moderate", tone: "yellow", percent: 60 },
    { label: "Database Performance", status: "Excellent", tone: "green", percent: 92 },
    { label: "CDN Status", status: "Online", tone: "green", percent: 100 },
  ],
  quickStats: [
    { label: "Pending 360° Images", value: 2, tone: "yellow" },
    { label: "Pending POI Claims", value: 23, tone: "yellow" },
    { label: "Active Ad Campaigns", value: 47, tone: "blue" },
    { label: "Flagged Messages", value: 8, tone: "red" },
    { label: "Business Approvals", value: 12, tone: "purple" },
  ],
};

export function Dashboard() {
  const { data: overview } = useAdmin<DashboardOverview>("/dashboard/overview", FALLBACK);

  return (
    <div className="p-8 space-y-8">
      <div className="flex flex-col gap-3">
        <GatewayStatusBanner />
        <div>
          <h2 className="text-2xl mb-2">Dashboard Overview</h2>
          <p className="text-muted-foreground">Monitor your maps platform performance</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {overview.stats.map((stat) => {
          const Icon = ICONS[stat.icon] ?? Activity;
          return (
            <div key={stat.label} className="bg-card border border-border rounded-lg p-6">
              <div className="flex items-center justify-between mb-4">
                <Icon className={`w-8 h-8 ${stat.color}`} />
                <span className="text-sm text-green-600 dark:text-green-400">{stat.change}</span>
              </div>
              <div className="text-3xl mb-1">{stat.value}</div>
              <div className="text-sm text-muted-foreground">{stat.label}</div>
            </div>
          );
        })}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-6">User Growth</h3>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={overview.userGrowth}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
              <XAxis dataKey="month" stroke="var(--muted-foreground)" />
              <YAxis stroke="var(--muted-foreground)" />
              <Tooltip
                contentStyle={{
                  backgroundColor: "var(--card)",
                  border: "1px solid var(--border)",
                  borderRadius: "8px"
                }}
              />
              <Legend />
              <Area type="monotone" dataKey="users" stroke="var(--chart-1)" fill="var(--chart-1)" fillOpacity={0.6} name="Total Users" />
              <Area type="monotone" dataKey="active" stroke="var(--chart-2)" fill="var(--chart-2)" fillOpacity={0.6} name="Active Users" />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-6">POI Categories Distribution</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={overview.poiCategories}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }: { name: string; percent: number }) =>
                  `${name} ${(percent * 100).toFixed(0)}%`}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
              >
                {overview.poiCategories.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{
                  backgroundColor: "var(--card)",
                  border: "1px solid var(--border)",
                  borderRadius: "8px"
                }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-card border border-border rounded-lg p-6 lg:col-span-2">
          <h3 className="mb-6">Revenue Overview</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={overview.revenue}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
              <XAxis dataKey="date" stroke="var(--muted-foreground)" />
              <YAxis stroke="var(--muted-foreground)" />
              <Tooltip
                contentStyle={{
                  backgroundColor: "var(--card)",
                  border: "1px solid var(--border)",
                  borderRadius: "8px"
                }}
              />
              <Legend />
              <Bar dataKey="ads" fill="var(--chart-1)" name="Ad Revenue" />
              <Bar dataKey="subscriptions" fill="var(--chart-2)" name="Subscriptions" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-4">Recent Activities</h3>
          <div className="space-y-4">
            {overview.recentActivities.map((a) => (
              <div key={a.id} className="flex items-start gap-3">
                <div className={`w-2 h-2 ${ACTIVITY_DOTS[a.tone] ?? "bg-chart-1"} rounded-full mt-2`}></div>
                <div className="flex-1">
                  <p className="text-sm">{a.text}</p>
                  <p className="text-xs text-muted-foreground">{a.time}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-4">System Health</h3>
          <div className="space-y-4">
            {overview.systemHealth.map((h) => (
              <div key={h.label}>
                <div className="flex justify-between mb-2">
                  <span className="text-sm">{h.label}</span>
                  <span className={`text-sm ${HEALTH_TONES[h.tone].text}`}>{h.status}</span>
                </div>
                <div className="w-full bg-secondary h-2 rounded-full">
                  <div className={`${HEALTH_TONES[h.tone].bar} h-2 rounded-full`} style={{ width: `${h.percent}%` }}></div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-4">Quick Stats</h3>
          <div className="space-y-4">
            {overview.quickStats.map((q) => (
              <div key={q.label} className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">{q.label}</span>
                <span className={`px-3 py-1 rounded-full text-sm ${PILL_TONES[q.tone]}`}>{q.value}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
