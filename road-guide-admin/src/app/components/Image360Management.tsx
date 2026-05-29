import { useState } from "react";
import { Search, Filter, CheckCircle, XCircle, Clock, Eye, MapPin, User, Calendar, MoreVertical, Image as ImageIcon, Download, Maximize2 } from "lucide-react";
import { useAdminCollection } from "../hooks/useAdminResource";

type Image360 = {
  id: number;
  title: string;
  poiName: string;
  poiId: number;
  uploadedBy: string;
  userId: number;
  status: string;
  uploadedAt: string;
  views: number;
  location: string;
  thumbnail: string;
  fileSize: string;
  rejectionReason?: string;
};

const fallback360Images: Image360[] = [
  {
    id: 1,
    title: "Joe's Coffee Shop Interior",
    poiName: "Joe's Coffee Shop",
    poiId: 1,
    uploadedBy: "John Smith",
    userId: 101,
    status: "Pending",
    uploadedAt: "2024-05-13 10:30 AM",
    views: 0,
    location: "123 Main St, Downtown",
    thumbnail: "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400&h=300&fit=crop",
    fileSize: "8.5 MB"
  },
  {
    id: 2,
    title: "City Park Panorama",
    poiName: "City Park",
    poiId: 3,
    uploadedBy: "Emma Wilson",
    userId: 102,
    status: "Approved",
    uploadedAt: "2024-05-12 03:15 PM",
    views: 1245,
    location: "789 Park Rd, Uptown",
    thumbnail: "https://images.unsplash.com/photo-1519331379826-f10be5486c6f?w=400&h=300&fit=crop",
    fileSize: "12.3 MB"
  },
  {
    id: 3,
    title: "Bella Restaurant Dining Area",
    poiName: "Bella Restaurant",
    poiId: 5,
    uploadedBy: "Maria Lopez",
    userId: 103,
    status: "Approved",
    uploadedAt: "2024-05-11 06:45 PM",
    views: 890,
    location: "654 Pine St, East End",
    thumbnail: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&h=300&fit=crop",
    fileSize: "10.1 MB"
  },
  {
    id: 4,
    title: "Tech Store Product Display",
    poiName: "Tech Store Plus",
    poiId: 2,
    uploadedBy: "Mike Chen",
    userId: 104,
    status: "Rejected",
    uploadedAt: "2024-05-10 11:20 AM",
    views: 0,
    location: "456 Oak Ave, Midtown",
    thumbnail: "https://images.unsplash.com/photo-1601524909162-ae8725290836?w=400&h=300&fit=crop",
    fileSize: "9.7 MB",
    rejectionReason: "Image quality too low, please re-upload in higher resolution"
  },
  {
    id: 5,
    title: "Gym Main Floor",
    poiName: "Gym Fitness Pro",
    poiId: 6,
    uploadedBy: "David Brown",
    userId: 105,
    status: "Pending",
    uploadedAt: "2024-05-13 09:00 AM",
    views: 0,
    location: "987 Maple Dr, South District",
    thumbnail: "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400&h=300&fit=crop",
    fileSize: "11.2 MB"
  },
  {
    id: 6,
    title: "Green Grocery Fresh Produce Section",
    poiName: "Green Grocery",
    poiId: 8,
    uploadedBy: "Anna Lee",
    userId: 106,
    status: "Approved",
    uploadedAt: "2024-05-09 02:30 PM",
    views: 567,
    location: "234 Market St, Old Town",
    thumbnail: "https://images.unsplash.com/photo-1542838132-92c53300491e?w=400&h=300&fit=crop",
    fileSize: "7.8 MB"
  },
];

export function Image360Management() {
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("All");
  const [selectedImage, setSelectedImage] = useState<number | null>(null);
  const [viewerOpen, setViewerOpen] = useState(false);

  const { data: mock360Images } = useAdminCollection<Image360>("/images-360", fallback360Images);

  const filteredImages = mock360Images.filter(img => {
    const matchesSearch = img.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         img.poiName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         img.uploadedBy.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === "All" || img.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  const selectedImageData = selectedImage ? mock360Images.find(img => img.id === selectedImage) : null;

  const pendingCount = mock360Images.filter(img => img.status === "Pending").length;
  const approvedCount = mock360Images.filter(img => img.status === "Approved").length;
  const rejectedCount = mock360Images.filter(img => img.status === "Rejected").length;
  const totalViews = mock360Images.filter(img => img.status === "Approved").reduce((sum, img) => sum + img.views, 0);

  return (
    <div className="p-8 space-y-6">
      <div>
        <h2 className="text-2xl mb-2">360° Image Management</h2>
        <p className="text-muted-foreground">Review and manage user-submitted 360-degree images</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Total Images</h3>
            <ImageIcon className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl mb-1">{mock360Images.length}</div>
          <p className="text-xs text-muted-foreground">360° panoramas</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Pending Review</h3>
            <Clock className="w-5 h-5 text-yellow-600" />
          </div>
          <div className="text-2xl mb-1">{pendingCount}</div>
          <p className="text-xs text-muted-foreground">Awaiting approval</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Approved</h3>
            <CheckCircle className="w-5 h-5 text-green-600" />
          </div>
          <div className="text-2xl mb-1">{approvedCount}</div>
          <p className="text-xs text-green-600 dark:text-green-400">Live on platform</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Total Views</h3>
            <Eye className="w-5 h-5 text-purple-600" />
          </div>
          <div className="text-2xl mb-1">{totalViews.toLocaleString()}</div>
          <p className="text-xs text-muted-foreground">All approved images</p>
        </div>
      </div>

      <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
        <div className="flex items-start gap-3">
          <ImageIcon className="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5" />
          <div>
            <h4 className="text-blue-900 dark:text-blue-100 mb-1">360° Image Feature</h4>
            <p className="text-sm text-blue-700 dark:text-blue-300">Users can create and upload 360-degree panoramic images of POI locations. Images require admin approval before being visible to other users. Approved images enhance location discovery and user engagement.</p>
          </div>
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg">
        <div className="p-4 border-b border-border">
          <div className="flex flex-col lg:flex-row gap-3">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
              <input
                type="text"
                placeholder="Search by title, POI, or uploader..."
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
                <option>Pending</option>
                <option>Approved</option>
                <option>Rejected</option>
              </select>
              <button className="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-2">
                <Filter className="w-4 h-4" />
                More Filters
              </button>
            </div>
          </div>
        </div>

        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredImages.map((image) => (
              <div key={image.id} className="border border-border rounded-lg overflow-hidden hover:border-primary/50 transition-all group">
                <div className="relative aspect-video overflow-hidden bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-800 dark:to-gray-900">
                  <img
                    src={image.thumbnail}
                    alt={image.title}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                  <div className="absolute top-3 left-3">
                    <span className={`px-2 py-1 rounded-full text-xs flex items-center gap-1 ${
                      image.status === "Approved"
                        ? "bg-green-600 text-white"
                        : image.status === "Rejected"
                        ? "bg-red-600 text-white"
                        : "bg-yellow-600 text-white"
                    }`}>
                      {image.status === "Approved" && <CheckCircle className="w-3 h-3" />}
                      {image.status === "Rejected" && <XCircle className="w-3 h-3" />}
                      {image.status === "Pending" && <Clock className="w-3 h-3" />}
                      {image.status}
                    </span>
                  </div>
                  <div className="absolute top-3 right-3">
                    <div className="bg-black/70 backdrop-blur-sm px-2 py-1 rounded text-white text-xs flex items-center gap-1">
                      <ImageIcon className="w-3 h-3" />
                      360°
                    </div>
                  </div>
                  {image.status === "Approved" && (
                    <div className="absolute bottom-3 right-3">
                      <div className="bg-black/70 backdrop-blur-sm px-2 py-1 rounded text-white text-xs flex items-center gap-1">
                        <Eye className="w-3 h-3" />
                        {image.views.toLocaleString()}
                      </div>
                    </div>
                  )}
                  <button
                    onClick={() => {
                      setSelectedImage(image.id);
                      setViewerOpen(true);
                    }}
                    className="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <div className="bg-white dark:bg-gray-800 rounded-full p-3">
                      <Maximize2 className="w-6 h-6" />
                    </div>
                  </button>
                </div>

                <div className="p-4">
                  <h4 className="mb-2 line-clamp-1">{image.title}</h4>

                  <div className="space-y-2 mb-4">
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <MapPin className="w-4 h-4 text-blue-600" />
                      <span className="line-clamp-1">{image.poiName}</span>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <User className="w-4 h-4 text-purple-600" />
                      <span>{image.uploadedBy}</span>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Calendar className="w-4 h-4 text-green-600" />
                      <span>{image.uploadedAt}</span>
                    </div>
                  </div>

                  {image.status === "Rejected" && image.rejectionReason && (
                    <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded text-xs text-red-700 dark:text-red-300">
                      <span className="font-medium">Rejection reason:</span> {image.rejectionReason}
                    </div>
                  )}

                  {image.status === "Pending" ? (
                    <div className="flex gap-2">
                      <button className="flex-1 px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm">
                        Approve
                      </button>
                      <button className="flex-1 px-3 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors text-sm">
                        Reject
                      </button>
                    </div>
                  ) : (
                    <div className="flex gap-2">
                      <button
                        onClick={() => {
                          setSelectedImage(image.id);
                          setViewerOpen(true);
                        }}
                        className="flex-1 px-3 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors text-sm"
                      >
                        View 360°
                      </button>
                      <button className="p-2 border border-border rounded-lg hover:bg-accent transition-colors">
                        <MoreVertical className="w-4 h-4" />
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {filteredImages.length === 0 && (
            <div className="text-center py-12">
              <ImageIcon className="w-16 h-16 mx-auto mb-4 text-muted-foreground opacity-50" />
              <h3 className="mb-2">No 360° Images Found</h3>
              <p className="text-muted-foreground">No images match your current filters</p>
            </div>
          )}

          {filteredImages.length > 0 && (
            <div className="flex items-center justify-between mt-6 pt-4 border-t border-border">
              <p className="text-sm text-muted-foreground">
                Showing {filteredImages.length} of {mock360Images.length} images
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
          )}
        </div>
      </div>

      {viewerOpen && selectedImageData && (
        <div
          className="fixed inset-0 bg-black/90 flex items-center justify-center z-50 p-4"
          onClick={() => {
            setViewerOpen(false);
            setSelectedImage(null);
          }}
        >
          <div
            className="bg-card border border-border rounded-lg max-w-6xl w-full max-h-[90vh] overflow-hidden"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="p-4 border-b border-border flex items-center justify-between">
              <div className="flex-1">
                <h3 className="mb-1">{selectedImageData.title}</h3>
                <div className="flex items-center gap-4 text-sm text-muted-foreground">
                  <span className="flex items-center gap-1">
                    <MapPin className="w-4 h-4" />
                    {selectedImageData.poiName}
                  </span>
                  <span className="flex items-center gap-1">
                    <User className="w-4 h-4" />
                    {selectedImageData.uploadedBy}
                  </span>
                  {selectedImageData.status === "Approved" && (
                    <span className="flex items-center gap-1">
                      <Eye className="w-4 h-4" />
                      {selectedImageData.views.toLocaleString()} views
                    </span>
                  )}
                </div>
              </div>
              <div className="flex items-center gap-2">
                <button className="p-2 hover:bg-accent rounded-lg transition-colors">
                  <Download className="w-5 h-5" />
                </button>
                <button
                  onClick={() => {
                    setViewerOpen(false);
                    setSelectedImage(null);
                  }}
                  className="p-2 hover:bg-accent rounded-lg transition-colors"
                >
                  <XCircle className="w-5 h-5" />
                </button>
              </div>
            </div>

            <div className="relative bg-gradient-to-br from-gray-900 to-black" style={{ height: "500px" }}>
              <img
                src={selectedImageData.thumbnail}
                alt={selectedImageData.title}
                className="w-full h-full object-contain"
              />
              <div className="absolute inset-0 flex items-center justify-center bg-black/50">
                <div className="text-center text-white">
                  <ImageIcon className="w-16 h-16 mx-auto mb-4 opacity-50" />
                  <p className="text-lg mb-2">360° Viewer</p>
                  <p className="text-sm opacity-75">Interactive 360° panorama would display here</p>
                  <p className="text-xs opacity-50 mt-2">(Requires pannellum.js or similar library)</p>
                </div>
              </div>
            </div>

            <div className="p-4 border-t border-border bg-accent/30">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                <div>
                  <p className="text-xs text-muted-foreground mb-1">File Size</p>
                  <p className="text-sm font-medium">{selectedImageData.fileSize}</p>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground mb-1">Uploaded</p>
                  <p className="text-sm font-medium">{selectedImageData.uploadedAt}</p>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground mb-1">Location</p>
                  <p className="text-sm font-medium">{selectedImageData.location}</p>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground mb-1">Status</p>
                  <span className={`px-2 py-1 rounded-full text-xs inline-flex items-center gap-1 ${
                    selectedImageData.status === "Approved"
                      ? "bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300"
                      : selectedImageData.status === "Rejected"
                      ? "bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-300"
                      : "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300"
                  }`}>
                    {selectedImageData.status}
                  </span>
                </div>
              </div>

              {selectedImageData.status === "Pending" && (
                <div className="flex gap-2">
                  <button className="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">
                    Approve Image
                  </button>
                  <button className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
                    Reject Image
                  </button>
                  <button className="px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors">
                    Request Changes
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
