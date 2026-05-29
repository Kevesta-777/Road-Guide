import { useState } from "react";
import { Search, Plus, Play, Pause, BarChart, DollarSign, Target, Calendar } from "lucide-react";
import { useAdminCollection } from "../hooks/useAdminResource";

type Ad = {
  id: number;
  title: string;
  advertiser: string;
  budget: number;
  spent: number;
  impressions: number;
  clicks: number;
  status: string;
  startDate: string;
  endDate: string;
};

const fallbackAds: Ad[] = [
  { id: 1, title: "Summer Sale Campaign", advertiser: "Fashion Store", budget: 5000, spent: 3200, impressions: 145000, clicks: 8900, status: "Active", startDate: "2024-05-01", endDate: "2024-05-31" },
  { id: 2, title: "New Restaurant Opening", advertiser: "Joe's Diner", budget: 2000, spent: 1850, impressions: 52000, clicks: 3100, status: "Active", startDate: "2024-05-10", endDate: "2024-05-20" },
  { id: 3, title: "Tech Store Promo", advertiser: "ElectroMart", budget: 8000, spent: 8000, impressions: 198000, clicks: 12400, status: "Completed", startDate: "2024-04-15", endDate: "2024-05-10" },
  { id: 4, title: "Gym Membership Deal", advertiser: "FitPro Gym", budget: 3500, spent: 890, impressions: 28000, clicks: 1560, status: "Paused", startDate: "2024-05-08", endDate: "2024-06-08" },
];

export function AdsManagement() {
  const [searchTerm, setSearchTerm] = useState("");
  const [showCreateModal, setShowCreateModal] = useState(false);

  const { data: mockAds } = useAdminCollection<Ad>("/ads", fallbackAds);

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl mb-2">Advertisement Management</h2>
          <p className="text-muted-foreground">Manage ad campaigns and performance</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          Create Campaign
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-card border border-border rounded-lg p-6">
          <div className="flex items-center justify-between mb-2">
            <h3>Total Revenue</h3>
            <DollarSign className="w-5 h-5 text-green-600" />
          </div>
          <div className="text-3xl">$89,234</div>
          <p className="text-sm text-green-600 dark:text-green-400 mt-1">+23.1% this month</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-6">
          <div className="flex items-center justify-between mb-2">
            <h3>Active Campaigns</h3>
            <Play className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-3xl">47</div>
          <p className="text-sm text-muted-foreground mt-1">12 ending soon</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-6">
          <div className="flex items-center justify-between mb-2">
            <h3>Total Impressions</h3>
            <BarChart className="w-5 h-5 text-purple-600" />
          </div>
          <div className="text-3xl">2.4M</div>
          <p className="text-sm text-muted-foreground mt-1">Last 30 days</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-6">
          <div className="flex items-center justify-between mb-2">
            <h3>Avg CTR</h3>
            <Target className="w-5 h-5 text-orange-600" />
          </div>
          <div className="text-3xl">6.2%</div>
          <p className="text-sm text-green-600 dark:text-green-400 mt-1">+1.3% vs last month</p>
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <div className="flex flex-col sm:flex-row gap-4 mb-6">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <input
              type="text"
              placeholder="Search campaigns..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
        </div>

        <div className="space-y-4">
          {mockAds.map((ad) => (
            <div key={ad.id} className="border border-border rounded-lg p-6 hover:bg-accent/30 transition-colors">
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <h3>{ad.title}</h3>
                    <span className={`px-3 py-1 rounded-full text-sm ${
                      ad.status === "Active"
                        ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                        : ad.status === "Paused"
                        ? "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                        : "bg-gray-100 dark:bg-gray-900/30 text-gray-800 dark:text-gray-300"
                    }`}>
                      {ad.status}
                    </span>
                  </div>
                  <p className="text-sm text-muted-foreground">{ad.advertiser}</p>
                  <div className="flex items-center gap-4 mt-2 text-sm">
                    <span className="flex items-center gap-1">
                      <Calendar className="w-4 h-4" />
                      {ad.startDate} - {ad.endDate}
                    </span>
                  </div>
                </div>
                <div className="flex gap-2">
                  <button className="px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors flex items-center gap-2">
                    {ad.status === "Active" ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
                    {ad.status === "Active" ? "Pause" : "Resume"}
                  </button>
                  <button className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">
                    View Details
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Budget</p>
                  <p className="text-lg">${ad.budget.toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Spent</p>
                  <p className="text-lg">${ad.spent.toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Impressions</p>
                  <p className="text-lg">{(ad.impressions / 1000).toFixed(1)}K</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Clicks</p>
                  <p className="text-lg">{ad.clicks.toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">CTR</p>
                  <p className="text-lg">{((ad.clicks / ad.impressions) * 100).toFixed(2)}%</p>
                </div>
              </div>

              <div className="mt-4">
                <div className="flex justify-between text-sm mb-2">
                  <span className="text-muted-foreground">Budget Usage</span>
                  <span>{((ad.spent / ad.budget) * 100).toFixed(0)}%</span>
                </div>
                <div className="w-full bg-secondary h-2 rounded-full overflow-hidden">
                  <div
                    className="bg-chart-1 h-2 rounded-full transition-all"
                    style={{ width: `${(ad.spent / ad.budget) * 100}%` }}
                  ></div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={() => setShowCreateModal(false)}>
          <div className="bg-card border border-border rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-xl mb-4">Create Ad Campaign</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm mb-2">Campaign Title</label>
                <input type="text" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring" placeholder="Enter campaign title" />
              </div>
              <div>
                <label className="block text-sm mb-2">Advertiser</label>
                <input type="text" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring" placeholder="Business name" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm mb-2">Budget ($)</label>
                  <input type="number" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring" placeholder="5000" />
                </div>
                <div>
                  <label className="block text-sm mb-2">Duration (days)</label>
                  <input type="number" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring" placeholder="30" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm mb-2">Start Date</label>
                  <input type="date" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring" />
                </div>
                <div>
                  <label className="block text-sm mb-2">End Date</label>
                  <input type="date" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring" />
                </div>
              </div>
              <div>
                <label className="block text-sm mb-2">Target Location</label>
                <select className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring">
                  <option>All Locations</option>
                  <option>Within 5 miles</option>
                  <option>Within 10 miles</option>
                  <option>Custom Radius</option>
                </select>
              </div>
              <div>
                <label className="block text-sm mb-2">Ad Content</label>
                <textarea className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring h-24" placeholder="Enter ad description..."></textarea>
              </div>
              <div className="flex gap-3 pt-4">
                <button onClick={() => setShowCreateModal(false)} className="flex-1 px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                  Cancel
                </button>
                <button className="flex-1 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">
                  Create Campaign
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
