import { useState } from "react";
import { Search, MapPin, Plus, MoreVertical, CheckCircle, Clock, XCircle, Edit, List, Map as MapIcon, Filter, Building2, Star, Navigation, Layers } from "lucide-react";
import { useAdminCollection } from "../hooks/useAdminResource";

type POI = {
  id: number;
  name: string;
  category: string;
  address: string;
  lat: number;
  lng: number;
  status: string;
  claimedBy: string | null;
  businessId: number | null;
  rating: number;
  reviews: number;
  claims: number;
  verified: boolean;
};

const fallbackPOIs: POI[] = [
  { id: 1, name: "Joe's Coffee Shop", category: "Restaurant", address: "123 Main St, Downtown", lat: 40.7580, lng: -73.9855, status: "Approved", claimedBy: "Joe Smith", businessId: 1, rating: 4.5, reviews: 234, claims: 0, verified: true },
  { id: 2, name: "Tech Store Plus", category: "Shop", address: "456 Oak Ave, Midtown", lat: 40.7614, lng: -73.9776, status: "Pending", claimedBy: null, businessId: null, rating: 4.2, reviews: 89, claims: 1, verified: false },
  { id: 3, name: "City Park", category: "Entertainment", address: "789 Park Rd, Uptown", lat: 40.7689, lng: -73.9681, status: "Approved", claimedBy: null, businessId: null, rating: 4.8, reviews: 567, claims: 0, verified: false },
  { id: 4, name: "Quick Repair", category: "Services", address: "321 Elm St, West Side", lat: 40.7489, lng: -73.9680, status: "Rejected", claimedBy: null, businessId: null, rating: 3.9, reviews: 45, claims: 0, verified: false },
  { id: 5, name: "Bella Restaurant", category: "Restaurant", address: "654 Pine St, East End", lat: 40.7529, lng: -73.9925, status: "Approved", claimedBy: "Maria Lopez", businessId: 3, rating: 4.7, reviews: 432, claims: 0, verified: true },
  { id: 6, name: "Gym Fitness Pro", category: "Services", address: "987 Maple Dr, South District", lat: 40.7458, lng: -73.9867, status: "Pending", claimedBy: null, businessId: null, rating: 4.3, reviews: 156, claims: 2, verified: false },
  { id: 7, name: "TechMart Store", category: "Shop", address: "555 Tech Blvd, Innovation Quarter", lat: 40.7601, lng: -73.9845, status: "Approved", claimedBy: "Sarah Johnson", businessId: 2, rating: 4.4, reviews: 178, claims: 0, verified: true },
  { id: 8, name: "Green Grocery", category: "Shop", address: "234 Market St, Old Town", lat: 40.7542, lng: -73.9712, status: "Approved", claimedBy: null, businessId: null, rating: 4.6, reviews: 289, claims: 0, verified: false },
];

const categoryColors: Record<string, string> = {
  Restaurant: "bg-red-500",
  Shop: "bg-blue-500",
  Entertainment: "bg-purple-500",
  Services: "bg-green-500",
};

export function POIManagement() {
  const [viewMode, setViewMode] = useState<"list" | "map">("map");
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("All");
  const [filterCategory, setFilterCategory] = useState("All");
  const [selectedPOI, setSelectedPOI] = useState<number | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);

  const { data: mockPOIs } = useAdminCollection<POI>("/pois", fallbackPOIs);

  const filteredPOIs = mockPOIs.filter(poi => {
    const matchesSearch = poi.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         poi.address.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (poi.claimedBy && poi.claimedBy.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesStatus = filterStatus === "All" || poi.status === filterStatus;
    const matchesCategory = filterCategory === "All" || poi.category === filterCategory;
    return matchesSearch && matchesStatus && matchesCategory;
  });

  const selectedPOIData = selectedPOI ? mockPOIs.find(p => p.id === selectedPOI) : null;

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl mb-2">POI Management</h2>
          <p className="text-muted-foreground">Manage points of interest and business claims</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1 bg-card border border-border rounded-lg p-1">
            <button
              onClick={() => setViewMode("list")}
              className={`px-4 py-2 rounded-md transition-colors flex items-center gap-2 ${
                viewMode === "list"
                  ? "bg-primary text-primary-foreground"
                  : "hover:bg-accent"
              }`}
            >
              <List className="w-4 h-4" />
              List
            </button>
            <button
              onClick={() => setViewMode("map")}
              className={`px-4 py-2 rounded-md transition-colors flex items-center gap-2 ${
                viewMode === "map"
                  ? "bg-primary text-primary-foreground"
                  : "hover:bg-accent"
              }`}
            >
              <MapIcon className="w-4 h-4" />
              Map
            </button>
          </div>
          <button
            onClick={() => setShowAddModal(true)}
            className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            Add POI
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Total POIs</h3>
            <MapPin className="w-5 h-5 text-chart-1" />
          </div>
          <div className="text-2xl mb-1">8,429</div>
          <p className="text-xs text-muted-foreground">+156 this week</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Pending Claims</h3>
            <Clock className="w-5 h-5 text-yellow-600" />
          </div>
          <div className="text-2xl mb-1">23</div>
          <p className="text-xs text-muted-foreground">Require review</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Business Owned</h3>
            <Building2 className="w-5 h-5 text-purple-600" />
          </div>
          <div className="text-2xl mb-1">3,234</div>
          <p className="text-xs text-muted-foreground">38% of total</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Avg Rating</h3>
            <Star className="w-5 h-5 text-yellow-500" />
          </div>
          <div className="text-2xl mb-1">4.3</div>
          <p className="text-xs text-muted-foreground">Across all POIs</p>
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg">
        <div className="p-4 border-b border-border">
          <div className="flex flex-col lg:flex-row gap-3">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
              <input
                type="text"
                placeholder="Search POIs by name, address, or owner..."
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
                <option>All Categories</option>
                <option>Restaurant</option>
                <option>Shop</option>
                <option>Entertainment</option>
                <option>Services</option>
              </select>
              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                className="px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option>All Status</option>
                <option>Approved</option>
                <option>Pending</option>
                <option>Rejected</option>
              </select>
              <button className="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-2">
                <Filter className="w-4 h-4" />
                More Filters
              </button>
            </div>
          </div>
        </div>

        {viewMode === "map" ? (
          <div className="relative">
            <div className="h-[600px] bg-gradient-to-br from-blue-50 to-green-50 dark:from-gray-800 dark:to-gray-900 relative overflow-hidden">
              <div className="absolute inset-0" style={{
                backgroundImage: `
                  linear-gradient(rgba(0,0,0,0.03) 1px, transparent 1px),
                  linear-gradient(90deg, rgba(0,0,0,0.03) 1px, transparent 1px)
                `,
                backgroundSize: '40px 40px'
              }}>
              </div>

              {filteredPOIs.map((poi) => {
                const xPos = ((poi.lng + 74) * 1000) % 100;
                const yPos = ((poi.lat - 40.74) * 1000) % 100;

                return (
                  <div
                    key={poi.id}
                    onClick={() => setSelectedPOI(poi.id)}
                    className="absolute cursor-pointer transform -translate-x-1/2 -translate-y-1/2 group"
                    style={{
                      left: `${xPos}%`,
                      top: `${yPos}%`
                    }}
                  >
                    <div className={`w-10 h-10 rounded-full ${categoryColors[poi.category]} shadow-lg flex items-center justify-center transition-transform group-hover:scale-125 ${
                      selectedPOI === poi.id ? 'ring-4 ring-white scale-125' : ''
                    }`}>
                      <MapPin className="w-5 h-5 text-white" />
                    </div>
                    {poi.claimedBy && (
                      <div className="absolute -top-1 -right-1 w-4 h-4 bg-purple-600 rounded-full border-2 border-white flex items-center justify-center">
                        <Building2 className="w-2.5 h-2.5 text-white" />
                      </div>
                    )}
                    {poi.status === "Pending" && (
                      <div className="absolute -bottom-1 -right-1 w-4 h-4 bg-yellow-500 rounded-full border-2 border-white">
                        <Clock className="w-2.5 h-2.5 text-white" />
                      </div>
                    )}
                  </div>
                );
              })}

              <div className="absolute top-4 right-4 bg-card border border-border rounded-lg p-3 shadow-lg">
                <div className="flex items-center gap-2 mb-3">
                  <Layers className="w-4 h-4 text-muted-foreground" />
                  <span className="text-sm">Legend</span>
                </div>
                <div className="space-y-2 text-xs">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-red-500"></div>
                    <span>Restaurant</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-blue-500"></div>
                    <span>Shop</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-purple-500"></div>
                    <span>Entertainment</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-green-500"></div>
                    <span>Services</span>
                  </div>
                  <div className="border-t border-border pt-2 mt-2">
                    <div className="flex items-center gap-2">
                      <Building2 className="w-3 h-3 text-purple-600" />
                      <span>Business Owned</span>
                    </div>
                    <div className="flex items-center gap-2 mt-1">
                      <Clock className="w-3 h-3 text-yellow-500" />
                      <span>Pending Review</span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="absolute bottom-4 left-4 bg-card border border-border rounded-lg px-3 py-2 shadow-lg">
                <div className="flex items-center gap-2 text-sm">
                  <Navigation className="w-4 h-4 text-chart-1" />
                  <span className="text-muted-foreground">Showing</span>
                  <span className="font-medium">{filteredPOIs.length} POIs</span>
                </div>
              </div>
            </div>

            {selectedPOIData && (
              <div className="absolute bottom-8 left-1/2 -translate-x-1/2 bg-card border border-border rounded-lg shadow-2xl w-96 overflow-hidden animate-in slide-in-from-bottom-4">
                <div className="p-4 border-b border-border bg-accent/30">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <h3>{selectedPOIData.name}</h3>
                        {selectedPOIData.verified && (
                          <CheckCircle className="w-4 h-4 text-green-600" />
                        )}
                      </div>
                      <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <MapPin className="w-4 h-4" />
                        {selectedPOIData.address}
                      </div>
                    </div>
                    <button
                      onClick={() => setSelectedPOI(null)}
                      className="p-1 hover:bg-accent rounded transition-colors"
                    >
                      <XCircle className="w-5 h-5" />
                    </button>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`px-2 py-1 rounded text-xs ${categoryColors[selectedPOIData.category]} text-white`}>
                      {selectedPOIData.category}
                    </span>
                    <span className={`px-2 py-1 rounded-full text-xs ${
                      selectedPOIData.status === "Approved"
                        ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                        : selectedPOIData.status === "Rejected"
                        ? "bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300"
                        : "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                    }`}>
                      {selectedPOIData.status}
                    </span>
                  </div>
                </div>
                <div className="p-4 space-y-3">
                  {selectedPOIData.claimedBy && (
                    <div className="flex items-center gap-2 p-3 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
                      <Building2 className="w-5 h-5 text-purple-600" />
                      <div className="flex-1">
                        <p className="text-sm font-medium">Business Owner</p>
                        <p className="text-sm text-muted-foreground">{selectedPOIData.claimedBy}</p>
                      </div>
                    </div>
                  )}
                  <div className="grid grid-cols-3 gap-3 text-center">
                    <div>
                      <div className="flex items-center justify-center gap-1 mb-1">
                        <Star className="w-4 h-4 text-yellow-500 fill-yellow-500" />
                        <span className="font-medium">{selectedPOIData.rating}</span>
                      </div>
                      <p className="text-xs text-muted-foreground">Rating</p>
                    </div>
                    <div>
                      <div className="font-medium mb-1">{selectedPOIData.reviews}</div>
                      <p className="text-xs text-muted-foreground">Reviews</p>
                    </div>
                    <div>
                      <div className="font-medium mb-1">{selectedPOIData.claims}</div>
                      <p className="text-xs text-muted-foreground">Claims</p>
                    </div>
                  </div>
                  <div className="flex gap-2 pt-2">
                    <button className="flex-1 px-3 py-2 border border-border rounded-lg hover:bg-accent transition-colors text-sm">
                      <Edit className="w-4 h-4 inline mr-1" />
                      Edit
                    </button>
                    <button className="flex-1 px-3 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors text-sm">
                      View Details
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        ) : (
          <div className="p-6">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">POI Name</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Category</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Business Owner</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Address</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Status</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Rating</th>
                    <th className="text-left py-3 px-4 text-sm text-muted-foreground">Claims</th>
                    <th className="text-right py-3 px-4 text-sm text-muted-foreground">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPOIs.map((poi) => (
                    <tr key={poi.id} className="border-b border-border hover:bg-accent/50 transition-colors">
                      <td className="py-4 px-4">
                        <div className="flex items-center gap-3">
                          <div className={`w-8 h-8 rounded-full ${categoryColors[poi.category]} flex items-center justify-center flex-shrink-0`}>
                            <MapPin className="w-4 h-4 text-white" />
                          </div>
                          <div>
                            <div className="font-medium flex items-center gap-2">
                              {poi.name}
                              {poi.verified && <CheckCircle className="w-4 h-4 text-green-600" />}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <span className="px-3 py-1 bg-secondary text-secondary-foreground rounded-full text-sm">
                          {poi.category}
                        </span>
                      </td>
                      <td className="py-4 px-4">
                        {poi.claimedBy ? (
                          <div className="flex items-center gap-2">
                            <Building2 className="w-4 h-4 text-purple-600" />
                            <span className="text-sm">{poi.claimedBy}</span>
                          </div>
                        ) : (
                          <span className="text-sm text-muted-foreground">Unclaimed</span>
                        )}
                      </td>
                      <td className="py-4 px-4 text-sm">{poi.address}</td>
                      <td className="py-4 px-4">
                        <span className={`px-3 py-1 rounded-full text-sm flex items-center gap-1 w-fit ${
                          poi.status === "Approved"
                            ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                            : poi.status === "Rejected"
                            ? "bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300"
                            : "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                        }`}>
                          {poi.status === "Approved" && <CheckCircle className="w-3 h-3" />}
                          {poi.status === "Rejected" && <XCircle className="w-3 h-3" />}
                          {poi.status === "Pending" && <Clock className="w-3 h-3" />}
                          {poi.status}
                        </span>
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex items-center gap-2">
                          <Star className="w-4 h-4 text-yellow-500 fill-yellow-500" />
                          <span>{poi.rating}</span>
                          <span className="text-sm text-muted-foreground">({poi.reviews})</span>
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        {poi.claims > 0 ? (
                          <span className="px-3 py-1 bg-orange-100 dark:bg-orange-900/30 text-orange-800 dark:text-orange-300 rounded-full text-sm">
                            {poi.claims} pending
                          </span>
                        ) : (
                          <span className="text-muted-foreground">None</span>
                        )}
                      </td>
                      <td className="py-4 px-4">
                        <div className="flex items-center justify-end gap-2">
                          <button className="p-2 hover:bg-accent rounded-lg transition-colors">
                            <Edit className="w-4 h-4" />
                          </button>
                          <button className="p-2 hover:bg-accent rounded-lg transition-colors">
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
              <p className="text-sm text-muted-foreground">Showing {filteredPOIs.length} of {mockPOIs.length} POIs</p>
              <div className="flex gap-2">
                <button className="px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">Previous</button>
                <button className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">Next</button>
              </div>
            </div>
          </div>
        )}
      </div>

      {showAddModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={() => setShowAddModal(false)}>
          <div className="bg-card border border-border rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-xl mb-4">Add New POI</h3>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="col-span-2">
                  <label className="block text-sm mb-2">POI Name</label>
                  <input type="text" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring" placeholder="Enter POI name" />
                </div>
                <div>
                  <label className="block text-sm mb-2">Category</label>
                  <select className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring">
                    <option>Restaurant</option>
                    <option>Shop</option>
                    <option>Entertainment</option>
                    <option>Services</option>
                    <option>Other</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm mb-2">Business Owner (Optional)</label>
                  <select className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring">
                    <option>None (Public POI)</option>
                    <option>Joe Smith - Joe's Coffee</option>
                    <option>Sarah Johnson - TechMart</option>
                    <option>Maria Lopez - Bella Restaurant</option>
                  </select>
                </div>
                <div className="col-span-2">
                  <label className="block text-sm mb-2">Address</label>
                  <input type="text" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring" placeholder="Enter full address" />
                </div>
                <div>
                  <label className="block text-sm mb-2">Latitude</label>
                  <input type="text" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring" placeholder="40.7580" />
                </div>
                <div>
                  <label className="block text-sm mb-2">Longitude</label>
                  <input type="text" className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring" placeholder="-73.9855" />
                </div>
                <div className="col-span-2">
                  <label className="block text-sm mb-2">Description</label>
                  <textarea className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring h-24" placeholder="Enter POI description..."></textarea>
                </div>
              </div>
              <div className="flex gap-3 pt-4">
                <button onClick={() => setShowAddModal(false)} className="flex-1 px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                  Cancel
                </button>
                <button className="flex-1 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors">
                  Add POI
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
