import { useCallback, useEffect, useMemo, useState, lazy, Suspense } from "react";
import { Search, Filter, CheckCircle, XCircle, Clock, Eye, MapPin, User, Calendar, MoreVertical, Image as ImageIcon, Download, Maximize2, RefreshCw } from "lucide-react";
import { useAdminCollection } from "../hooks/useAdminResource";
import { approvePanorama, rejectPanorama } from "../lib/api";
import type { PanoramaImage } from "../types";

const PanoramaViewer = lazy(() =>
  import("./PanoramaViewer").then((module) => ({ default: module.PanoramaViewer })),
);

const POLL_INTERVAL_MS = 10_000;

function formatFileSize(bytes: number): string {
  if (!bytes || bytes <= 0) return "—";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatUploadedAt(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return iso;
  return date.toLocaleString();
}

function mediaUrl(path: string): string {
  if (!path) return "";
  if (path.startsWith("http://") || path.startsWith("https://")) return path;
  return path.startsWith("/") ? path : `/${path}`;
}

export function Image360Management() {
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("All");
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [viewerOpen, setViewerOpen] = useState(false);
  const [actionBusy, setActionBusy] = useState(false);
  const [actionMessage, setActionMessage] = useState("");

  const { data: panoramas, loading, error, refresh } = useAdminCollection<PanoramaImage>("/panoramas", []);

  useEffect(() => {
    const timer = window.setInterval(() => {
      void refresh();
    }, POLL_INTERVAL_MS);
    return () => window.clearInterval(timer);
  }, [refresh]);

  const filteredImages = useMemo(() => {
    return panoramas.filter((img) => {
      const matchesSearch =
        img.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        img.poiName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        img.uploadedBy.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesStatus = filterStatus === "All" || img.status === filterStatus;
      return matchesSearch && matchesStatus;
    });
  }, [panoramas, searchTerm, filterStatus]);

  const selectedImageData = selectedImage ? panoramas.find((img) => img.id === selectedImage) : null;

  const pendingCount = panoramas.filter((img) => img.status === "Pending").length;
  const approvedCount = panoramas.filter((img) => img.status === "Approved").length;
  const rejectedCount = panoramas.filter((img) => img.status === "Rejected").length;
  const totalViews = panoramas.filter((img) => img.status === "Approved").reduce((sum, img) => sum + img.views, 0);

  const handleApprove = useCallback(async (mediaId: string) => {
    setActionBusy(true);
    setActionMessage("");
    try {
      await approvePanorama(mediaId);
      setActionMessage("Panorama approved.");
      await refresh();
    } catch (e) {
      setActionMessage(e instanceof Error ? e.message : "Failed to approve panorama.");
    } finally {
      setActionBusy(false);
    }
  }, [refresh]);

  const handleReject = useCallback(async (mediaId: string) => {
    const reason = window.prompt("Rejection reason (optional):", "Image quality too low, please re-upload in higher resolution.");
    if (reason === null) return;
    setActionBusy(true);
    setActionMessage("");
    try {
      await rejectPanorama(mediaId, reason.trim() || "Rejected by admin.");
      setActionMessage("Panorama rejected.");
      await refresh();
    } catch (e) {
      setActionMessage(e instanceof Error ? e.message : "Failed to reject panorama.");
    } finally {
      setActionBusy(false);
    }
  }, [refresh]);

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl mb-2">360° Image Management</h2>
          <p className="text-muted-foreground">Review and manage business-uploaded 360° panoramas</p>
        </div>
        <button
          type="button"
          onClick={() => void refresh()}
          disabled={loading || actionBusy}
          className="inline-flex items-center gap-2 px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors disabled:opacity-60"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? "animate-spin" : ""}`} />
          Refresh
        </button>
      </div>

      {error && (
        <div className="bg-yellow-50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4 text-sm text-yellow-800 dark:text-yellow-300">
          Could not load panoramas from server ({error}). Showing last known data.
        </div>
      )}

      {actionMessage && (
        <div className="bg-green-50 dark:bg-green-900/10 border border-green-200 dark:border-green-800 rounded-lg p-4 text-sm text-green-800 dark:text-green-300">
          {actionMessage}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Total Images</h3>
            <ImageIcon className="w-5 h-5 text-blue-600" />
          </div>
          <div className="text-2xl mb-1">{panoramas.length}</div>
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
            <h3 className="text-sm">Rejected</h3>
            <XCircle className="w-5 h-5 text-red-600" />
          </div>
          <div className="text-2xl mb-1">{rejectedCount}</div>
          <p className="text-xs text-muted-foreground">{totalViews.toLocaleString()} total views (approved)</p>
        </div>
      </div>

      <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
        <div className="flex items-start gap-3">
          <ImageIcon className="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5" />
          <div>
            <h4 className="text-blue-900 dark:text-blue-100 mb-1">360° Image Feature</h4>
            <p className="text-sm text-blue-700 dark:text-blue-300">
              When a business uploads a panorama from the mobile app, it appears here immediately as Pending.
              This page auto-refreshes every {POLL_INTERVAL_MS / 1000} seconds.
            </p>
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
          {loading && panoramas.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground">Loading panoramas…</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredImages.map((image) => (
                <div key={image.id} className="border border-border rounded-lg overflow-hidden hover:border-primary/50 transition-all group">
                  <div className="relative aspect-video overflow-hidden bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-800 dark:to-gray-900">
                    <img
                      src={mediaUrl(image.thumbnail)}
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
                        <span>{formatUploadedAt(image.uploadedAt)}</span>
                      </div>
                    </div>

                    {image.status === "Rejected" && image.rejectionReason && (
                      <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded text-xs text-red-700 dark:text-red-300">
                        <span className="font-medium">Rejection reason:</span> {image.rejectionReason}
                      </div>
                    )}

                    {image.status === "Pending" ? (
                      <div className="flex gap-2">
                        <button
                          type="button"
                          disabled={actionBusy}
                          onClick={() => void handleApprove(image.id)}
                          className="flex-1 px-3 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm disabled:opacity-60"
                        >
                          Approve
                        </button>
                        <button
                          type="button"
                          disabled={actionBusy}
                          onClick={() => void handleReject(image.id)}
                          className="flex-1 px-3 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors text-sm disabled:opacity-60"
                        >
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
          )}

          {!loading && filteredImages.length === 0 && (
            <div className="text-center py-12">
              <ImageIcon className="w-16 h-16 mx-auto mb-4 text-muted-foreground opacity-50" />
              <h3 className="mb-2">No 360° Images Found</h3>
              <p className="text-muted-foreground">
                {panoramas.length === 0
                  ? "No panoramas uploaded yet. They will appear here when a business uploads from the mobile app."
                  : "No images match your current filters"}
              </p>
            </div>
          )}

          {filteredImages.length > 0 && (
            <div className="flex items-center justify-between mt-6 pt-4 border-t border-border">
              <p className="text-sm text-muted-foreground">
                Showing {filteredImages.length} of {panoramas.length} images
              </p>
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
                <a
                  href={mediaUrl(selectedImageData.imageUrl)}
                  download
                  className="p-2 hover:bg-accent rounded-lg transition-colors"
                >
                  <Download className="w-5 h-5" />
                </a>
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

            <div className="relative bg-black" style={{ height: "min(60vh, 520px)" }}>
              <Suspense fallback={<div className="flex h-full items-center justify-center text-white/70">Loading viewer…</div>}>
                <PanoramaViewer
                  key={selectedImageData.id}
                  src={mediaUrl(selectedImageData.imageUrl)}
                  className="h-full"
                />
              </Suspense>
            </div>

            <div className="p-4 border-t border-border bg-accent/30">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                <div>
                  <p className="text-xs text-muted-foreground mb-1">File Size</p>
                  <p className="text-sm font-medium">{formatFileSize(selectedImageData.fileSizeBytes)}</p>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground mb-1">Uploaded</p>
                  <p className="text-sm font-medium">{formatUploadedAt(selectedImageData.uploadedAt)}</p>
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
                  <button
                    type="button"
                    disabled={actionBusy}
                    onClick={() => void handleApprove(selectedImageData.id)}
                    className="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-60"
                  >
                    Approve Image
                  </button>
                  <button
                    type="button"
                    disabled={actionBusy}
                    onClick={() => void handleReject(selectedImageData.id)}
                    className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-60"
                  >
                    Reject Image
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
