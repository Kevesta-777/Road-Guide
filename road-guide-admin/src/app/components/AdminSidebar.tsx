import {
  LayoutDashboard,
  Users,
  MapPin,
  Megaphone,
  Gamepad2,
  MessageSquare,
  Download,
  Settings,
  BarChart3,
  Shield,
  Building2,
  Bell,
  Search,
  Sparkles,
  Map,
  Moon,
  Sun,
  Image as ImageIcon,
  Crown as CrownIcon,
  DollarSign as DollarSignIcon,
  Car as CarIcon,
  LogOut,
} from "lucide-react";
import type { AuthUser } from "../types";

interface AdminSidebarProps {
  activeSection: string;
  onSectionChange: (section: string) => void;
  isDark: boolean;
  onThemeToggle: () => void;
  me?: AuthUser | null;
  onLogout?: () => void;
  pendingClaims?: number;
}

export function AdminSidebar({
  activeSection,
  onSectionChange,
  isDark,
  onThemeToggle,
  me,
  onLogout,
  pendingClaims = 0,
}: AdminSidebarProps) {
  const menuItems = [
    { id: "dashboard", icon: LayoutDashboard, label: "Dashboard" },
    { id: "users", icon: Users, label: "User Management" },
    { id: "subscriptions", icon: CrownIcon, label: "Premium & Subscriptions" },
    { id: "payments", icon: DollarSignIcon, label: "Payments & Revenue" },
    { id: "pois", icon: MapPin, label: "POI Management" },
    { id: "image360", icon: ImageIcon, label: "360° Images" },
    { id: "companion", icon: CarIcon, label: "Companion Finder" },
    { id: "ads", icon: Megaphone, label: "Advertisements" },
    { id: "games", icon: Gamepad2, label: "Games" },
    { id: "chat", icon: MessageSquare, label: "Chat Moderation" },
    { id: "map-content", icon: Map, label: "Map Content" },
    { id: "business", icon: Building2, label: "Business Accounts" },
    { id: "ai-guide", icon: Sparkles, label: "AI Guide" },
    { id: "ota", icon: Download, label: "OTA Updates" },
    { id: "analytics", icon: BarChart3, label: "Analytics & Reports" },
    { id: "notifications", icon: Bell, label: "Notifications" },
    { id: "moderation", icon: Shield, label: "Content Moderation" },
    { id: "search", icon: Search, label: "Search Management" },
    { id: "settings", icon: Settings, label: "Settings" },
  ];

  return (
    <div className="w-64 bg-sidebar border-r border-sidebar-border h-screen flex flex-col">
      <div className="p-6 border-b border-sidebar-border">
        <h1 className="text-xl text-sidebar-foreground">Road Guide Admin</h1>
        <p className="text-sm text-muted-foreground mt-1">Control Panel</p>
      </div>

      <nav className="flex-1 overflow-y-auto p-4">
        <ul className="space-y-1">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeSection === item.id;

            return (
              <li key={item.id}>
                <button
                  onClick={() => onSectionChange(item.id)}
                  className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg transition-colors ${
                    isActive
                      ? "bg-sidebar-accent text-sidebar-accent-foreground"
                      : "text-sidebar-foreground hover:bg-sidebar-accent/50"
                  }`}
                >
                  <Icon className="w-5 h-5 shrink-0" />
                  <span className="text-sm flex-1 text-left">{item.label}</span>
                  {item.id === "business" && pendingClaims > 0 && (
                    <span className="text-xs px-2 py-0.5 rounded-full bg-red-500 text-white">
                      {pendingClaims}
                    </span>
                  )}
                </button>
              </li>
            );
          })}
        </ul>
      </nav>

      <div className="p-4 border-t border-sidebar-border space-y-1">
        {me && (
          <div className="px-3 py-2 text-xs text-muted-foreground truncate" title={me.email}>
            {me.name}
          </div>
        )}
        <button
          onClick={onThemeToggle}
          className="w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sidebar-foreground hover:bg-sidebar-accent/50 transition-colors"
        >
          {isDark ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
          <span className="text-sm">{isDark ? "Light Mode" : "Dark Mode"}</span>
        </button>
        {onLogout && (
          <button
            onClick={onLogout}
            className="w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sidebar-foreground hover:bg-sidebar-accent/50 transition-colors"
          >
            <LogOut className="w-5 h-5" />
            <span className="text-sm">Sign out</span>
          </button>
        )}
      </div>
    </div>
  );
}
