import { useState } from "react";
import { Car, Users, MapPin, Clock, Calendar, DollarSign, CheckCircle, XCircle, AlertTriangle, Search, Filter, Navigation, UserCheck, Shield, TrendingUp, MessageSquare } from "lucide-react";
import { useAdminCollection } from "../hooks/useAdminResource";

type DriverPost = {
  id: number;
  driver: string;
  driverId: number;
  verified: boolean;
  from: string;
  to: string;
  date: string;
  time: string;
  seats: number;
  seatsBooked: number;
  pricePerSeat: number;
  status: string;
  vehicle: string;
  preferences: string;
  postedAt: string;
  route: string;
  distance: string;
  duration: string;
};

type PassengerRequest = {
  id: number;
  passenger: string;
  passengerId: number;
  verified: boolean;
  from: string;
  to: string;
  date: string;
  timeRange: string;
  passengers: number;
  maxPrice: number;
  status: string;
  preferences: string;
  postedAt: string;
  matches: number;
  matchedDriver?: string;
};

type MatchSuggestion = {
  id: number;
  driverPost: number;
  passengerRequest: number;
  matchScore: number;
  reasons: string[];
  status: string;
};

const fallbackDriverPosts: DriverPost[] = [
  {
    id: 1,
    driver: "John Smith",
    driverId: 101,
    verified: true,
    from: "123 Main St, Downtown",
    to: "456 Oak Ave, Airport",
    date: "2024-05-15",
    time: "08:00 AM",
    seats: 3,
    seatsBooked: 2,
    pricePerSeat: 15.00,
    status: "Active",
    vehicle: "Toyota Camry 2022",
    preferences: "Non-smoking, No pets",
    postedAt: "2024-05-13 10:30 AM",
    route: "Main St → Highway 101 → Airport Blvd",
    distance: "28 miles",
    duration: "35 min"
  },
  {
    id: 2,
    driver: "Sarah Johnson",
    driverId: 102,
    verified: true,
    from: "789 Park Rd, Uptown",
    to: "234 Beach Dr, Coast City",
    date: "2024-05-16",
    time: "06:00 PM",
    seats: 4,
    seatsBooked: 1,
    pricePerSeat: 25.00,
    status: "Active",
    vehicle: "Honda Accord 2023",
    preferences: "Music okay, Flexible stops",
    postedAt: "2024-05-13 09:15 AM",
    route: "Park Rd → Highway 1 → Beach Dr",
    distance: "45 miles",
    duration: "55 min"
  },
  {
    id: 3,
    driver: "Mike Chen",
    driverId: 103,
    verified: false,
    from: "321 Elm St, West Side",
    to: "654 University Ave, College Town",
    date: "2024-05-14",
    time: "07:30 AM",
    seats: 2,
    seatsBooked: 2,
    pricePerSeat: 10.00,
    status: "Completed",
    vehicle: "Ford Focus 2021",
    preferences: "Students preferred",
    postedAt: "2024-05-12 08:20 PM",
    route: "Elm St → Downtown → University Ave",
    distance: "18 miles",
    duration: "25 min"
  },
  {
    id: 4,
    driver: "Emma Wilson",
    driverId: 104,
    verified: true,
    from: "987 Maple Dr, South District",
    to: "555 Tech Blvd, Innovation Quarter",
    date: "2024-05-15",
    time: "09:00 AM",
    seats: 3,
    seatsBooked: 0,
    pricePerSeat: 12.00,
    status: "Cancelled",
    vehicle: "Tesla Model 3 2023",
    preferences: "Professional riders, Quiet ride",
    postedAt: "2024-05-13 07:45 AM",
    route: "Maple Dr → Express Lane → Tech Blvd",
    distance: "22 miles",
    duration: "30 min"
  },
];

const fallbackPassengerRequests: PassengerRequest[] = [
  {
    id: 1,
    passenger: "David Lee",
    passengerId: 201,
    verified: true,
    from: "150 Main St, Downtown",
    to: "450 Oak Ave, Airport Area",
    date: "2024-05-15",
    timeRange: "07:30 AM - 09:00 AM",
    passengers: 1,
    maxPrice: 20.00,
    status: "Looking",
    preferences: "Prefer verified drivers",
    postedAt: "2024-05-13 11:20 AM",
    matches: 2
  },
  {
    id: 2,
    passenger: "Anna Martinez",
    passengerId: 202,
    verified: true,
    from: "800 Park Rd, Uptown",
    to: "250 Beach Dr, Coast City",
    date: "2024-05-16",
    timeRange: "05:00 PM - 07:00 PM",
    passengers: 2,
    maxPrice: 30.00,
    status: "Matched",
    preferences: "Family-friendly",
    postedAt: "2024-05-13 10:05 AM",
    matches: 1,
    matchedDriver: "Sarah Johnson"
  },
  {
    id: 3,
    passenger: "Tom Wilson",
    passengerId: 203,
    verified: false,
    from: "300 Elm St, West Side",
    to: "600 University Ave, College Town",
    date: "2024-05-14",
    timeRange: "07:00 AM - 08:00 AM",
    passengers: 1,
    maxPrice: 12.00,
    status: "Completed",
    preferences: "Flexible",
    postedAt: "2024-05-12 09:30 PM",
    matches: 3,
    matchedDriver: "Mike Chen"
  },
];

const fallbackMatchingSuggestions: MatchSuggestion[] = [
  {
    id: 1,
    driverPost: 1,
    passengerRequest: 1,
    matchScore: 95,
    reasons: ["Same route", "Time match", "Price compatible"],
    status: "Pending"
  },
  {
    id: 2,
    driverPost: 2,
    passengerRequest: 2,
    matchScore: 88,
    reasons: ["Similar route", "Time match"],
    status: "Accepted"
  },
];

export function CompanionFinderManagement() {
  const [selectedTab, setSelectedTab] = useState<"overview" | "drivers" | "passengers" | "matches" | "safety">("overview");
  const [filterStatus, setFilterStatus] = useState("All");
  const [searchTerm, setSearchTerm] = useState("");

  const { data: driverPosts } = useAdminCollection<DriverPost>(
    "/companion/driver-posts",
    fallbackDriverPosts,
  );
  const { data: passengerRequests } = useAdminCollection<PassengerRequest>(
    "/companion/passenger-requests",
    fallbackPassengerRequests,
  );
  const { data: matchingSuggestions } = useAdminCollection<MatchSuggestion>(
    "/companion/matches",
    fallbackMatchingSuggestions,
  );

  const activeDriverPosts = driverPosts.filter(p => p.status === "Active").length;
  const activePasRequests = passengerRequests.filter(p => p.status === "Looking").length;
  const completedRides = driverPosts.filter(p => p.status === "Completed").length;
  const totalMatches = passengerRequests.filter(p => p.status === "Matched" || p.status === "Completed").length;

  const totalMoneySaved = completedRides * 35; // Estimated average savings per ride
  const totalCO2Saved = completedRides * 8.5; // Estimated kg CO2 saved per ride

  const filteredDriverPosts = driverPosts.filter(post => {
    const matchesSearch = post.driver.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         post.from.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         post.to.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === "All" || post.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  const filteredPassengerRequests = passengerRequests.filter(req => {
    const matchesSearch = req.passenger.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         req.from.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         req.to.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === "All" || req.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl mb-2">Companion Finder (Rideshare)</h2>
          <p className="text-muted-foreground">Manage carpooling posts, match drivers with passengers, and promote eco-friendly travel</p>
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
            onClick={() => setSelectedTab("drivers")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "drivers"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Driver Posts
          </button>
          <button
            onClick={() => setSelectedTab("passengers")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "passengers"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Passenger Requests
          </button>
          <button
            onClick={() => setSelectedTab("matches")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "matches"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Auto-Match
          </button>
          <button
            onClick={() => setSelectedTab("safety")}
            className={`px-4 py-2 rounded-lg transition-colors ${
              selectedTab === "safety"
                ? "bg-primary text-primary-foreground"
                : "bg-card border border-border hover:bg-accent"
            }`}
          >
            Safety & Moderation
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Active Driver Posts</h3>
            <Car className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl mb-1">{activeDriverPosts}</div>
          <p className="text-xs text-muted-foreground">Available rides</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Passenger Requests</h3>
            <Users className="w-5 h-5 text-purple-600" />
          </div>
          <div className="text-2xl mb-1">{activePasRequests}</div>
          <p className="text-xs text-muted-foreground">Looking for rides</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Successful Matches</h3>
            <CheckCircle className="w-5 h-5 text-green-600" />
          </div>
          <div className="text-2xl mb-1">{totalMatches}</div>
          <p className="text-xs text-green-600 dark:text-green-400">This week</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Money Saved</h3>
            <DollarSign className="w-5 h-5 text-yellow-600" />
          </div>
          <div className="text-2xl mb-1">${totalMoneySaved}</div>
          <p className="text-xs text-muted-foreground">Community savings</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">CO₂ Saved</h3>
            <TrendingUp className="w-5 h-5 text-green-600" />
          </div>
          <div className="text-2xl mb-1">{totalCO2Saved}kg</div>
          <p className="text-xs text-green-600 dark:text-green-400">Environmental impact</p>
        </div>
      </div>

      {selectedTab === "overview" && (
        <>
          <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
            <div className="flex items-start gap-3">
              <Car className="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5" />
              <div>
                <h4 className="text-blue-900 dark:text-blue-100 mb-1">How Companion Finder Works</h4>
                <p className="text-sm text-blue-700 dark:text-blue-300">
                  Drivers post their travel schedules (when, from, to) and available seats. Passengers can browse driver posts and contact them to join the ride, or post their own travel needs for drivers to find. This carpooling system helps users save money on fuel and tolls while reducing environmental impact.
                </p>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Recent Driver Posts</h3>
              <div className="space-y-4">
                {driverPosts.slice(0, 3).map((post) => (
                  <div key={post.id} className="border border-border rounded-lg p-4">
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex items-center gap-2">
                        <Car className="w-5 h-5 text-blue-600" />
                        <div>
                          <p className="font-medium flex items-center gap-2">
                            {post.driver}
                            {post.verified && <CheckCircle className="w-4 h-4 text-green-600" />}
                          </p>
                          <p className="text-xs text-muted-foreground">{post.vehicle}</p>
                        </div>
                      </div>
                      <span className={`px-2 py-1 rounded-full text-xs ${
                        post.status === "Active"
                          ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                          : post.status === "Completed"
                          ? "bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300"
                          : "bg-gray-100 dark:bg-gray-900/30 text-gray-800 dark:text-gray-300"
                      }`}>
                        {post.status}
                      </span>
                    </div>

                    <div className="space-y-2 text-sm mb-3">
                      <div className="flex items-center gap-2">
                        <MapPin className="w-4 h-4 text-green-600" />
                        <span className="text-muted-foreground">From:</span>
                        <span className="flex-1 truncate">{post.from}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <MapPin className="w-4 h-4 text-red-600" />
                        <span className="text-muted-foreground">To:</span>
                        <span className="flex-1 truncate">{post.to}</span>
                      </div>
                      <div className="flex items-center gap-4">
                        <div className="flex items-center gap-1">
                          <Calendar className="w-4 h-4" />
                          <span>{post.date}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Clock className="w-4 h-4" />
                          <span>{post.time}</span>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center justify-between pt-3 border-t border-border">
                      <div className="flex items-center gap-4 text-sm">
                        <span className="flex items-center gap-1">
                          <Users className="w-4 h-4" />
                          {post.seatsBooked}/{post.seats} booked
                        </span>
                        <span className="flex items-center gap-1 font-medium text-green-600">
                          <DollarSign className="w-4 h-4" />
                          ${post.pricePerSeat}/seat
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Recent Passenger Requests</h3>
              <div className="space-y-4">
                {passengerRequests.slice(0, 3).map((req) => (
                  <div key={req.id} className="border border-border rounded-lg p-4">
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex items-center gap-2">
                        <Users className="w-5 h-5 text-purple-600" />
                        <div>
                          <p className="font-medium flex items-center gap-2">
                            {req.passenger}
                            {req.verified && <CheckCircle className="w-4 h-4 text-green-600" />}
                          </p>
                          <p className="text-xs text-muted-foreground">{req.passengers} passenger(s)</p>
                        </div>
                      </div>
                      <span className={`px-2 py-1 rounded-full text-xs ${
                        req.status === "Looking"
                          ? "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                          : req.status === "Matched"
                          ? "bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300"
                          : "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                      }`}>
                        {req.status}
                      </span>
                    </div>

                    <div className="space-y-2 text-sm mb-3">
                      <div className="flex items-center gap-2">
                        <MapPin className="w-4 h-4 text-green-600" />
                        <span className="text-muted-foreground">From:</span>
                        <span className="flex-1 truncate">{req.from}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <MapPin className="w-4 h-4 text-red-600" />
                        <span className="text-muted-foreground">To:</span>
                        <span className="flex-1 truncate">{req.to}</span>
                      </div>
                      <div className="flex items-center gap-4">
                        <div className="flex items-center gap-1">
                          <Calendar className="w-4 h-4" />
                          <span>{req.date}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Clock className="w-4 h-4" />
                          <span>{req.timeRange}</span>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center justify-between pt-3 border-t border-border">
                      <div className="flex items-center gap-4 text-sm">
                        <span className="text-muted-foreground">Max price:</span>
                        <span className="font-medium text-green-600">${req.maxPrice}</span>
                      </div>
                      {req.matches > 0 && (
                        <span className="text-xs text-blue-600">{req.matches} match(es)</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Popular Routes</h3>
              <div className="space-y-3">
                {[
                  { route: "Downtown → Airport", trips: 45, avgPrice: "$15" },
                  { route: "Uptown → Beach", trips: 32, avgPrice: "$25" },
                  { route: "West Side → University", trips: 28, avgPrice: "$10" },
                  { route: "South → Tech Quarter", trips: 23, avgPrice: "$12" },
                ].map((route, idx) => (
                  <div key={idx} className="flex items-center justify-between p-3 bg-accent/30 rounded-lg">
                    <div>
                      <p className="text-sm font-medium">{route.route}</p>
                      <p className="text-xs text-muted-foreground">{route.trips} trips</p>
                    </div>
                    <span className="text-sm font-medium text-green-600">{route.avgPrice}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Peak Travel Times</h3>
              <div className="space-y-3">
                {[
                  { time: "7:00 AM - 9:00 AM", posts: 34 },
                  { time: "5:00 PM - 7:00 PM", posts: 28 },
                  { time: "12:00 PM - 2:00 PM", posts: 15 },
                  { time: "6:00 PM - 8:00 PM", posts: 12 },
                ].map((slot, idx) => (
                  <div key={idx}>
                    <div className="flex justify-between mb-1 text-sm">
                      <span>{slot.time}</span>
                      <span className="font-medium">{slot.posts} posts</span>
                    </div>
                    <div className="w-full bg-secondary h-2 rounded-full">
                      <div
                        className="bg-blue-600 h-2 rounded-full"
                        style={{ width: `${(slot.posts / 34) * 100}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <h3 className="mb-4">Environmental Impact</h3>
              <div className="space-y-4">
                <div className="p-4 bg-green-50 dark:bg-green-900/10 border border-green-200 dark:border-green-800 rounded-lg">
                  <div className="flex items-center gap-2 mb-2">
                    <TrendingUp className="w-5 h-5 text-green-600" />
                    <span className="font-medium">CO₂ Reduction</span>
                  </div>
                  <p className="text-2xl font-bold text-green-700 dark:text-green-400">{totalCO2Saved}kg</p>
                  <p className="text-xs text-muted-foreground mt-1">This month</p>
                </div>

                <div className="p-4 bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg">
                  <div className="flex items-center gap-2 mb-2">
                    <Car className="w-5 h-5 text-blue-600" />
                    <span className="font-medium">Cars Saved</span>
                  </div>
                  <p className="text-2xl font-bold text-blue-700 dark:text-blue-400">{completedRides}</p>
                  <p className="text-xs text-muted-foreground mt-1">From the road</p>
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {selectedTab === "drivers" && (
        <>
          <div className="bg-card border border-border rounded-lg">
            <div className="p-4 border-b border-border">
              <div className="flex flex-col lg:flex-row gap-3">
                <div className="flex-1 relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                  <input
                    type="text"
                    placeholder="Search driver posts..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div className="flex gap-2">
                  <select
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                    className="px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                  >
                    <option>All</option>
                    <option>Active</option>
                    <option>Completed</option>
                    <option>Cancelled</option>
                  </select>
                  <button className="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-2">
                    <Filter className="w-4 h-4" />
                    Filters
                  </button>
                </div>
              </div>
            </div>

            <div className="p-6">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {filteredDriverPosts.map((post) => (
                  <div key={post.id} className="border border-border rounded-lg p-5 hover:border-blue-500/50 transition-all">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center gap-3">
                        <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center">
                          <Car className="w-6 h-6 text-blue-600" />
                        </div>
                        <div>
                          <p className="font-medium flex items-center gap-2">
                            {post.driver}
                            {post.verified && (
                              <span className="flex items-center gap-1 text-xs text-green-600">
                                <Shield className="w-3 h-3" />
                                Verified
                              </span>
                            )}
                          </p>
                          <p className="text-sm text-muted-foreground">{post.vehicle}</p>
                        </div>
                      </div>
                      <span className={`px-3 py-1 rounded-full text-sm ${
                        post.status === "Active"
                          ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                          : post.status === "Completed"
                          ? "bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300"
                          : "bg-gray-100 dark:bg-gray-900/30 text-gray-800 dark:text-gray-300"
                      }`}>
                        {post.status}
                      </span>
                    </div>

                    <div className="space-y-3 mb-4">
                      <div className="flex items-start gap-2">
                        <Navigation className="w-4 h-4 text-blue-600 mt-1" />
                        <div className="flex-1">
                          <p className="text-sm text-muted-foreground mb-1">Route</p>
                          <p className="text-sm">{post.route}</p>
                          <div className="flex gap-4 mt-2 text-xs text-muted-foreground">
                            <span>{post.distance}</span>
                            <span>{post.duration}</span>
                          </div>
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-3 pt-3 border-t border-border">
                        <div>
                          <div className="flex items-center gap-1 text-sm text-muted-foreground mb-1">
                            <Calendar className="w-4 h-4" />
                            Date
                          </div>
                          <p className="text-sm font-medium">{post.date}</p>
                        </div>
                        <div>
                          <div className="flex items-center gap-1 text-sm text-muted-foreground mb-1">
                            <Clock className="w-4 h-4" />
                            Time
                          </div>
                          <p className="text-sm font-medium">{post.time}</p>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center justify-between p-3 bg-accent/30 rounded-lg mb-3">
                      <div className="flex items-center gap-1">
                        <Users className="w-4 h-4" />
                        <span className="text-sm">
                          {post.seatsBooked}/{post.seats} seats booked
                        </span>
                      </div>
                      <div className="flex items-center gap-1 font-medium text-green-600">
                        <DollarSign className="w-4 h-4" />
                        ${post.pricePerSeat}/seat
                      </div>
                    </div>

                    <div className="text-xs text-muted-foreground mb-3">
                      <span className="font-medium">Preferences:</span> {post.preferences}
                    </div>

                    <div className="flex gap-2">
                      <button className="flex-1 px-3 py-2 border border-border rounded-lg hover:bg-accent transition-colors text-sm">
                        View Details
                      </button>
                      {post.status === "Active" && (
                        <button className="px-3 py-2 border border-border rounded-lg hover:bg-accent transition-colors text-sm">
                          <MessageSquare className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </>
      )}

      {selectedTab === "passengers" && (
        <>
          <div className="bg-card border border-border rounded-lg">
            <div className="p-4 border-b border-border">
              <div className="flex flex-col lg:flex-row gap-3">
                <div className="flex-1 relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
                  <input
                    type="text"
                    placeholder="Search passenger requests..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                  />
                </div>
                <div className="flex gap-2">
                  <select
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                    className="px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                  >
                    <option>All</option>
                    <option>Looking</option>
                    <option>Matched</option>
                    <option>Completed</option>
                  </select>
                  <button className="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-2">
                    <Filter className="w-4 h-4" />
                    Filters
                  </button>
                </div>
              </div>
            </div>

            <div className="p-6">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {filteredPassengerRequests.map((req) => (
                  <div key={req.id} className="border border-border rounded-lg p-5 hover:border-purple-500/50 transition-all">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center gap-3">
                        <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/30 rounded-full flex items-center justify-center">
                          <Users className="w-6 h-6 text-purple-600" />
                        </div>
                        <div>
                          <p className="font-medium flex items-center gap-2">
                            {req.passenger}
                            {req.verified && (
                              <span className="flex items-center gap-1 text-xs text-green-600">
                                <Shield className="w-3 h-3" />
                                Verified
                              </span>
                            )}
                          </p>
                          <p className="text-sm text-muted-foreground">{req.passengers} passenger(s)</p>
                        </div>
                      </div>
                      <span className={`px-3 py-1 rounded-full text-sm ${
                        req.status === "Looking"
                          ? "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                          : req.status === "Matched"
                          ? "bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300"
                          : "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                      }`}>
                        {req.status}
                      </span>
                    </div>

                    <div className="space-y-2 text-sm mb-4">
                      <div className="flex items-center gap-2">
                        <MapPin className="w-4 h-4 text-green-600" />
                        <span className="text-muted-foreground">From:</span>
                        <span className="flex-1 truncate">{req.from}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <MapPin className="w-4 h-4 text-red-600" />
                        <span className="text-muted-foreground">To:</span>
                        <span className="flex-1 truncate">{req.to}</span>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-3 mb-4 pt-3 border-t border-border">
                      <div>
                        <div className="flex items-center gap-1 text-sm text-muted-foreground mb-1">
                          <Calendar className="w-4 h-4" />
                          Date
                        </div>
                        <p className="text-sm font-medium">{req.date}</p>
                      </div>
                      <div>
                        <div className="flex items-center gap-1 text-sm text-muted-foreground mb-1">
                          <Clock className="w-4 h-4" />
                          Time Range
                        </div>
                        <p className="text-xs font-medium">{req.timeRange}</p>
                      </div>
                    </div>

                    <div className="flex items-center justify-between p-3 bg-accent/30 rounded-lg mb-3">
                      <span className="text-sm">Max Price</span>
                      <div className="flex items-center gap-1 font-medium text-green-600">
                        <DollarSign className="w-4 h-4" />
                        ${req.maxPrice}
                      </div>
                    </div>

                    {req.status === "Matched" && req.matchedDriver && (
                      <div className="p-3 bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg mb-3">
                        <div className="flex items-center gap-2 text-sm">
                          <UserCheck className="w-4 h-4 text-blue-600" />
                          <span className="text-blue-900 dark:text-blue-100">Matched with {req.matchedDriver}</span>
                        </div>
                      </div>
                    )}

                    {req.matches > 0 && req.status === "Looking" && (
                      <div className="p-3 bg-yellow-50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-800 rounded-lg mb-3">
                        <div className="flex items-center gap-2 text-sm">
                          <AlertTriangle className="w-4 h-4 text-yellow-600" />
                          <span className="text-yellow-900 dark:text-yellow-100">{req.matches} potential match(es) available</span>
                        </div>
                      </div>
                    )}

                    <div className="text-xs text-muted-foreground mb-3">
                      <span className="font-medium">Preferences:</span> {req.preferences}
                    </div>

                    <div className="flex gap-2">
                      <button className="flex-1 px-3 py-2 border border-border rounded-lg hover:bg-accent transition-colors text-sm">
                        View Details
                      </button>
                      {req.status === "Looking" && (
                        <button className="px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm">
                          Find Match
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </>
      )}

      {selectedTab === "matches" && (
        <>
          <div className="bg-green-50 dark:bg-green-900/10 border border-green-200 dark:border-green-800 rounded-lg p-4 mb-6">
            <div className="flex items-start gap-3">
              <UserCheck className="w-5 h-5 text-green-600 dark:text-green-400 mt-0.5" />
              <div>
                <h4 className="text-green-900 dark:text-green-100 mb-1">Auto-Match System</h4>
                <p className="text-sm text-green-700 dark:text-green-300">
                  Our AI-powered matching system automatically suggests the best driver-passenger pairings based on route similarity, time compatibility, price matching, and user preferences. Review and approve matches below.
                </p>
              </div>
            </div>
          </div>

          <div className="bg-card border border-border rounded-lg p-6">
            <h3 className="mb-6">Suggested Matches</h3>
            <div className="space-y-6">
              {matchingSuggestions.map((match) => {
                const driver = driverPosts.find(d => d.id === match.driverPost);
                const passenger = passengerRequests.find(p => p.id === match.passengerRequest);

                if (!driver || !passenger) return null;

                return (
                  <div key={match.id} className="border border-border rounded-lg p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center gap-2">
                        <div className={`w-16 h-16 rounded-full flex items-center justify-center ${
                          match.matchScore >= 90 ? "bg-green-100 dark:bg-green-900/30" :
                          match.matchScore >= 80 ? "bg-blue-100 dark:bg-blue-900/30" :
                          "bg-yellow-100 dark:bg-yellow-900/30"
                        }`}>
                          <span className="text-2xl font-bold">{match.matchScore}%</span>
                        </div>
                        <div>
                          <h4>Match Quality</h4>
                          <div className="flex gap-2 mt-1">
                            {match.reasons.map((reason, idx) => (
                              <span key={idx} className="px-2 py-1 bg-secondary text-xs rounded">
                                {reason}
                              </span>
                            ))}
                          </div>
                        </div>
                      </div>
                      <span className={`px-3 py-1 rounded-full text-sm ${
                        match.status === "Accepted"
                          ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                          : "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                      }`}>
                        {match.status}
                      </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="p-4 bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg">
                        <div className="flex items-center gap-2 mb-3">
                          <Car className="w-5 h-5 text-blue-600" />
                          <h5>Driver: {driver.driver}</h5>
                        </div>
                        <div className="space-y-2 text-sm">
                          <div className="flex items-center gap-2">
                            <MapPin className="w-4 h-4" />
                            <span className="truncate">{driver.from} → {driver.to}</span>
                          </div>
                          <div className="flex gap-4">
                            <span>{driver.date}</span>
                            <span>{driver.time}</span>
                          </div>
                          <div className="flex items-center gap-1 text-green-600 font-medium">
                            <DollarSign className="w-4 h-4" />
                            ${driver.pricePerSeat}/seat
                          </div>
                        </div>
                      </div>

                      <div className="p-4 bg-purple-50 dark:bg-purple-900/10 border border-purple-200 dark:border-purple-800 rounded-lg">
                        <div className="flex items-center gap-2 mb-3">
                          <Users className="w-5 h-5 text-purple-600" />
                          <h5>Passenger: {passenger.passenger}</h5>
                        </div>
                        <div className="space-y-2 text-sm">
                          <div className="flex items-center gap-2">
                            <MapPin className="w-4 h-4" />
                            <span className="truncate">{passenger.from} → {passenger.to}</span>
                          </div>
                          <div className="flex gap-4">
                            <span>{passenger.date}</span>
                            <span className="text-xs">{passenger.timeRange}</span>
                          </div>
                          <div className="flex items-center gap-1 text-green-600 font-medium">
                            Max: ${passenger.maxPrice}
                          </div>
                        </div>
                      </div>
                    </div>

                    {match.status === "Pending" && (
                      <div className="flex gap-3 mt-4 pt-4 border-t border-border">
                        <button className="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">
                          Notify Both Parties
                        </button>
                        <button className="px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                          Dismiss
                        </button>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        </>
      )}

      {selectedTab === "safety" && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-card border border-border rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <Shield className="w-5 h-5 text-green-600" />
                <h3>User Verification</h3>
              </div>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Verified Drivers</span>
                  <span className="font-medium">67%</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Verified Passengers</span>
                  <span className="font-medium">72%</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Pending Verification</span>
                  <span className="font-medium text-yellow-600">23</span>
                </div>
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <AlertTriangle className="w-5 h-5 text-yellow-600" />
                <h3>Safety Incidents</h3>
              </div>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Reports This Month</span>
                  <span className="font-medium">4</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Resolved</span>
                  <span className="font-medium text-green-600">3</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">Under Review</span>
                  <span className="font-medium text-yellow-600">1</span>
                </div>
              </div>
            </div>

            <div className="bg-card border border-border rounded-lg p-6">
              <div className="flex items-center gap-2 mb-4">
                <CheckCircle className="w-5 h-5 text-blue-600" />
                <h3>Trust Score</h3>
              </div>
              <div className="text-center mb-4">
                <div className="text-4xl font-bold text-blue-600">4.8</div>
                <p className="text-sm text-muted-foreground">out of 5.0</p>
              </div>
              <p className="text-xs text-center text-muted-foreground">
                Based on user ratings and completed trips
              </p>
            </div>
          </div>

          <div className="bg-card border border-border rounded-lg p-6">
            <h3 className="mb-4">Safety Guidelines & Policies</h3>
            <div className="space-y-4">
              <div className="flex items-start gap-3 p-4 bg-accent/30 rounded-lg">
                <CheckCircle className="w-5 h-5 text-green-600 mt-0.5" />
                <div>
                  <h4 className="mb-1">Identity Verification</h4>
                  <p className="text-sm text-muted-foreground">All drivers must verify their identity with government-issued ID and driver's license. Optional for passengers.</p>
                </div>
              </div>

              <div className="flex items-start gap-3 p-4 bg-accent/30 rounded-lg">
                <CheckCircle className="w-5 h-5 text-green-600 mt-0.5" />
                <div>
                  <h4 className="mb-1">Vehicle Verification</h4>
                  <p className="text-sm text-muted-foreground">Drivers must provide vehicle registration and insurance documentation.</p>
                </div>
              </div>

              <div className="flex items-start gap-3 p-4 bg-accent/30 rounded-lg">
                <CheckCircle className="w-5 h-5 text-green-600 mt-0.5" />
                <div>
                  <h4 className="mb-1">Rating System</h4>
                  <p className="text-sm text-muted-foreground">Both drivers and passengers can rate each other after each trip. Low-rated users may be flagged for review.</p>
                </div>
              </div>

              <div className="flex items-start gap-3 p-4 bg-accent/30 rounded-lg">
                <CheckCircle className="w-5 h-5 text-green-600 mt-0.5" />
                <div>
                  <h4 className="mb-1">Emergency Reporting</h4>
                  <p className="text-sm text-muted-foreground">In-app emergency button to report safety concerns immediately.</p>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
