import { useState } from "react";
import { Search, Filter, MoreVertical, Ban, CheckCircle, XCircle, Mail, UserCog, Users, Building2 } from "lucide-react";
import { useAdminCollection } from "../hooks/useAdminResource";

type ConsumerUser = {
  id: number;
  name: string;
  email: string;
  status: string;
  joined: string;
  lastActive: string;
  friends: number;
};

type BusinessUser = {
  id: number;
  name: string;
  email: string;
  businessName: string;
  status: string;
  joined: string;
  lastActive: string;
  pois: number;
  verified: boolean;
};

const fallbackConsumerUsers: ConsumerUser[] = [
  { id: 1, name: "John Smith", email: "john@example.com", status: "Active", joined: "2024-01-15", lastActive: "2 hours ago", friends: 23 },
  { id: 2, name: "Mike Chen", email: "mike@example.com", status: "Suspended", joined: "2024-01-08", lastActive: "1 week ago", friends: 12 },
  { id: 3, name: "David Brown", email: "david@example.com", status: "Active", joined: "2024-03-12", lastActive: "3 hours ago", friends: 67 },
  { id: 4, name: "Anna Lee", email: "anna@example.com", status: "Active", joined: "2024-02-18", lastActive: "15 min ago", friends: 34 },
  { id: 5, name: "Tom Wilson", email: "tom@example.com", status: "Active", joined: "2024-04-22", lastActive: "1 hour ago", friends: 56 },
];

const fallbackBusinessUsers: BusinessUser[] = [
  { id: 1, name: "Sarah Johnson", email: "sarah@example.com", businessName: "Joe's Coffee Shop", status: "Active", joined: "2024-02-20", lastActive: "1 day ago", pois: 3, verified: true },
  { id: 2, name: "Emma Wilson", email: "emma@example.com", businessName: "TechMart Store", status: "Pending", joined: "2024-05-10", lastActive: "5 min ago", pois: 1, verified: false },
  { id: 3, name: "Lisa Anderson", email: "lisa@example.com", businessName: "Bella Restaurant", status: "Active", joined: "2024-04-05", lastActive: "30 min ago", pois: 2, verified: true },
  { id: 4, name: "James Brown", email: "james@example.com", businessName: "FitPro Gym", status: "Pending", joined: "2024-05-12", lastActive: "2 hours ago", pois: 0, verified: false },
  { id: 5, name: "Maria Garcia", email: "maria@example.com", businessName: "Quick Auto Repair", status: "Rejected", joined: "2024-05-08", lastActive: "3 days ago", pois: 0, verified: false },
];

export function UserManagement() {
  const [activeTab, setActiveTab] = useState<"consumer" | "business">("consumer");
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("All");
  const [selectedUser, setSelectedUser] = useState<number | null>(null);

  const { data: mockConsumerUsers } = useAdminCollection<ConsumerUser>(
    "/users?type=consumer",
    fallbackConsumerUsers,
  );
  const { data: mockBusinessUsers } = useAdminCollection<BusinessUser>(
    "/users?type=business",
    fallbackBusinessUsers,
  );

  const filteredConsumerUsers = mockConsumerUsers.filter(user => {
    const matchesSearch = user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         user.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesFilter = filterStatus === "All" || user.status === filterStatus;
    return matchesSearch && matchesFilter;
  });

  const filteredBusinessUsers = mockBusinessUsers.filter(user => {
    const matchesSearch = user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         user.businessName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesFilter = filterStatus === "All" || user.status === filterStatus;
    return matchesSearch && matchesFilter;
  });

  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">User Management</h2>
        <p className="text-muted-foreground">Manage consumer and business accounts</p>
      </div>

      <div className="flex gap-2 mb-6">
        <button
          onClick={() => setActiveTab("consumer")}
          className={`flex items-center gap-2 px-6 py-3 rounded-lg transition-colors ${
            activeTab === "consumer"
              ? "bg-primary text-primary-foreground"
              : "bg-card border border-border hover:bg-accent"
          }`}
        >
          <Users className="w-5 h-5" />
          Consumer Users
          <span className={`px-2 py-0.5 rounded-full text-xs ${
            activeTab === "consumer"
              ? "bg-primary-foreground/20 text-primary-foreground"
              : "bg-secondary text-secondary-foreground"
          }`}>
            {mockConsumerUsers.length}
          </span>
        </button>
        <button
          onClick={() => setActiveTab("business")}
          className={`flex items-center gap-2 px-6 py-3 rounded-lg transition-colors ${
            activeTab === "business"
              ? "bg-primary text-primary-foreground"
              : "bg-card border border-border hover:bg-accent"
          }`}
        >
          <Building2 className="w-5 h-5" />
          Business Users
          <span className={`px-2 py-0.5 rounded-full text-xs ${
            activeTab === "business"
              ? "bg-primary-foreground/20 text-primary-foreground"
              : "bg-secondary text-secondary-foreground"
          }`}>
            {mockBusinessUsers.length}
          </span>
        </button>
      </div>

      {activeTab === "consumer" && (
        <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4 mb-6">
          <div className="flex items-start gap-3">
            <CheckCircle className="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5" />
            <div>
              <h4 className="text-blue-900 dark:text-blue-100 mb-1">Self-Registration Enabled</h4>
              <p className="text-sm text-blue-700 dark:text-blue-300">Consumer users can sign up themselves through the app. No admin approval required.</p>
            </div>
          </div>
        </div>
      )}

      {activeTab === "business" && (
        <div className="bg-yellow-50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4 mb-6">
          <div className="flex items-start gap-3">
            <XCircle className="w-5 h-5 text-yellow-600 dark:text-yellow-400 mt-0.5" />
            <div>
              <h4 className="text-yellow-900 dark:text-yellow-100 mb-1">Admin Approval Required</h4>
              <p className="text-sm text-yellow-700 dark:text-yellow-300">Business accounts must be approved by an administrator before they can access the platform.</p>
            </div>
          </div>
        </div>
      )}

      <div className="bg-card border border-border rounded-lg p-6">
        <div className="flex flex-col sm:flex-row gap-4 mb-6">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <input
              type="text"
              placeholder={activeTab === "consumer" ? "Search users by name or email..." : "Search by name, email, or business name..."}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
            />
          </div>
          <div className="flex gap-2">
            <button className="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-2">
              <Filter className="w-4 h-4" />
              Filters
            </button>
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option>All</option>
              <option>Active</option>
              {activeTab === "business" && <option>Pending</option>}
              {activeTab === "business" && <option>Rejected</option>}
              <option>Suspended</option>
            </select>
          </div>
        </div>

        {activeTab === "consumer" && (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">User</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Status</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Joined</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Last Active</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Friends</th>
                    <th className="text-right py-3 px-4 text-sm text-muted-foreground">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredConsumerUsers.map((user) => (
                    <tr key={user.id} className="border-b border-border hover:bg-accent/50 transition-colors">
                      <td className="py-4 px-4">
                        <div>
                          <div className="font-medium">{user.name}</div>
                          <div className="text-sm text-muted-foreground">{user.email}</div>
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <span className={`px-3 py-1 rounded-full text-sm flex items-center gap-1 w-fit ${
                          user.status === "Active"
                            ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                            : "bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300"
                        }`}>
                          {user.status === "Active" && <CheckCircle className="w-3 h-3" />}
                          {user.status === "Suspended" && <Ban className="w-3 h-3" />}
                          {user.status}
                        </span>
                      </td>
                      <td className="py-4 px-4 text-sm">{user.joined}</td>
                      <td className="py-4 px-4 text-sm text-muted-foreground">{user.lastActive}</td>
                      <td className="py-4 px-4 text-sm">{user.friends}</td>
                      <td className="py-4 px-4">
                        <div className="flex items-center justify-end gap-2">
                          <button className="p-2 hover:bg-accent rounded-lg transition-colors" title="Send Email">
                            <Mail className="w-4 h-4" />
                          </button>
                          <button className="p-2 hover:bg-accent rounded-lg transition-colors" title="Manage User">
                            <UserCog className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => setSelectedUser(selectedUser === user.id ? null : user.id)}
                            className="p-2 hover:bg-accent rounded-lg transition-colors"
                            title="More Options"
                          >
                            <MoreVertical className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="flex items-center justify-between mt-6 pt-4 border-t border-border">
              <p className="text-sm text-muted-foreground">Showing {filteredConsumerUsers.length} of {mockConsumerUsers.length} consumer users</p>
              <div className="flex gap-2">
                <button className="px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">Previous</button>
                <button className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">Next</button>
              </div>
            </div>
          </>
        )}

        {activeTab === "business" && (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Business Owner</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Business Name</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Status</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">POIs</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Verified</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Joined</th>
                    <th className="text-right py-3 px-4 text-sm text-muted-foreground">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredBusinessUsers.map((user) => (
                    <tr key={user.id} className="border-b border-border hover:bg-accent/50 transition-colors">
                      <td className="py-4 px-4">
                        <div>
                          <div className="font-medium">{user.name}</div>
                          <div className="text-sm text-muted-foreground">{user.email}</div>
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex items-center gap-2">
                          <Building2 className="w-4 h-4 text-purple-600" />
                          <span>{user.businessName}</span>
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <span className={`px-3 py-1 rounded-full text-sm flex items-center gap-1 w-fit ${
                          user.status === "Active"
                            ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                            : user.status === "Pending"
                            ? "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                            : "bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300"
                        }`}>
                          {user.status === "Active" && <CheckCircle className="w-3 h-3" />}
                          {user.status === "Rejected" && <XCircle className="w-3 h-3" />}
                          {user.status}
                        </span>
                      </td>
                      <td className="py-4 px-4 text-sm">{user.pois}</td>
                      <td className="py-4 px-4">
                        {user.verified ? (
                          <span className="flex items-center gap-1 text-green-600 dark:text-green-400">
                            <CheckCircle className="w-4 h-4" />
                            <span className="text-sm">Verified</span>
                          </span>
                        ) : (
                          <span className="text-sm text-muted-foreground">Not verified</span>
                        )}
                      </td>
                      <td className="py-4 px-4 text-sm">{user.joined}</td>
                      <td className="py-4 px-4">
                        <div className="flex items-center justify-end gap-2">
                          {user.status === "Pending" ? (
                            <>
                              <button className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700 text-sm">
                                Approve
                              </button>
                              <button className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 text-sm">
                                Reject
                              </button>
                            </>
                          ) : (
                            <>
                              <button className="p-2 hover:bg-accent rounded-lg transition-colors" title="Send Email">
                                <Mail className="w-4 h-4" />
                              </button>
                              <button className="p-2 hover:bg-accent rounded-lg transition-colors" title="Manage User">
                                <UserCog className="w-4 h-4" />
                              </button>
                              <button
                                onClick={() => setSelectedUser(selectedUser === user.id ? null : user.id)}
                                className="p-2 hover:bg-accent rounded-lg transition-colors"
                                title="More Options"
                              >
                                <MoreVertical className="w-4 h-4" />
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="flex items-center justify-between mt-6 pt-4 border-t border-border">
              <p className="text-sm text-muted-foreground">Showing {filteredBusinessUsers.length} of {mockBusinessUsers.length} business users</p>
              <div className="flex gap-2">
                <button className="px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">Previous</button>
                <button className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">Next</button>
              </div>
            </div>
          </>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {activeTab === "consumer" ? (
          <>
            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Consumer Statistics</h3>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Total Consumers</span>
                  <span>98,234</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Active Today</span>
                  <span>38,456</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">New This Week</span>
                  <span className="text-green-600 dark:text-green-400">+1,234</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Suspended</span>
                  <span className="text-red-600 dark:text-red-400">234</span>
                </div>
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Engagement Metrics</h3>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Avg Friends</span>
                  <span>34.5</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Daily Active</span>
                  <span>67.2%</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Weekly Active</span>
                  <span>89.4%</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Monthly Active</span>
                  <span>95.8%</span>
                </div>
              </div>
            </div>
          </>
        ) : (
          <>
            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Business Statistics</h3>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Total Businesses</span>
                  <span>2,345</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Active</span>
                  <span className="text-green-600 dark:text-green-400">2,127</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Pending Approval</span>
                  <span className="text-yellow-600 dark:text-yellow-400">156</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Rejected</span>
                  <span className="text-red-600 dark:text-red-400">62</span>
                </div>
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Verification Status</h3>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Verified</span>
                  <span className="text-green-600 dark:text-green-400">1,892</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Not Verified</span>
                  <span>453</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Avg POIs per Business</span>
                  <span>3.2</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Total Business POIs</span>
                  <span>7,504</span>
                </div>
              </div>
            </div>
          </>
        )}

        <div className="bg-card border border-border rounded-lg p-6">
          <h3 className="mb-4">Quick Actions</h3>
          <div className="space-y-2">
            <button className="w-full px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">
              Export User Data
            </button>
            <button className="w-full px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
              Send Bulk Email
            </button>
            <button className="w-full px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
              Generate Report
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
