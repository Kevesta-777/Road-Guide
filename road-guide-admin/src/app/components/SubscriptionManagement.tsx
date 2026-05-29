import { useState } from "react";
import { Crown, Users, DollarSign, TrendingUp, Edit, Save, X, Check, Sparkles, Zap, Star, Shield } from "lucide-react";
import { useAdminCollection } from "../hooks/useAdminResource";

type PlanFeature = { name: string; included: boolean };

type SubscriptionPlan = {
  id: number;
  name: string;
  type: string;
  price: number;
  billingPeriod: string;
  users: number;
  description: string;
  features: PlanFeature[];
};

type PremiumContentItem = {
  id: number;
  name: string;
  category: string;
  enabled: boolean;
  premiumOnly: boolean;
};

const fallbackSubscriptionPlans: SubscriptionPlan[] = [
  {
    id: 1,
    name: "Free",
    type: "free",
    price: 0,
    billingPeriod: "forever",
    users: 98234,
    description: "Basic features for casual users",
    features: [
      { name: "Basic map navigation", included: true },
      { name: "Standard POI search", included: true },
      { name: "Ad-supported experience", included: true },
      { name: "Daily game limits: 20 gold, 10 landmarks", included: true },
      { name: "Basic 360° image viewing", included: true },
      { name: "Standard navigation voice", included: true },
      { name: "Limited friend list (50 max)", included: true },
    ]
  },
  {
    id: 2,
    name: "Premium",
    type: "premium",
    price: 9.99,
    billingPeriod: "month",
    users: 26349,
    description: "Enhanced features for power users",
    features: [
      { name: "Ad-free experience", included: true },
      { name: "Advanced map layers (traffic, 3D, satellite)", included: true },
      { name: "Unlimited game plays (no daily limits)", included: true },
      { name: "Premium 360° image uploads", included: true },
      { name: "Offline map downloads", included: true },
      { name: "Premium navigation voices", included: true },
      { name: "Unlimited friend list", included: true },
      { name: "Priority POI claim reviews", included: true },
      { name: "Exclusive POI access", included: true },
      { name: "Advanced AI guide features", included: true },
      { name: "Custom map themes", included: true },
      { name: "2x coin earnings in games", included: true },
    ]
  }
];

const fallbackPremiumContent: PremiumContentItem[] = [
  { id: 1, name: "Advanced Traffic Layer", category: "Map Layer", enabled: true, premiumOnly: true },
  { id: 2, name: "3D Buildings View", category: "Map Layer", enabled: true, premiumOnly: true },
  { id: 3, name: "Satellite Imagery", category: "Map Layer", enabled: true, premiumOnly: true },
  { id: 4, name: "Offline Map Downloads", category: "Feature", enabled: true, premiumOnly: true },
  { id: 5, name: "Premium Voice Pack 1", category: "Navigation", enabled: true, premiumOnly: true },
  { id: 6, name: "Premium Voice Pack 2", category: "Navigation", enabled: true, premiumOnly: true },
  { id: 7, name: "Dark Mode Themes", category: "Customization", enabled: true, premiumOnly: true },
  { id: 8, name: "Custom POI Icons", category: "Customization", enabled: true, premiumOnly: true },
  { id: 9, name: "Exclusive Landmark Tours", category: "Content", enabled: true, premiumOnly: true },
  { id: 10, name: "Advanced AI Recommendations", category: "AI Feature", enabled: true, premiumOnly: true },
];

export function SubscriptionManagement() {
  const [editingPlan, setEditingPlan] = useState<number | null>(null);
  const [editedPrice, setEditedPrice] = useState<number>(0);
  const [selectedTab, setSelectedTab] = useState<"plans" | "content" | "analytics">("plans");

  const { data: subscriptionPlans } = useAdminCollection<SubscriptionPlan>(
    "/subscriptions/plans",
    fallbackSubscriptionPlans,
  );
  const { data: premiumContent } = useAdminCollection<PremiumContentItem>(
    "/subscriptions/premium-content",
    fallbackPremiumContent,
  );

  const totalRevenue = 26349 * 9.99;
  const conversionRate = (26349 / (98234 + 26349)) * 100;
  const monthlyGrowth = 12.5;

  const handleEditPrice = (planId: number, currentPrice: number) => {
    setEditingPlan(planId);
    setEditedPrice(currentPrice);
  };

  const handleSavePrice = () => {
    setEditingPlan(null);
  };

  const handleCancelEdit = () => {
    setEditingPlan(null);
  };

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl mb-2">Premium & Subscriptions</h2>
          <p className="text-muted-foreground">Manage subscription plans, pricing, and premium content</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setSelectedTab("plans")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "plans"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Subscription Plans
          </button>
          <button
            onClick={() => setSelectedTab("content")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "content"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Premium Content
          </button>
          <button
            onClick={() => setSelectedTab("analytics")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "analytics"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Analytics
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Premium Users</h3>
            <Crown className="w-5 h-5 text-yellow-600" />
          </div>
          <div className="text-2xl mb-1">26,349</div>
          <p className="text-xs text-green-600 dark:text-green-400">+{monthlyGrowth}% this month</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Free Users</h3>
            <Users className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl mb-1">98,234</div>
          <p className="text-xs text-muted-foreground">Standard tier</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Monthly Revenue</h3>
            <DollarSign className="w-5 h-5 text-green-600" />
          </div>
          <div className="text-2xl mb-1">${(totalRevenue / 1000).toFixed(1)}K</div>
          <p className="text-xs text-muted-foreground">Recurring income</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Conversion Rate</h3>
            <TrendingUp className="w-5 h-5 text-purple-600" />
          </div>
          <div className="text-2xl mb-1">{conversionRate.toFixed(1)}%</div>
          <p className="text-xs text-green-600 dark:text-green-400">+2.3% vs last month</p>
        </div>
      </div>

      {selectedTab === "plans" && (
        <>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {subscriptionPlans.map((plan) => (
              <div
                key={plan.id}
                className={`bg-card border rounded-lg overflow-hidden ${
                  plan.type === "premium"
                    ? "border-yellow-500 shadow-lg shadow-yellow-500/20"
                    : "border-border"
                }`}
              >
                <div className={`p-6 ${
                  plan.type === "premium"
                    ? "bg-gradient-to-br from-yellow-500 to-orange-500 text-white"
                    : "bg-accent/30"
                }`}>
                  <div className="flex items-start justify-between mb-4">
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <h3 className="text-2xl">{plan.name}</h3>
                        {plan.type === "premium" && <Crown className="w-6 h-6" />}
                      </div>
                      <p className={`text-sm ${plan.type === "premium" ? "opacity-90" : "text-muted-foreground"}`}>
                        {plan.description}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-baseline gap-2 mb-4">
                    {editingPlan === plan.id ? (
                      <div className="flex items-center gap-2">
                        <span className="text-3xl">$</span>
                        <input
                          type="number"
                          value={editedPrice}
                          onChange={(e) => setEditedPrice(parseFloat(e.target.value))}
                          className="w-24 px-3 py-2 bg-white dark:bg-gray-800 text-gray-900 dark:text-white border border-border rounded-lg text-2xl"
                          step="0.01"
                        />
                        <div className="flex gap-1">
                          <button
                            onClick={handleSavePrice}
                            className="p-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
                          >
                            <Save className="w-4 h-4" />
                          </button>
                          <button
                            onClick={handleCancelEdit}
                            className="p-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
                          >
                            <X className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    ) : (
                      <>
                        <span className="text-4xl font-bold">${plan.price.toFixed(2)}</span>
                        <span className={`text-lg ${plan.type === "premium" ? "opacity-90" : "text-muted-foreground"}`}>
                          /{plan.billingPeriod}
                        </span>
                        {plan.type === "premium" && (
                          <button
                            onClick={() => handleEditPrice(plan.id, plan.price)}
                            className="ml-2 p-1.5 bg-white/20 hover:bg-white/30 rounded-lg transition-colors"
                          >
                            <Edit className="w-4 h-4" />
                          </button>
                        )}
                      </>
                    )}
                  </div>

                  <div className="flex items-center gap-2">
                    <Users className="w-4 h-4" />
                    <span className="text-sm">{plan.users.toLocaleString()} active users</span>
                  </div>
                </div>

                <div className="p-6">
                  <h4 className="mb-4">Features</h4>
                  <div className="space-y-2.5">
                    {plan.features.map((feature, idx) => (
                      <div key={idx} className="flex items-start gap-3">
                        <Check className={`w-5 h-5 flex-shrink-0 mt-0.5 ${
                          plan.type === "premium" ? "text-yellow-600" : "text-green-600"
                        }`} />
                        <span className="text-sm">{feature.name}</span>
                      </div>
                    ))}
                  </div>

                  {plan.type === "premium" && (
                    <div className="mt-6 pt-6 border-t border-border">
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-sm text-muted-foreground">Monthly Revenue</span>
                        <span className="font-medium">${(plan.users * plan.price / 1000).toFixed(1)}K</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Annual Value</span>
                        <span className="font-medium">${(plan.users * plan.price * 12 / 1000).toFixed(1)}K</span>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
            <div className="flex items-start gap-3">
              <Sparkles className="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5" />
              <div>
                <h4 className="text-blue-900 dark:text-blue-100 mb-1">Premium Benefits</h4>
                <p className="text-sm text-blue-700 dark:text-blue-300">
                  Premium users get ad-free experience, unlimited game plays, advanced map features, offline downloads, and exclusive content.
                  They also earn 2x coins in games and get priority support. Adjust pricing to optimize conversion rate while maintaining value.
                </p>
              </div>
            </div>
          </div>
        </>
      )}

      {selectedTab === "content" && (
        <>
          <div className="bg-card border border-border rounded-lg">
            <div className="p-6 border-b border-border">
              <h3 className="mb-2">Premium-Only Features & Content</h3>
              <p className="text-sm text-muted-foreground">Manage features and content exclusive to premium subscribers</p>
            </div>

            <div className="p-6">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {premiumContent.map((content) => (
                  <div
                    key={content.id}
                    className="border border-border rounded-lg p-4 hover:border-yellow-500/50 transition-all"
                  >
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <Crown className="w-4 h-4 text-yellow-600" />
                          <h4 className="text-sm font-medium">{content.name}</h4>
                        </div>
                        <span className="text-xs px-2 py-1 bg-secondary text-secondary-foreground rounded">
                          {content.category}
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center justify-between mt-4 pt-4 border-t border-border">
                      <span className="text-xs text-muted-foreground">Status</span>
                      <label className="relative inline-flex items-center cursor-pointer">
                        <input
                          type="checkbox"
                          checked={content.enabled}
                          className="sr-only peer"
                          readOnly
                        />
                        <div className="w-11 h-6 bg-gray-200 dark:bg-gray-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-green-600"></div>
                      </label>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-4">Content by Category</h4>
              <div className="space-y-3">
                {[
                  { name: "Map Layers", count: 3, icon: "🗺️" },
                  { name: "Navigation", count: 2, icon: "🧭" },
                  { name: "Customization", count: 2, icon: "🎨" },
                  { name: "AI Features", count: 1, icon: "🤖" },
                  { name: "Features", count: 1, icon: "⚡" },
                  { name: "Content", count: 1, icon: "📍" },
                ].map((cat) => (
                  <div key={cat.name} className="flex items-center justify-between p-3 bg-accent/30 rounded-lg">
                    <div className="flex items-center gap-2">
                      <span className="text-xl">{cat.icon}</span>
                      <span className="text-sm">{cat.name}</span>
                    </div>
                    <span className="font-medium">{cat.count}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-4">Most Used Premium Features</h4>
              <div className="space-y-3">
                {[
                  { name: "Ad-Free Experience", usage: 95 },
                  { name: "Offline Downloads", usage: 78 },
                  { name: "3D Buildings", usage: 72 },
                  { name: "Premium Voices", usage: 65 },
                  { name: "Custom Themes", usage: 58 },
                ].map((feature) => (
                  <div key={feature.name}>
                    <div className="flex justify-between mb-1 text-sm">
                      <span>{feature.name}</span>
                      <span className="text-muted-foreground">{feature.usage}%</span>
                    </div>
                    <div className="w-full bg-secondary h-2 rounded-full">
                      <div
                        className="bg-yellow-600 h-2 rounded-full"
                        style={{ width: `${feature.usage}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-4">Quick Actions</h4>
              <div className="space-y-2">
                <button className="w-full px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors flex items-center justify-center gap-2">
                  <Crown className="w-4 h-4" />
                  Add Premium Feature
                </button>
                <button className="w-full px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                  Enable All Features
                </button>
                <button className="w-full px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                  Export Feature List
                </button>
              </div>
            </div>
          </div>
        </>
      )}

      {selectedTab === "analytics" && (
        <>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Subscription Growth</h3>
              <div className="space-y-4">
                {[
                  { month: "Jan", free: 92000, premium: 18500 },
                  { month: "Feb", free: 93500, premium: 20100 },
                  { month: "Mar", free: 95200, premium: 22300 },
                  { month: "Apr", free: 96800, premium: 24200 },
                  { month: "May", free: 98234, premium: 26349 },
                ].map((data) => (
                  <div key={data.month}>
                    <div className="flex justify-between mb-2 text-sm">
                      <span>{data.month}</span>
                      <div className="flex gap-4">
                        <span className="text-blue-600">Free: {(data.free / 1000).toFixed(1)}K</span>
                        <span className="text-yellow-600">Premium: {(data.premium / 1000).toFixed(1)}K</span>
                      </div>
                    </div>
                    <div className="w-full bg-secondary h-8 rounded-full overflow-hidden flex">
                      <div
                        className="bg-blue-600"
                        style={{ width: `${(data.free / (data.free + data.premium)) * 100}%` }}
                      ></div>
                      <div
                        className="bg-yellow-600"
                        style={{ width: `${(data.premium / (data.free + data.premium)) * 100}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Revenue Breakdown</h3>
              <div className="space-y-6">
                <div>
                  <div className="flex justify-between mb-2">
                    <span className="text-sm text-muted-foreground">Monthly Subscriptions</span>
                    <span className="font-medium">${(totalRevenue / 1000).toFixed(1)}K</span>
                  </div>
                  <div className="w-full bg-secondary h-3 rounded-full">
                    <div className="bg-green-600 h-3 rounded-full" style={{ width: "85%" }}></div>
                  </div>
                </div>

                <div>
                  <div className="flex justify-between mb-2">
                    <span className="text-sm text-muted-foreground">Projected Annual</span>
                    <span className="font-medium">${(totalRevenue * 12 / 1000000).toFixed(2)}M</span>
                  </div>
                  <div className="w-full bg-secondary h-3 rounded-full">
                    <div className="bg-blue-600 h-3 rounded-full" style={{ width: "92%" }}></div>
                  </div>
                </div>

                <div className="pt-4 border-t border-border">
                  <h4 className="mb-3">Key Metrics</h4>
                  <div className="space-y-3">
                    <div className="flex justify-between">
                      <span className="text-sm text-muted-foreground">ARPU (Avg Revenue Per User)</span>
                      <span className="font-medium">$9.99</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-muted-foreground">Churn Rate</span>
                      <span className="font-medium text-green-600">2.3%</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-muted-foreground">LTV (Lifetime Value)</span>
                      <span className="font-medium">$434</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-4">Top Conversion Sources</h4>
              <div className="space-y-3">
                {[
                  { source: "In-App Prompt", rate: 18.5 },
                  { source: "Game Rewards", rate: 12.3 },
                  { source: "Feature Discovery", rate: 8.7 },
                  { source: "Friend Referral", rate: 6.2 },
                ].map((source) => (
                  <div key={source.source} className="flex items-center justify-between">
                    <span className="text-sm">{source.source}</span>
                    <span className="text-sm font-medium text-green-600">{source.rate}%</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-4">Retention Rate</h4>
              <div className="text-center mb-4">
                <div className="text-4xl font-bold text-green-600">87.3%</div>
                <p className="text-sm text-muted-foreground mt-1">Premium users stay subscribed</p>
              </div>
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>30-day retention</span>
                  <span>94%</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>90-day retention</span>
                  <span>89%</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>180-day retention</span>
                  <span>87%</span>
                </div>
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-4">Upgrade Triggers</h4>
              <div className="space-y-3">
                {[
                  { trigger: "Hit game limit", percentage: 34 },
                  { trigger: "Ad fatigue", percentage: 28 },
                  { trigger: "Need offline maps", percentage: 21 },
                  { trigger: "Want premium voice", percentage: 17 },
                ].map((item) => (
                  <div key={item.trigger}>
                    <div className="flex justify-between mb-1 text-sm">
                      <span>{item.trigger}</span>
                      <span>{item.percentage}%</span>
                    </div>
                    <div className="w-full bg-secondary h-2 rounded-full">
                      <div
                        className="bg-purple-600 h-2 rounded-full"
                        style={{ width: `${item.percentage}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
