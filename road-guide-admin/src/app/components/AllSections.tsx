import { useState } from "react";
import { MessageSquare, Download, Bell, Shield, Search as SearchIcon, Sparkles, Settings as SettingsIcon, Map, BarChart3, Building2, MapPin, CheckCircle, Clock, Calendar } from "lucide-react";

export function ChatModeration() {
  const messages = [
    { id: 1, from: "John S.", to: "Sarah M.", content: "Hey, want to meet at the park?", flagged: false, time: "2 min ago" },
    { id: 2, from: "Anonymous", to: "Mike C.", content: "Check this link: suspicious-site.com", flagged: true, time: "15 min ago" },
    { id: 3, from: "Emma W.", to: "David L.", content: "Thanks for the restaurant recommendation!", flagged: false, time: "1 hour ago" },
  ];

  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">Chat Moderation</h2>
        <p className="text-muted-foreground">Monitor and moderate user messages</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Total Messages</h3>
          <div className="text-3xl">234,567</div>
          <p className="text-sm text-muted-foreground mt-1">Last 24 hours</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Flagged Messages</h3>
          <div className="text-3xl text-red-600 dark:text-red-400">23</div>
          <p className="text-sm text-muted-foreground mt-1">Require review</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Auto-Blocked</h3>
          <div className="text-3xl">156</div>
          <p className="text-sm text-muted-foreground mt-1">By AI filter</p>
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <h3 className="mb-4">Recent Messages</h3>
        <div className="space-y-3">
          {messages.map((msg) => (
            <div key={msg.id} className={`p-4 rounded-lg border ${msg.flagged ? "border-red-500 bg-red-50 dark:bg-red-900/10" : "border-border"}`}>
              <div className="flex items-start justify-between mb-2">
                <div>
                  <span className="font-medium">{msg.from}</span>
                  <span className="text-muted-foreground mx-2">→</span>
                  <span>{msg.to}</span>
                </div>
                <span className="text-sm text-muted-foreground">{msg.time}</span>
              </div>
              <p className="text-sm mb-2">{msg.content}</p>
              {msg.flagged && (
                <div className="flex gap-2 mt-3">
                  <button className="px-3 py-1 bg-green-600 text-white rounded text-sm">Approve</button>
                  <button className="px-3 py-1 bg-red-600 text-white rounded text-sm">Block</button>
                  <button className="px-3 py-1 border border-border rounded text-sm">Review</button>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export function OTAUpdates() {
  const updates = [
    { version: "2.4.0", status: "Live", users: "98.5%", released: "2024-05-10", size: "45.2 MB" },
    { version: "2.3.5", status: "Deprecated", users: "1.2%", released: "2024-04-15", size: "43.8 MB" },
    { version: "2.5.0-beta", status: "Testing", users: "0.3%", released: "2024-05-12", size: "46.1 MB" },
  ];

  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">OTA Updates</h2>
        <p className="text-muted-foreground">Manage over-the-air app updates</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Current Version</h3>
          <div className="text-3xl">2.4.0</div>
          <p className="text-sm text-green-600 dark:text-green-400 mt-1">98.5% adoption</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Beta Testing</h3>
          <div className="text-3xl">2.5.0</div>
          <p className="text-sm text-muted-foreground mt-1">350 beta users</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Update Success Rate</h3>
          <div className="text-3xl">99.7%</div>
          <p className="text-sm text-green-600 dark:text-green-400 mt-1">Excellent stability</p>
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <div className="flex items-center justify-between mb-6">
          <h3>Version History</h3>
          <button className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">
            Upload New Version
          </button>
        </div>
        <div className="space-y-4">
          {updates.map((update) => (
            <div key={update.version} className="border border-border rounded-lg p-4">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-3">
                  <Download className="w-5 h-5 text-blue-600" />
                  <div>
                    <h4>Version {update.version}</h4>
                    <p className="text-sm text-muted-foreground">Released {update.released}</p>
                  </div>
                </div>
                <span className={`px-3 py-1 rounded-full text-sm ${
                  update.status === "Live" ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300" :
                  update.status === "Testing" ? "bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300" :
                  "bg-gray-100 dark:bg-gray-900/30 text-gray-800 dark:text-gray-300"
                }`}>
                  {update.status}
                </span>
              </div>
              <div className="grid grid-cols-3 gap-4 text-sm">
                <div>
                  <p className="text-muted-foreground">Adoption</p>
                  <p className="font-medium">{update.users}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Size</p>
                  <p className="font-medium">{update.size}</p>
                </div>
                <div className="flex gap-2">
                  <button className="px-3 py-1 border border-border rounded text-sm hover:bg-accent">Details</button>
                  <button className="px-3 py-1 border border-border rounded text-sm hover:bg-accent">Rollback</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export function BusinessAccounts() {
  const [selectedBusiness, setSelectedBusiness] = useState<number | null>(null);

  const businesses = [
    {
      id: 1,
      name: "Joe's Coffee",
      owner: "Joe Smith",
      email: "joe@joescoffee.com",
      status: "Approved",
      pois: 3,
      verified: true,
      joined: "2024-01-15",
      revenue: "$12,450",
      views: 8920,
      poiList: ["Joe's Coffee Shop - Main St", "Joe's Coffee - Park Ave", "Joe's Coffee - Downtown"]
    },
    {
      id: 2,
      name: "TechMart Store",
      owner: "Sarah Lee",
      email: "sarah@techmart.com",
      status: "Pending",
      pois: 1,
      verified: false,
      joined: "2024-05-10",
      revenue: "$0",
      views: 234,
      poiList: ["TechMart Store - Oak Ave"]
    },
    {
      id: 3,
      name: "Bella Restaurant",
      owner: "Maria Lopez",
      email: "maria@bella.com",
      status: "Approved",
      pois: 2,
      verified: true,
      joined: "2024-02-20",
      revenue: "$8,340",
      views: 5670,
      poiList: ["Bella Restaurant - Pine St", "Bella Cafe - Market Square"]
    },
  ];

  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">Business Accounts</h2>
        <p className="text-muted-foreground">Manage business registrations and their POIs</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Total Businesses</h3>
            <Building2 className="w-5 h-5 text-purple-600" />
          </div>
          <div className="text-2xl mb-1">2,345</div>
          <p className="text-xs text-green-600 dark:text-green-400">+12 this week</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Pending Approval</h3>
            <Clock className="w-5 h-5 text-yellow-600" />
          </div>
          <div className="text-2xl mb-1">18</div>
          <p className="text-xs text-muted-foreground">Awaiting review</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Verified</h3>
            <CheckCircle className="w-5 h-5 text-green-600" />
          </div>
          <div className="text-2xl mb-1">2,127</div>
          <p className="text-xs text-muted-foreground">90.7% verified</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Total POIs</h3>
            <MapPin className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl mb-1">7,504</div>
          <p className="text-xs text-muted-foreground">Avg 3.2 per business</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-card border border-border rounded-lg">
          <div className="p-6 border-b border-border">
            <h3>Business Applications</h3>
          </div>
          <div className="p-6 space-y-4">
            {businesses.map((business) => (
              <div
                key={business.id}
                className="border border-border rounded-lg p-5 hover:border-primary/50 transition-all cursor-pointer"
                onClick={() => setSelectedBusiness(selectedBusiness === business.id ? null : business.id)}
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-start gap-4 flex-1">
                    <div className="w-14 h-14 bg-gradient-to-br from-purple-500 to-pink-500 rounded-lg flex items-center justify-center flex-shrink-0">
                      <Building2 className="w-7 h-7 text-white" />
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <h4>{business.name}</h4>
                        {business.verified && (
                          <CheckCircle className="w-4 h-4 text-green-600" />
                        )}
                      </div>
                      <p className="text-sm text-muted-foreground mb-2">{business.owner} • {business.email}</p>
                      <div className="flex items-center gap-4 text-sm">
                        <span className="flex items-center gap-1">
                          <MapPin className="w-4 h-4 text-blue-600" />
                          {business.pois} POIs
                        </span>
                        <span className="flex items-center gap-1">
                          <Calendar className="w-4 h-4" />
                          Joined {business.joined}
                        </span>
                      </div>
                    </div>
                  </div>
                  <span className={`px-3 py-1 rounded-full text-sm flex-shrink-0 ${
                    business.status === "Approved"
                      ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                      : "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                  }`}>
                    {business.status}
                  </span>
                </div>

                {selectedBusiness === business.id && (
                  <div className="mt-4 pt-4 border-t border-border animate-in slide-in-from-top-2">
                    <div className="grid grid-cols-3 gap-4 mb-4">
                      <div className="text-center p-3 bg-accent/30 rounded-lg">
                        <p className="text-xs text-muted-foreground mb-1">Revenue</p>
                        <p className="font-medium">{business.revenue}</p>
                      </div>
                      <div className="text-center p-3 bg-accent/30 rounded-lg">
                        <p className="text-xs text-muted-foreground mb-1">Views</p>
                        <p className="font-medium">{business.views.toLocaleString()}</p>
                      </div>
                      <div className="text-center p-3 bg-accent/30 rounded-lg">
                        <p className="text-xs text-muted-foreground mb-1">Verified</p>
                        <p className="font-medium">{business.verified ? "Yes" : "No"}</p>
                      </div>
                    </div>

                    <div className="mb-4">
                      <h5 className="text-sm mb-2">Managed POIs:</h5>
                      <div className="space-y-2">
                        {business.poiList.map((poi, idx) => (
                          <div key={idx} className="flex items-center gap-2 text-sm p-2 bg-accent/20 rounded">
                            <MapPin className="w-4 h-4 text-blue-600" />
                            <span>{poi}</span>
                          </div>
                        ))}
                      </div>
                    </div>

                    <div className="flex gap-2">
                      {business.status === "Pending" ? (
                        <>
                          <button className="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">
                            Approve Account
                          </button>
                          <button className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
                            Reject
                          </button>
                          <button className="px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                            Request Info
                          </button>
                        </>
                      ) : (
                        <>
                          <button className="flex-1 px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                            View Details
                          </button>
                          <button className="flex-1 px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                            Manage POIs
                          </button>
                          <button className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">
                            Contact
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-card border border-border rounded-lg p-6">
            <h3 className="mb-4">Quick Actions</h3>
            <div className="space-y-2">
              <button className="w-full px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors text-sm">
                Review Pending
              </button>
              <button className="w-full px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors text-sm">
                Export Data
              </button>
              <button className="w-full px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors text-sm">
                Send Announcement
              </button>
            </div>
          </div>

          <div className="bg-card border border-border rounded-lg p-6">
            <h3 className="mb-4">Top Performing</h3>
            <div className="space-y-3">
              {[
                { name: "Joe's Coffee", revenue: "$12,450", change: "+23%" },
                { name: "Bella Restaurant", revenue: "$8,340", change: "+18%" },
                { name: "FitPro Gym", revenue: "$6,780", change: "+12%" },
              ].map((biz, idx) => (
                <div key={idx} className="flex items-center justify-between p-3 bg-accent/30 rounded-lg">
                  <div>
                    <p className="text-sm font-medium">{biz.name}</p>
                    <p className="text-xs text-muted-foreground">{biz.revenue}</p>
                  </div>
                  <span className="text-xs text-green-600 dark:text-green-400">{biz.change}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export function AIGuide() {
  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">AI Guide Configuration</h2>
        <p className="text-muted-foreground">Manage AI assistant and recommendations</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">AI Interactions</h3>
          <div className="text-3xl">45,892</div>
          <p className="text-sm text-muted-foreground mt-1">Last 24 hours</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Satisfaction Rate</h3>
          <div className="text-3xl text-green-600 dark:text-green-400">94%</div>
          <p className="text-sm text-muted-foreground mt-1">User feedback</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Avg Response Time</h3>
          <div className="text-3xl">0.8s</div>
          <p className="text-sm text-green-600 dark:text-green-400 mt-1">Excellent</p>
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <h3 className="mb-4">AI Settings</h3>
        <div className="space-y-6">
          <div>
            <label className="block text-sm mb-2">Model Version</label>
            <select className="w-full px-4 py-2 bg-input-background border border-border rounded-lg">
              <option>GPT-4 Turbo (Latest)</option>
              <option>GPT-4</option>
              <option>GPT-3.5 Turbo</option>
            </select>
          </div>
          <div>
            <label className="block text-sm mb-2">Recommendation Radius</label>
            <input type="range" min="1" max="50" defaultValue="10" className="w-full" />
            <p className="text-sm text-muted-foreground mt-1">10 miles</p>
          </div>
          <div>
            <label className="block text-sm mb-2">Personality Tone</label>
            <select className="w-full px-4 py-2 bg-input-background border border-border rounded-lg">
              <option>Friendly & Casual</option>
              <option>Professional</option>
              <option>Concise</option>
            </select>
          </div>
          <div className="flex items-center justify-between p-4 bg-accent/30 rounded-lg">
            <div>
              <h4>Enable Context Learning</h4>
              <p className="text-sm text-muted-foreground">AI learns from user preferences</p>
            </div>
            <input type="checkbox" defaultChecked className="w-5 h-5" />
          </div>
          <button className="w-full px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90">
            Save Configuration
          </button>
        </div>
      </div>
    </div>
  );
}

export function Analytics() {
  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">Analytics & Reports</h2>
        <p className="text-muted-foreground">Detailed platform analytics</p>
      </div>
      <div className="bg-card border border-border rounded-lg p-6">
        <h3 className="mb-4">Generate Custom Report</h3>
        <div className="grid grid-cols-2 gap-4">
          <button className="p-6 border border-border rounded-lg hover:bg-accent transition-colors text-left">
            <BarChart3 className="w-6 h-6 mb-2 text-chart-1" />
            <h4>User Activity Report</h4>
            <p className="text-sm text-muted-foreground">Engagement metrics</p>
          </button>
          <button className="p-6 border border-border rounded-lg hover:bg-accent transition-colors text-left">
            <BarChart3 className="w-6 h-6 mb-2 text-chart-2" />
            <h4>Revenue Report</h4>
            <p className="text-sm text-muted-foreground">Financial analysis</p>
          </button>
          <button className="p-6 border border-border rounded-lg hover:bg-accent transition-colors text-left">
            <BarChart3 className="w-6 h-6 mb-2 text-chart-3" />
            <h4>POI Performance</h4>
            <p className="text-sm text-muted-foreground">Location insights</p>
          </button>
          <button className="p-6 border border-border rounded-lg hover:bg-accent transition-colors text-left">
            <BarChart3 className="w-6 h-6 mb-2 text-chart-4" />
            <h4>Ad Campaign ROI</h4>
            <p className="text-sm text-muted-foreground">Marketing metrics</p>
          </button>
        </div>
      </div>
    </div>
  );
}

export function Notifications() {
  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">Notifications</h2>
        <p className="text-muted-foreground">Send push notifications and announcements</p>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <h3 className="mb-4">Send New Notification</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm mb-2">Title</label>
            <input type="text" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" placeholder="Notification title" />
          </div>
          <div>
            <label className="block text-sm mb-2">Message</label>
            <textarea className="w-full px-4 py-2 bg-input-background border border-border rounded-lg h-24" placeholder="Notification message..."></textarea>
          </div>
          <div>
            <label className="block text-sm mb-2">Target Audience</label>
            <select className="w-full px-4 py-2 bg-input-background border border-border rounded-lg">
              <option>All Users</option>
              <option>Consumer Users Only</option>
              <option>Business Users Only</option>
              <option>Specific User Group</option>
            </select>
          </div>
          <button className="w-full px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90">
            Send Notification
          </button>
        </div>
      </div>
    </div>
  );
}

export function ContentModeration() {
  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">Content Moderation</h2>
        <p className="text-muted-foreground">Review user-generated content</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Pending Review</h3>
          <div className="text-3xl text-yellow-600 dark:text-yellow-400">34</div>
          <p className="text-sm text-muted-foreground mt-1">Items awaiting approval</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Approved Today</h3>
          <div className="text-3xl">127</div>
          <p className="text-sm text-green-600 dark:text-green-400 mt-1">Content published</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Rejected Today</h3>
          <div className="text-3xl">8</div>
          <p className="text-sm text-red-600 dark:text-red-400 mt-1">Policy violations</p>
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <h3 className="mb-4">Content Queue</h3>
        <div className="space-y-3">
          {[
            { type: "POI Photo", user: "John S.", content: "Restaurant interior photo", time: "5 min ago" },
            { type: "Review", user: "Emma W.", content: "Great service and food quality!", time: "12 min ago" },
            { type: "POI Description", user: "Mike C.", content: "Best coffee shop in town...", time: "23 min ago" },
          ].map((item, i) => (
            <div key={i} className="border border-border rounded-lg p-4">
              <div className="flex items-start justify-between mb-2">
                <div>
                  <span className="font-medium">{item.type}</span>
                  <span className="text-sm text-muted-foreground ml-2">by {item.user}</span>
                </div>
                <span className="text-sm text-muted-foreground">{item.time}</span>
              </div>
              <p className="text-sm mb-3">{item.content}</p>
              <div className="flex gap-2">
                <button className="px-3 py-1 bg-green-600 text-white rounded text-sm">Approve</button>
                <button className="px-3 py-1 bg-red-600 text-white rounded text-sm">Reject</button>
                <button className="px-3 py-1 border border-border rounded text-sm">View Details</button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export function SearchManagement() {
  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">Search Management</h2>
        <p className="text-muted-foreground">Manage search indexes and results</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Total Searches</h3>
          <div className="text-3xl">1.2M</div>
          <p className="text-sm text-muted-foreground mt-1">Last 30 days</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Avg Response Time</h3>
          <div className="text-3xl">120ms</div>
          <p className="text-sm text-green-600 dark:text-green-400 mt-1">Fast performance</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-2">Success Rate</h3>
          <div className="text-3xl">94%</div>
          <p className="text-sm text-muted-foreground mt-1">Found results</p>
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <h3 className="mb-4">Top Search Queries</h3>
        <div className="space-y-3">
          {[
            { query: "coffee shops near me", count: 45230, trend: "+12%" },
            { query: "restaurants", count: 38920, trend: "+8%" },
            { query: "gas stations", count: 29340, trend: "-2%" },
            { query: "parks", count: 23450, trend: "+15%" },
            { query: "shopping malls", count: 18670, trend: "+5%" },
          ].map((item, i) => (
            <div key={i} className="flex items-center justify-between p-3 bg-accent/30 rounded-lg">
              <div className="flex items-center gap-3">
                <span className="text-muted-foreground">#{i + 1}</span>
                <span>{item.query}</span>
              </div>
              <div className="flex items-center gap-4">
                <span className="text-sm text-muted-foreground">{item.count.toLocaleString()} searches</span>
                <span className={`text-sm ${item.trend.startsWith("+") ? "text-green-600 dark:text-green-400" : "text-red-600 dark:text-red-400"}`}>
                  {item.trend}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export function MapContent() {
  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">Map Content Management</h2>
        <p className="text-muted-foreground">Manage map layers, overlays, and custom content</p>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <h3 className="mb-4">Map Layers</h3>
        <div className="space-y-3">
          {[
            { name: "Traffic Layer", enabled: true, users: "All" },
            { name: "Transit Layer", enabled: true, users: "All" },
            { name: "Bicycle Routes", enabled: false, users: "Premium" },
            { name: "3D Buildings", enabled: true, users: "All" },
            { name: "Satellite View", enabled: true, users: "All" },
          ].map((layer, i) => (
            <div key={i} className="flex items-center justify-between p-4 border border-border rounded-lg">
              <div className="flex items-center gap-3">
                <Map className="w-5 h-5 text-chart-1" />
                <div>
                  <h4>{layer.name}</h4>
                  <p className="text-sm text-muted-foreground">Available to: {layer.users}</p>
                </div>
              </div>
              <input type="checkbox" checked={layer.enabled} readOnly className="w-5 h-5" />
            </div>
          ))}
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <h3 className="mb-4">Custom Overlays</h3>
        <button className="w-full px-4 py-2 border-2 border-dashed border-border rounded-lg hover:bg-accent transition-colors text-muted-foreground">
          + Add Custom Overlay
        </button>
      </div>
    </div>
  );
}

export function Settings() {
  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">Platform Settings</h2>
        <p className="text-muted-foreground">Configure global platform settings</p>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <h3 className="mb-4">General Settings</h3>
        <div className="space-y-6">
          <div>
            <label className="block text-sm mb-2">Platform Name</label>
            <input type="text" defaultValue="Maps Platform" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" />
          </div>
          <div>
            <label className="block text-sm mb-2">Support Email</label>
            <input type="email" defaultValue="support@mapsplatform.com" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" />
          </div>
          <div>
            <label className="block text-sm mb-2">Default Map Center</label>
            <div className="grid grid-cols-2 gap-4">
              <input type="text" placeholder="Latitude" defaultValue="40.7128" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" />
              <input type="text" placeholder="Longitude" defaultValue="-74.0060" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" />
            </div>
          </div>
          <div className="flex items-center justify-between p-4 bg-accent/30 rounded-lg">
            <div>
              <h4>Maintenance Mode</h4>
              <p className="text-sm text-muted-foreground">Temporarily disable user access</p>
            </div>
            <input type="checkbox" className="w-5 h-5" />
          </div>
          <div className="flex items-center justify-between p-4 bg-accent/30 rounded-lg">
            <div>
              <h4>Enable User Registration</h4>
              <p className="text-sm text-muted-foreground">Allow new user signups</p>
            </div>
            <input type="checkbox" defaultChecked className="w-5 h-5" />
          </div>
          <button className="w-full px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90">
            Save Changes
          </button>
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg p-6">
        <h3 className="mb-4">API Configuration</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm mb-2">Maps API Key</label>
            <input type="password" defaultValue="sk_live_********************************" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" />
          </div>
          <div>
            <label className="block text-sm mb-2">Rate Limit (requests/minute)</label>
            <input type="number" defaultValue="1000" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" />
          </div>
        </div>
      </div>
    </div>
  );
}
