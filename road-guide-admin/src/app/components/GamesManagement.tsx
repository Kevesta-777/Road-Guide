import { useState } from "react";
import { Gamepad2, Users, Trophy, Play, Settings as SettingsIcon, Plus, Coins, MapPin, Gift, TrendingUp, Award, DollarSign, Target, Sparkles, Edit, Building2 } from "lucide-react";
import { useAdminCollection } from "../hooks/useAdminResource";

type Game = {
  id: number;
  name: string;
  type: string;
  description: string;
  icon: string;
  players: number;
  active: boolean;
  totalGamesPlayed: number;
  coinsPerGold?: number;
  coinsPerLocation?: number;
  goldLocations?: number;
  totalLocations?: number;
  sponsoredLocations?: number;
  dailyLimit: number;
  avgPlayTime: string;
};

type Reward = {
  id: number;
  name: string;
  cost: number;
  icon: string;
  category: string;
  claimed: number;
  available: boolean;
};

type SponsoredLocation = {
  id: number;
  business: string;
  location: string;
  coinBonus: number;
  impressions: number;
  visits: number;
  budget: string;
  status: string;
};

const fallbackGames: Game[] = [
  {
    id: 1,
    name: "Gold Hunt",
    type: "Treasure Hunt",
    description: "Search for hidden gold coins on the map",
    icon: "🪙",
    players: 24850,
    active: true,
    totalGamesPlayed: 156780,
    coinsPerGold: 10,
    goldLocations: 125,
    dailyLimit: 50,
    avgPlayTime: "12 min"
  },
  {
    id: 2,
    name: "Landmark Finder",
    type: "Location Discovery",
    description: "Find and visit landmarks and locations",
    icon: "🏛️",
    players: 18920,
    active: true,
    totalGamesPlayed: 98450,
    coinsPerLocation: 25,
    totalLocations: 89,
    sponsoredLocations: 23,
    dailyLimit: 20,
    avgPlayTime: "18 min"
  }
];

const fallbackRewards: Reward[] = [
  { id: 1, name: "Premium Map Theme", cost: 500, icon: "🗺️", category: "Customization", claimed: 1234, available: true },
  { id: 2, name: "$5 Starbucks Gift Card", cost: 1000, icon: "☕", category: "Gift Card", claimed: 456, available: true },
  { id: 3, name: "$10 Restaurant Voucher", cost: 2000, icon: "🍽️", category: "Gift Card", claimed: 234, available: true },
  { id: 4, name: "Ad-Free Experience (1 Month)", cost: 1500, icon: "🚫", category: "Premium", claimed: 678, available: true },
  { id: 5, name: "Special Avatar Badge", cost: 300, icon: "⭐", category: "Badge", claimed: 2340, available: true },
  { id: 6, name: "$25 Amazon Gift Card", cost: 5000, icon: "🎁", category: "Gift Card", claimed: 89, available: true },
  { id: 7, name: "Premium Navigation Voice", cost: 800, icon: "🎙️", category: "Customization", claimed: 567, available: true },
  { id: 8, name: "Exclusive POI Access", cost: 1200, icon: "🔓", category: "Premium", claimed: 345, available: true },
];

const fallbackSponsored: SponsoredLocation[] = [
  { id: 1, business: "Joe's Coffee Shop", location: "123 Main St", coinBonus: 50, impressions: 8920, visits: 234, budget: "$500", status: "Active" },
  { id: 2, business: "Bella Restaurant", location: "654 Pine St", coinBonus: 75, impressions: 6540, visits: 189, budget: "$750", status: "Active" },
  { id: 3, business: "TechMart Store", location: "456 Oak Ave", coinBonus: 40, impressions: 4320, visits: 112, budget: "$400", status: "Pending" },
];

export function GamesManagement() {
  const [selectedTab, setSelectedTab] = useState<"overview" | "rewards" | "sponsored">("overview");
  const [showRewardModal, setShowRewardModal] = useState(false);
  const [showSponsorModal, setShowSponsorModal] = useState(false);

  const { data: games } = useAdminCollection<Game>("/games", fallbackGames);
  const { data: rewardCatalog } = useAdminCollection<Reward>("/games/rewards", fallbackRewards);
  const { data: sponsoredLocations } = useAdminCollection<SponsoredLocation>(
    "/games/sponsored-locations",
    fallbackSponsored,
  );

  const totalCoinsEarned = 2456780;
  const totalCoinsSpent = 1234560;
  const activeCoins = totalCoinsEarned - totalCoinsSpent;

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl mb-2">Games & Rewards</h2>
          <p className="text-muted-foreground">Manage games, coin economy, and reward system</p>
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
            Games
          </button>
          <button
            onClick={() => setSelectedTab("rewards")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "rewards"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Rewards
          </button>
          <button
            onClick={() => setSelectedTab("sponsored")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "sponsored"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Sponsored Locations
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Active Players</h3>
            <Users className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl mb-1">43,770</div>
          <p className="text-xs text-green-600 dark:text-green-400">+18.5% growth</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Coins Earned</h3>
            <Coins className="w-5 h-5 text-yellow-600" />
          </div>
          <div className="text-2xl mb-1">{(totalCoinsEarned / 1000000).toFixed(2)}M</div>
          <p className="text-xs text-muted-foreground">Total distributed</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Coins Redeemed</h3>
            <Gift className="w-5 h-5 text-purple-600" />
          </div>
          <div className="text-2xl mb-1">{(totalCoinsSpent / 1000000).toFixed(2)}M</div>
          <p className="text-xs text-muted-foreground">For rewards</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Active Coins</h3>
            <TrendingUp className="w-5 h-5 text-green-600" />
          </div>
          <div className="text-2xl mb-1">{(activeCoins / 1000000).toFixed(2)}M</div>
          <p className="text-xs text-muted-foreground">In circulation</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Sponsored Ads</h3>
            <Target className="w-5 h-5 text-orange-600" />
          </div>
          <div className="text-2xl mb-1">23</div>
          <p className="text-xs text-muted-foreground">Active locations</p>
        </div>
      </div>

      {selectedTab === "overview" && (
        <>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {games.map((game) => (
              <div key={game.id} className="bg-card border border-border rounded-lg overflow-hidden">
                <div className="bg-gradient-to-br from-purple-500 to-pink-500 p-6 text-white">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="text-5xl">{game.icon}</div>
                      <div>
                        <h3 className="text-xl mb-1">{game.name}</h3>
                        <p className="text-sm opacity-90">{game.description}</p>
                      </div>
                    </div>
                    <span className="px-3 py-1 bg-white/20 backdrop-blur-sm rounded-full text-sm">
                      Active
                    </span>
                  </div>
                  <div className="grid grid-cols-3 gap-4">
                    <div>
                      <div className="flex items-center gap-1 mb-1">
                        <Users className="w-4 h-4" />
                        <span className="text-sm opacity-90">Players</span>
                      </div>
                      <p className="text-xl font-medium">{game.players.toLocaleString()}</p>
                    </div>
                    <div>
                      <div className="flex items-center gap-1 mb-1">
                        <Play className="w-4 h-4" />
                        <span className="text-sm opacity-90">Games</span>
                      </div>
                      <p className="text-xl font-medium">{(game.totalGamesPlayed / 1000).toFixed(0)}K</p>
                    </div>
                    <div>
                      <div className="flex items-center gap-1 mb-1">
                        <Trophy className="w-4 h-4" />
                        <span className="text-sm opacity-90">Avg Time</span>
                      </div>
                      <p className="text-xl font-medium">{game.avgPlayTime}</p>
                    </div>
                  </div>
                </div>

                <div className="p-6">
                  <h4 className="mb-4">Game Configuration</h4>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between p-3 bg-accent/30 rounded-lg">
                      <div className="flex items-center gap-2">
                        <Coins className="w-5 h-5 text-yellow-600" />
                        <span className="text-sm">Coins per {game.id === 1 ? "Gold" : "Location"}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <input
                          type="number"
                          defaultValue={game.id === 1 ? game.coinsPerGold : game.coinsPerLocation}
                          className="w-20 px-3 py-1 bg-input-background border border-border rounded text-sm text-right"
                        />
                        <Coins className="w-4 h-4 text-yellow-600" />
                      </div>
                    </div>

                    <div className="flex items-center justify-between p-3 bg-accent/30 rounded-lg">
                      <div className="flex items-center gap-2">
                        <MapPin className="w-5 h-5 text-blue-600" />
                        <span className="text-sm">Total {game.id === 1 ? "Gold Locations" : "Landmarks"}</span>
                      </div>
                      <span className="font-medium">{game.id === 1 ? game.goldLocations : game.totalLocations}</span>
                    </div>

                    {game.id === 2 && (
                      <div className="flex items-center justify-between p-3 bg-accent/30 rounded-lg">
                        <div className="flex items-center gap-2">
                          <Target className="w-5 h-5 text-orange-600" />
                          <span className="text-sm">Sponsored Locations</span>
                        </div>
                        <span className="font-medium text-orange-600">{game.sponsoredLocations}</span>
                      </div>
                    )}

                    <div className="flex items-center justify-between p-3 bg-accent/30 rounded-lg">
                      <div className="flex items-center gap-2">
                        <Award className="w-5 h-5 text-purple-600" />
                        <span className="text-sm">Daily Limit</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <input
                          type="number"
                          defaultValue={game.dailyLimit}
                          className="w-20 px-3 py-1 bg-input-background border border-border rounded text-sm text-right"
                        />
                        <span className="text-sm text-muted-foreground">per day</span>
                      </div>
                    </div>
                  </div>

                  <div className="flex gap-2 mt-6">
                    <button className="flex-1 px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                      View Analytics
                    </button>
                    <button className="flex-1 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">
                      Save Settings
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="bg-card border border-border rounded-lg p-6">
            <h3 className="mb-4">Leaderboard - Top Players</h3>
            <div className="space-y-3">
              {[
                { rank: 1, name: "Alex Chen", coins: 15680, goldFound: 89, landmarksFound: 45 },
                { rank: 2, name: "Maria Garcia", coins: 14230, goldFound: 76, landmarksFound: 52 },
                { rank: 3, name: "John Smith", coins: 13890, goldFound: 82, landmarksFound: 41 },
                { rank: 4, name: "Emma Wilson", coins: 12450, goldFound: 71, landmarksFound: 38 },
                { rank: 5, name: "David Lee", coins: 11920, goldFound: 68, landmarksFound: 36 },
              ].map((player) => (
                <div key={player.rank} className="flex items-center justify-between p-4 bg-accent/30 rounded-lg">
                  <div className="flex items-center gap-4">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                      player.rank === 1 ? "bg-yellow-500 text-white" :
                      player.rank === 2 ? "bg-gray-400 text-white" :
                      player.rank === 3 ? "bg-orange-700 text-white" :
                      "bg-secondary text-foreground"
                    }`}>
                      {player.rank <= 3 ? <Trophy className="w-5 h-5" /> : <span>#{player.rank}</span>}
                    </div>
                    <div>
                      <p className="font-medium">{player.name}</p>
                      <div className="flex items-center gap-3 text-xs text-muted-foreground">
                        <span>🪙 {player.goldFound} gold found</span>
                        <span>🏛️ {player.landmarksFound} landmarks</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Coins className="w-5 h-5 text-yellow-600" />
                    <span className="font-medium">{player.coins.toLocaleString()}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}

      {selectedTab === "rewards" && (
        <>
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-xl mb-1">Reward Catalog</h3>
              <p className="text-sm text-muted-foreground">Manage rewards that users can redeem with coins</p>
            </div>
            <button
              onClick={() => setShowRewardModal(true)}
              className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              Add Reward
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {rewardCatalog.map((reward) => (
              <div key={reward.id} className="bg-card border border-border rounded-lg p-5 hover:border-primary/50 transition-all">
                <div className="flex items-start justify-between mb-3">
                  <div className="text-4xl">{reward.icon}</div>
                  <span className="px-2 py-1 bg-secondary text-secondary-foreground rounded text-xs">
                    {reward.category}
                  </span>
                </div>
                <h4 className="mb-2 line-clamp-2 min-h-[3rem]">{reward.name}</h4>
                <div className="flex items-center gap-2 mb-3">
                  <Coins className="w-5 h-5 text-yellow-600" />
                  <span className="text-xl font-medium">{reward.cost.toLocaleString()}</span>
                  <span className="text-sm text-muted-foreground">coins</span>
                </div>
                <div className="flex items-center justify-between text-sm mb-4">
                  <span className="text-muted-foreground">Claimed:</span>
                  <span className="font-medium">{reward.claimed.toLocaleString()}</span>
                </div>
                <div className="flex gap-2">
                  <button className="flex-1 px-3 py-2 border border-border rounded-lg hover:bg-accent transition-colors text-sm">
                    <Edit className="w-3 h-3 inline mr-1" />
                    Edit
                  </button>
                  <button className={`px-3 py-2 rounded-lg text-sm transition-colors ${
                    reward.available
                      ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                      : "bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300"
                  }`}>
                    {reward.available ? "Active" : "Paused"}
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6">
            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-4">Top Redeemed Rewards</h4>
              <div className="space-y-3">
                {rewardCatalog
                  .sort((a, b) => b.claimed - a.claimed)
                  .slice(0, 5)
                  .map((reward) => (
                    <div key={reward.id} className="flex items-center gap-3 p-3 bg-accent/30 rounded-lg">
                      <span className="text-2xl">{reward.icon}</span>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">{reward.name}</p>
                        <p className="text-xs text-muted-foreground">{reward.claimed.toLocaleString()} claims</p>
                      </div>
                    </div>
                  ))}
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-4">Coin Economy Stats</h4>
              <div className="space-y-4">
                <div>
                  <div className="flex justify-between mb-2">
                    <span className="text-sm text-muted-foreground">Total Earned</span>
                    <span className="font-medium">{(totalCoinsEarned / 1000000).toFixed(2)}M</span>
                  </div>
                  <div className="w-full bg-secondary h-2 rounded-full">
                    <div className="bg-green-600 h-2 rounded-full" style={{ width: "100%" }}></div>
                  </div>
                </div>
                <div>
                  <div className="flex justify-between mb-2">
                    <span className="text-sm text-muted-foreground">Total Spent</span>
                    <span className="font-medium">{(totalCoinsSpent / 1000000).toFixed(2)}M</span>
                  </div>
                  <div className="w-full bg-secondary h-2 rounded-full">
                    <div className="bg-blue-600 h-2 rounded-full" style={{ width: `${(totalCoinsSpent / totalCoinsEarned) * 100}%` }}></div>
                  </div>
                </div>
                <div>
                  <div className="flex justify-between mb-2">
                    <span className="text-sm text-muted-foreground">Redemption Rate</span>
                    <span className="font-medium">{((totalCoinsSpent / totalCoinsEarned) * 100).toFixed(1)}%</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-4">Reward Categories</h4>
              <div className="space-y-3">
                {[
                  { category: "Gift Cards", count: 3, percentage: 37 },
                  { category: "Customization", count: 2, percentage: 25 },
                  { category: "Premium", count: 2, percentage: 25 },
                  { category: "Badge", count: 1, percentage: 13 },
                ].map((cat) => (
                  <div key={cat.category} className="flex items-center justify-between">
                    <span className="text-sm">{cat.category}</span>
                    <div className="flex items-center gap-2">
                      <div className="w-20 bg-secondary h-2 rounded-full">
                        <div className="bg-purple-600 h-2 rounded-full" style={{ width: `${cat.percentage}%` }}></div>
                      </div>
                      <span className="text-sm font-medium w-8 text-right">{cat.count}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </>
      )}

      {selectedTab === "sponsored" && (
        <>
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-xl mb-1">Sponsored Locations</h3>
              <p className="text-sm text-muted-foreground">Businesses pay to feature their locations in Landmark Finder game</p>
            </div>
            <button
              onClick={() => setShowSponsorModal(true)}
              className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              Add Sponsored Location
            </button>
          </div>

          <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4 mb-6">
            <div className="flex items-start gap-3">
              <Sparkles className="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5" />
              <div>
                <h4 className="text-blue-900 dark:text-blue-100 mb-1">How Sponsored Locations Work</h4>
                <p className="text-sm text-blue-700 dark:text-blue-300">Businesses can pay to feature their locations in the Landmark Finder game. When players discover these sponsored locations, they earn bonus coins. This creates a win-win: businesses get visibility and foot traffic, while players earn more rewards.</p>
              </div>
            </div>
          </div>

          <div className="bg-card border border-border rounded-lg">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Business</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Location</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Coin Bonus</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Impressions</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Visits</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Budget</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Status</th>
                    <th className="text-right py-3 px-4 text-sm text-muted-foreground">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {sponsoredLocations.map((location) => (
                    <tr key={location.id} className="border-b border-border hover:bg-accent/50 transition-colors">
                      <td className="py-4 px-4">
                        <div className="flex items-center gap-2">
                          <Building2 className="w-4 h-4 text-purple-600" />
                          <span className="font-medium">{location.business}</span>
                        </div>
                      </td>
                      <td className="py-4 px-4 text-sm">{location.location}</td>
                      <td className="py-4 px-4">
                        <div className="flex items-center gap-1">
                          <Coins className="w-4 h-4 text-yellow-600" />
                          <span className="font-medium">+{location.coinBonus}</span>
                        </div>
                      </td>
                      <td className="py-4 px-4 text-sm">{location.impressions.toLocaleString()}</td>
                      <td className="py-4 px-4">
                        <span className="font-medium text-green-600 dark:text-green-400">{location.visits}</span>
                      </td>
                      <td className="py-4 px-4 text-sm">{location.budget}</td>
                      <td className="py-4 px-4">
                        <span className={`px-3 py-1 rounded-full text-sm ${
                          location.status === "Active"
                            ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                            : "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                        }`}>
                          {location.status}
                        </span>
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex items-center justify-end gap-2">
                          <button className="px-3 py-1 border border-border rounded hover:bg-accent transition-colors text-sm">
                            Edit
                          </button>
                          {location.status === "Pending" && (
                            <button className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700 text-sm">
                              Approve
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6">
            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-2">Total Revenue</h4>
              <div className="flex items-center gap-2 mb-1">
                <DollarSign className="w-6 h-6 text-green-600" />
                <span className="text-3xl font-medium">$1,650</span>
              </div>
              <p className="text-sm text-green-600 dark:text-green-400">+28% this month</p>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-2">Conversion Rate</h4>
              <div className="flex items-center gap-2 mb-1">
                <Target className="w-6 h-6 text-blue-600" />
                <span className="text-3xl font-medium">2.6%</span>
              </div>
              <p className="text-sm text-muted-foreground">Impressions to visits</p>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h4 className="mb-2">Avg Coin Bonus</h4>
              <div className="flex items-center gap-2 mb-1">
                <Coins className="w-6 h-6 text-yellow-600" />
                <span className="text-3xl font-medium">55</span>
              </div>
              <p className="text-sm text-muted-foreground">Per sponsored location</p>
            </div>
          </div>
        </>
      )}

      {showRewardModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={() => setShowRewardModal(false)}>
          <div className="bg-card border border-border rounded-lg p-6 max-w-md w-full mx-4" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-xl mb-4">Add New Reward</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm mb-2">Reward Name</label>
                <input type="text" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" placeholder="e.g., $10 Gift Card" />
              </div>
              <div>
                <label className="block text-sm mb-2">Category</label>
                <select className="w-full px-4 py-2 bg-input-background border border-border rounded-lg">
                  <option>Gift Card</option>
                  <option>Customization</option>
                  <option>Premium</option>
                  <option>Badge</option>
                </select>
              </div>
              <div>
                <label className="block text-sm mb-2">Coin Cost</label>
                <input type="number" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" placeholder="1000" />
              </div>
              <div>
                <label className="block text-sm mb-2">Emoji Icon</label>
                <input type="text" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" placeholder="🎁" />
              </div>
              <div className="flex gap-3 pt-4">
                <button onClick={() => setShowRewardModal(false)} className="flex-1 px-4 py-2 border border-border rounded-lg hover:bg-accent">
                  Cancel
                </button>
                <button className="flex-1 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90">
                  Add Reward
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {showSponsorModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={() => setShowSponsorModal(false)}>
          <div className="bg-card border border-border rounded-lg p-6 max-w-md w-full mx-4" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-xl mb-4">Add Sponsored Location</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm mb-2">Business</label>
                <select className="w-full px-4 py-2 bg-input-background border border-border rounded-lg">
                  <option>Joe's Coffee Shop</option>
                  <option>Bella Restaurant</option>
                  <option>TechMart Store</option>
                  <option>FitPro Gym</option>
                </select>
              </div>
              <div>
                <label className="block text-sm mb-2">Coin Bonus</label>
                <input type="number" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" placeholder="50" />
              </div>
              <div>
                <label className="block text-sm mb-2">Campaign Budget ($)</label>
                <input type="number" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" placeholder="500" />
              </div>
              <div>
                <label className="block text-sm mb-2">Duration (days)</label>
                <input type="number" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg" placeholder="30" />
              </div>
              <div className="flex gap-3 pt-4">
                <button onClick={() => setShowSponsorModal(false)} className="flex-1 px-4 py-2 border border-border rounded-lg hover:bg-accent">
                  Cancel
                </button>
                <button className="flex-1 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90">
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
