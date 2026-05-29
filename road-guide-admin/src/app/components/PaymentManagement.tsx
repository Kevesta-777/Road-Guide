import { useState } from "react";
import { DollarSign, TrendingUp, CreditCard, Building2, Megaphone, Crown, Users, Calendar, Download, Filter, Search, CheckCircle, XCircle, Clock, RefreshCw, ArrowUpRight, ArrowDownRight } from "lucide-react";
import { useAdmin, useAdminCollection } from "../hooks/useAdminResource";

type RevenueStream = {
  monthly: number;
  growth: number;
  users?: number;
  campaigns?: number;
  avgRevenue: number;
  color: string;
};

type RevenueStreams = {
  premiumUsers: RevenueStream;
  businessUsers: RevenueStream;
  advertising: RevenueStream;
};

type MonthlyRevenue = { month: string; premium: number; business: number; ads: number };

type BusinessPricing = { id: number; tier: string; price: number; features: string; users: number; active: boolean };

type PaymentsOverview = {
  revenueStreams: RevenueStreams;
  monthlyRevenue: MonthlyRevenue[];
  businessPricing: BusinessPricing[];
};

type Transaction = {
  id: number;
  type: string;
  user: string;
  amount: number;
  status: string;
  date: string;
  method: string;
  category: string;
};

const fallbackRevenueStreams: RevenueStreams = {
  premiumUsers: {
    monthly: 263490,
    growth: 12.5,
    users: 26349,
    avgRevenue: 9.99,
    color: "text-yellow-600"
  },
  businessUsers: {
    monthly: 89250,
    growth: 18.3,
    users: 2127,
    avgRevenue: 42.00,
    color: "text-purple-600"
  },
  advertising: {
    monthly: 89234,
    growth: 23.1,
    campaigns: 47,
    avgRevenue: 1899.00,
    color: "text-blue-600"
  }
};

const fallbackRecentTransactions: Transaction[] = [
  { id: 1, type: "Premium Subscription", user: "John Smith", amount: 9.99, status: "Completed", date: "2024-05-13 10:30", method: "Credit Card", category: "premium" },
  { id: 2, type: "Business Account", user: "Joe's Coffee Shop", amount: 42.00, status: "Completed", date: "2024-05-13 09:15", method: "PayPal", category: "business" },
  { id: 3, type: "Ad Campaign", user: "TechMart Store", amount: 500.00, status: "Completed", date: "2024-05-13 08:45", method: "Credit Card", category: "ads" },
  { id: 4, type: "Premium Subscription", user: "Emma Wilson", amount: 9.99, status: "Pending", date: "2024-05-13 08:20", method: "Credit Card", category: "premium" },
  { id: 5, type: "Business Renewal", user: "Bella Restaurant", amount: 42.00, status: "Completed", date: "2024-05-12 18:30", method: "Bank Transfer", category: "business" },
  { id: 6, type: "Premium Subscription", user: "Mike Chen", amount: 9.99, status: "Refunded", date: "2024-05-12 16:45", method: "Credit Card", category: "premium" },
  { id: 7, type: "Sponsored Location", user: "FitPro Gym", amount: 750.00, status: "Completed", date: "2024-05-12 14:20", method: "Credit Card", category: "ads" },
  { id: 8, type: "Premium Subscription", user: "Anna Lee", amount: 9.99, status: "Completed", date: "2024-05-12 11:10", method: "Google Pay", category: "premium" },
  { id: 9, type: "Business Account", user: "Green Grocery", amount: 42.00, status: "Failed", date: "2024-05-12 09:30", method: "Credit Card", category: "business" },
  { id: 10, type: "Ad Campaign", user: "Joe's Coffee Shop", amount: 300.00, status: "Completed", date: "2024-05-11 15:45", method: "PayPal", category: "ads" },
];

const fallbackMonthlyRevenue: MonthlyRevenue[] = [
  { month: "Jan", premium: 245000, business: 76000, ads: 68000 },
  { month: "Feb", premium: 251000, business: 79500, ads: 72000 },
  { month: "Mar", premium: 256000, business: 82000, ads: 78000 },
  { month: "Apr", premium: 259000, business: 85500, ads: 82000 },
  { month: "May", premium: 263490, business: 89250, ads: 89234 },
];

const fallbackBusinessPricing: BusinessPricing[] = [
  { id: 1, tier: "Basic", price: 29.00, features: "1 POI, Basic Analytics", users: 856, active: true },
  { id: 2, tier: "Professional", price: 42.00, features: "3 POIs, Advanced Analytics, Priority Review", users: 1045, active: true },
  { id: 3, tier: "Enterprise", price: 99.00, features: "Unlimited POIs, Premium Support, API Access", users: 226, active: true },
];

export function PaymentManagement() {
  const [selectedTab, setSelectedTab] = useState<"overview" | "transactions" | "business-pricing" | "payouts">("overview");
  const [filterCategory, setFilterCategory] = useState("All");
  const [searchTerm, setSearchTerm] = useState("");
  const [editingTier, setEditingTier] = useState<number | null>(null);

  const { data: overview } = useAdmin<PaymentsOverview>("/payments/overview", {
    revenueStreams: fallbackRevenueStreams,
    monthlyRevenue: fallbackMonthlyRevenue,
    businessPricing: fallbackBusinessPricing,
  });
  const revenueStreams = overview.revenueStreams;
  const monthlyRevenue = overview.monthlyRevenue;
  const businessPricing = overview.businessPricing;
  const { data: recentTransactions } = useAdminCollection<Transaction>(
    "/payments/transactions",
    fallbackRecentTransactions,
  );

  const totalRevenue = revenueStreams.premiumUsers.monthly + revenueStreams.businessUsers.monthly + revenueStreams.advertising.monthly;
  const totalGrowth = ((revenueStreams.premiumUsers.monthly * revenueStreams.premiumUsers.growth +
                        revenueStreams.businessUsers.monthly * revenueStreams.businessUsers.growth +
                        revenueStreams.advertising.monthly * revenueStreams.advertising.growth) / totalRevenue) / 100;

  const filteredTransactions = recentTransactions.filter(tx => {
    const matchesCategory = filterCategory === "All" || tx.category === filterCategory;
    const matchesSearch = tx.user.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         tx.type.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl mb-2">Payment & Revenue Management</h2>
          <p className="text-muted-foreground">Monitor revenue streams, transactions, and business pricing</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setSelectedTab("overview")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "overview"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Overview
          </button>
          <button
            onClick={() => setSelectedTab("transactions")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "transactions"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Transactions
          </button>
          <button
            onClick={() => setSelectedTab("business-pricing")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "business-pricing"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Business Pricing
          </button>
          <button
            onClick={() => setSelectedTab("payouts")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "payouts"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Payouts
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Total Revenue</h3>
            <DollarSign className="w-5 h-5 text-green-600" />
          </div>
          <div className="text-2xl mb-1">${(totalRevenue / 1000).toFixed(1)}K</div>
          <p className="text-xs text-green-600 dark:text-green-400 flex items-center gap-1">
            <ArrowUpRight className="w-3 h-3" />
            +{totalGrowth.toFixed(1)}% this month
          </p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Premium Users</h3>
            <Crown className="w-5 h-5 text-yellow-600" />
          </div>
          <div className="text-2xl mb-1">${(revenueStreams.premiumUsers.monthly / 1000).toFixed(1)}K</div>
          <p className="text-xs text-muted-foreground">{(revenueStreams.premiumUsers.users ?? 0).toLocaleString()} subscribers</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Business Users</h3>
            <Building2 className="w-5 h-5 text-purple-600" />
          </div>
          <div className="text-2xl mb-1">${(revenueStreams.businessUsers.monthly / 1000).toFixed(1)}K</div>
          <p className="text-xs text-muted-foreground">{(revenueStreams.businessUsers.users ?? 0).toLocaleString()} accounts</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Advertising</h3>
            <Megaphone className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl mb-1">${(revenueStreams.advertising.monthly / 1000).toFixed(1)}K</div>
          <p className="text-xs text-muted-foreground">{revenueStreams.advertising.campaigns} campaigns</p>
        </div>
      </div>

      {selectedTab === "overview" && (
        <>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 bg-card border border-border rounded-lg p-6">
              <h3 className="mb-6">Revenue Breakdown</h3>
              <div className="space-y-6">
                {monthlyRevenue.map((data) => {
                  const total = data.premium + data.business + data.ads;
                  return (
                    <div key={data.month}>
                      <div className="flex justify-between mb-2">
                        <span className="text-sm font-medium">{data.month} 2024</span>
                        <span className="text-sm font-medium">${(total / 1000).toFixed(1)}K</span>
                      </div>
                      <div className="w-full h-10 bg-secondary rounded-full overflow-hidden flex">
                        <div
                          className="bg-yellow-600 flex items-center justify-center text-xs text-white"
                          style={{ width: `${(data.premium / total) * 100}%` }}
                          title={`Premium: $${(data.premium / 1000).toFixed(1)}K`}
                        >
                          {((data.premium / total) * 100).toFixed(0)}%
                        </div>
                        <div
                          className="bg-purple-600 flex items-center justify-center text-xs text-white"
                          style={{ width: `${(data.business / total) * 100}%` }}
                          title={`Business: $${(data.business / 1000).toFixed(1)}K`}
                        >
                          {((data.business / total) * 100).toFixed(0)}%
                        </div>
                        <div
                          className="bg-blue-600 flex items-center justify-center text-xs text-white"
                          style={{ width: `${(data.ads / total) * 100}%` }}
                          title={`Ads: $${(data.ads / 1000).toFixed(1)}K`}
                        >
                          {((data.ads / total) * 100).toFixed(0)}%
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>

              <div className="mt-6 pt-6 border-t border-border">
                <div className="flex items-center gap-6">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 bg-yellow-600 rounded"></div>
                    <span className="text-sm">Premium Users</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 bg-purple-600 rounded"></div>
                    <span className="text-sm">Business Users</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 bg-blue-600 rounded"></div>
                    <span className="text-sm">Advertising</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="space-y-6">
              <div className="bg-card border border-border rounded-lg p-6">
                <h3 className="mb-4">Revenue Sources</h3>
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-4 bg-yellow-50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-800 rounded-lg">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <Crown className="w-4 h-4 text-yellow-600" />
                        <span className="text-sm font-medium">Premium Users</span>
                      </div>
                      <p className="text-2xl font-bold text-yellow-700 dark:text-yellow-400">
                        ${(revenueStreams.premiumUsers.monthly / 1000).toFixed(1)}K
                      </p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {((revenueStreams.premiumUsers.monthly / totalRevenue) * 100).toFixed(0)}% of total
                      </p>
                    </div>
                    <div className="text-right">
                      <div className="text-xs text-green-600 dark:text-green-400 flex items-center gap-1">
                        <ArrowUpRight className="w-3 h-3" />
                        +{revenueStreams.premiumUsers.growth}%
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center justify-between p-4 bg-purple-50 dark:bg-purple-900/10 border border-purple-200 dark:border-purple-800 rounded-lg">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <Building2 className="w-4 h-4 text-purple-600" />
                        <span className="text-sm font-medium">Business Users</span>
                      </div>
                      <p className="text-2xl font-bold text-purple-700 dark:text-purple-400">
                        ${(revenueStreams.businessUsers.monthly / 1000).toFixed(1)}K
                      </p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {((revenueStreams.businessUsers.monthly / totalRevenue) * 100).toFixed(0)}% of total
                      </p>
                    </div>
                    <div className="text-right">
                      <div className="text-xs text-green-600 dark:text-green-400 flex items-center gap-1">
                        <ArrowUpRight className="w-3 h-3" />
                        +{revenueStreams.businessUsers.growth}%
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center justify-between p-4 bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <Megaphone className="w-4 h-4 text-blue-600" />
                        <span className="text-sm font-medium">Advertising</span>
                      </div>
                      <p className="text-2xl font-bold text-blue-700 dark:text-blue-400">
                        ${(revenueStreams.advertising.monthly / 1000).toFixed(1)}K
                      </p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {((revenueStreams.advertising.monthly / totalRevenue) * 100).toFixed(0)}% of total
                      </p>
                    </div>
                    <div className="text-right">
                      <div className="text-xs text-green-600 dark:text-green-400 flex items-center gap-1">
                        <ArrowUpRight className="w-3 h-3" />
                        +{revenueStreams.advertising.growth}%
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="bg-card border border-border rounded-lg p-6">
                <h3 className="mb-4">Quick Stats</h3>
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">Annual Run Rate</span>
                    <span className="font-medium">${(totalRevenue * 12 / 1000000).toFixed(2)}M</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">Avg Transaction</span>
                    <span className="font-medium">$48.23</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">Success Rate</span>
                    <span className="font-medium text-green-600">96.8%</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">Refund Rate</span>
                    <span className="font-medium text-red-600">1.2%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {selectedTab === "transactions" && (
        <>
          <div className="bg-card border border-border rounded-lg">
            <div className="p-4 border-b border-border">
              <div className="flex flex-col lg:flex-row gap-3">
                <div className="flex-1 relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                  <input
                    type="text"
                    placeholder="Search transactions by user or type..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div className="flex gap-2">
                  <select
                    value={filterCategory}
                    onChange={(e) => setFilterCategory(e.target.value)}
                    className="px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                  >
                    <option>All</option>
                    <option value="premium">Premium</option>
                    <option value="business">Business</option>
                    <option value="ads">Advertising</option>
                  </select>
                  <button className="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-2">
                    <Filter className="w-4 h-4" />
                    Filters
                  </button>
                  <button className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2">
                    <Download className="w-4 h-4" />
                    Export
                  </button>
                </div>
              </div>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Type</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">User/Business</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Amount</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Method</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Status</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Date</th>
                    <th className="text-right py-3 px-4 text-sm text-muted-foreground">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTransactions.map((tx) => (
                    <tr key={tx.id} className="border-b border-border hover:bg-accent/50 transition-colors">
                      <td className="py-4 px-4">
                        <div className="flex items-center gap-2">
                          {tx.category === "premium" && <Crown className="w-4 h-4 text-yellow-600" />}
                          {tx.category === "business" && <Building2 className="w-4 h-4 text-purple-600" />}
                          {tx.category === "ads" && <Megaphone className="w-4 h-4 text-blue-600" />}
                          <span className="text-sm">{tx.type}</span>
                        </div>
                      </td>
                      <td className="py-4 px-4 text-sm">{tx.user}</td>
                      <td className="py-4 px-4">
                        <span className="font-medium">${tx.amount.toFixed(2)}</span>
                      </td>
                      <td className="py-4 px-4 text-sm">{tx.method}</td>
                      <td className="py-4 px-4">
                        <span className={`px-3 py-1 rounded-full text-sm flex items-center gap-1 w-fit ${
                          tx.status === "Completed"
                            ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                            : tx.status === "Pending"
                            ? "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                            : tx.status === "Failed"
                            ? "bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300"
                            : "bg-gray-100 dark:bg-gray-900/30 text-gray-800 dark:text-gray-300"
                        }`}>
                          {tx.status === "Completed" && <CheckCircle className="w-3 h-3" />}
                          {tx.status === "Pending" && <Clock className="w-3 h-3" />}
                          {tx.status === "Failed" && <XCircle className="w-3 h-3" />}
                          {tx.status === "Refunded" && <RefreshCw className="w-3 h-3" />}
                          {tx.status}
                        </span>
                      </td>
                      <td className="py-4 px-4 text-sm text-muted-foreground">{tx.date}</td>
                      <td className="py-4 px-4">
                        <div className="flex items-center justify-end gap-2">
                          <button className="px-3 py-1 border border-border rounded hover:bg-accent transition-colors text-sm">
                            View Details
                          </button>
                          {tx.status === "Completed" && (
                            <button className="px-3 py-1 border border-border rounded hover:bg-accent transition-colors text-sm">
                              Refund
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="p-4 border-t border-border flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                Showing {filteredTransactions.length} of {recentTransactions.length} transactions
              </p>
              <div className="flex gap-2">
                <button className="px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                  Previous
                </button>
                <button className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">
                  Next
                </button>
              </div>
            </div>
          </div>
        </>
      )}

      {selectedTab === "business-pricing" && (
        <>
          <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4 mb-6">
            <div className="flex items-start gap-3">
              <Building2 className="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5" />
              <div>
                <h4 className="text-blue-900 dark:text-blue-100 mb-1">Business Account Pricing</h4>
                <p className="text-sm text-blue-700 dark:text-blue-300">
                  Business users pay monthly subscriptions based on their tier. Higher tiers include more POIs, advanced features, and priority support.
                </p>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {businessPricing.map((tier) => (
              <div key={tier.id} className="bg-card border border-border rounded-lg overflow-hidden">
                <div className="bg-purple-50 dark:bg-purple-900/20 p-6 border-b border-border">
                  <h3 className="text-xl mb-2">{tier.tier}</h3>
                  <div className="flex items-baseline gap-2 mb-4">
                    <span className="text-3xl font-bold">${tier.price.toFixed(2)}</span>
                    <span className="text-muted-foreground">/month</span>
                    <button
                      onClick={() => setEditingTier(tier.id)}
                      className="ml-2 p-1.5 hover:bg-purple-100 dark:hover:bg-purple-900/40 rounded-lg transition-colors"
                    >
                      <CreditCard className="w-4 h-4" />
                    </button>
                  </div>
                  <p className="text-sm text-muted-foreground">{tier.features}</p>
                </div>

                <div className="p-6">
                  <div className="space-y-4">
                    <div className="flex justify-between">
                      <span className="text-sm text-muted-foreground">Active Users</span>
                      <span className="font-medium">{tier.users.toLocaleString()}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-muted-foreground">Monthly Revenue</span>
                      <span className="font-medium">${(tier.users * tier.price / 1000).toFixed(1)}K</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-muted-foreground">Annual Value</span>
                      <span className="font-medium">${(tier.users * tier.price * 12 / 1000).toFixed(1)}K</span>
                    </div>
                  </div>

                  <div className="mt-6 pt-4 border-t border-border">
                    <span className={`px-3 py-1 rounded-full text-sm ${
                      tier.active
                        ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                        : "bg-gray-100 dark:bg-gray-900/30 text-gray-800 dark:text-gray-300"
                    }`}>
                      {tier.active ? "Active" : "Inactive"}
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Tier Distribution</h3>
              <div className="space-y-4">
                {businessPricing.map((tier) => {
                  const totalUsers = businessPricing.reduce((sum, t) => sum + t.users, 0);
                  const percentage = (tier.users / totalUsers) * 100;
                  return (
                    <div key={tier.id}>
                      <div className="flex justify-between mb-2">
                        <span className="text-sm">{tier.tier}</span>
                        <span className="text-sm font-medium">{tier.users.toLocaleString()} ({percentage.toFixed(0)}%)</span>
                      </div>
                      <div className="w-full bg-secondary h-3 rounded-full">
                        <div
                          className="bg-purple-600 h-3 rounded-full"
                          style={{ width: `${percentage}%` }}
                        ></div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Pricing Insights</h3>
              <div className="space-y-4">
                <div className="p-4 bg-accent/30 rounded-lg">
                  <div className="flex items-center gap-2 mb-1">
                    <TrendingUp className="w-4 h-4 text-green-600" />
                    <span className="text-sm font-medium">Upsell Opportunity</span>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    34% of Basic users have 2+ POIs. Consider suggesting Professional tier.
                  </p>
                </div>

                <div className="p-4 bg-accent/30 rounded-lg">
                  <div className="flex items-center gap-2 mb-1">
                    <DollarSign className="w-4 h-4 text-blue-600" />
                    <span className="text-sm font-medium">Average Revenue</span>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    Current ARPU: ${(revenueStreams.businessUsers.avgRevenue).toFixed(2)}/month across all tiers
                  </p>
                </div>

                <div className="p-4 bg-accent/30 rounded-lg">
                  <div className="flex items-center gap-2 mb-1">
                    <Users className="w-4 h-4 text-purple-600" />
                    <span className="text-sm font-medium">Churn Rate</span>
                  </div>
                  <p className="text-xs text-muted-foreground">
                    Business churn: 3.2% (lower than industry avg of 5%)
                  </p>
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {selectedTab === "payouts" && (
        <>
          <div className="bg-card border border-border rounded-lg p-6">
            <h3 className="mb-6">Payment Processing & Payouts</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="p-6 bg-green-50 dark:bg-green-900/10 border border-green-200 dark:border-green-800 rounded-lg">
                <div className="flex items-center gap-2 mb-2">
                  <CheckCircle className="w-5 h-5 text-green-600" />
                  <h4>Successful Payments</h4>
                </div>
                <div className="text-3xl font-bold mb-1">96.8%</div>
                <p className="text-sm text-muted-foreground">8,920 transactions</p>
              </div>

              <div className="p-6 bg-yellow-50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-800 rounded-lg">
                <div className="flex items-center gap-2 mb-2">
                  <Clock className="w-5 h-5 text-yellow-600" />
                  <h4>Pending</h4>
                </div>
                <div className="text-3xl font-bold mb-1">1.8%</div>
                <p className="text-sm text-muted-foreground">165 transactions</p>
              </div>

              <div className="p-6 bg-red-50 dark:bg-red-900/10 border border-red-200 dark:border-red-800 rounded-lg">
                <div className="flex items-center gap-2 mb-2">
                  <XCircle className="w-5 h-5 text-red-600" />
                  <h4>Failed/Refunded</h4>
                </div>
                <div className="text-3xl font-bold mb-1">1.4%</div>
                <p className="text-sm text-muted-foreground">129 transactions</p>
              </div>
            </div>

            <div className="mt-6 pt-6 border-t border-border">
              <h4 className="mb-4">Payment Methods</h4>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {[
                  { method: "Credit Card", percentage: 65, count: 5992 },
                  { method: "PayPal", percentage: 20, count: 1845 },
                  { method: "Google Pay", percentage: 10, count: 922 },
                  { method: "Bank Transfer", percentage: 5, count: 461 },
                ].map((pm) => (
                  <div key={pm.method} className="p-4 bg-accent/30 rounded-lg">
                    <p className="text-sm text-muted-foreground mb-1">{pm.method}</p>
                    <p className="text-xl font-bold mb-1">{pm.percentage}%</p>
                    <p className="text-xs text-muted-foreground">{pm.count.toLocaleString()} transactions</p>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="bg-card border border-border rounded-lg p-6">
            <h3 className="mb-4">Scheduled Payouts</h3>
            <div className="space-y-3">
              {[
                { date: "May 15, 2024", amount: 442974, status: "Scheduled" },
                { date: "May 1, 2024", amount: 435820, status: "Completed" },
                { date: "April 15, 2024", amount: 426500, status: "Completed" },
              ].map((payout, idx) => (
                <div key={idx} className="flex items-center justify-between p-4 border border-border rounded-lg">
                  <div className="flex items-center gap-3">
                    <Calendar className="w-5 h-5 text-blue-600" />
                    <div>
                      <p className="font-medium">{payout.date}</p>
                      <p className="text-sm text-muted-foreground">${payout.amount.toLocaleString()}</p>
                    </div>
                  </div>
                  <span className={`px-3 py-1 rounded-full text-sm ${
                    payout.status === "Completed"
                      ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                      : "bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300"
                  }`}>
                    {payout.status}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
