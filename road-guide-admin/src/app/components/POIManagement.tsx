import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  Search,
  MapPin,
  Plus,
  MoreVertical,
  CheckCircle,
  Clock,
  XCircle,
  Edit,
  List,
  Map as MapIcon,
  Filter,
  Building2,
  Navigation,
  Layers,
  RefreshCw,
} from "lucide-react";
import {
  createBusinessPoi,
  getBusinessPoiDetail,
  updateBusinessPoi,
} from "../lib/api";
import { normalizeCategoryId } from "../lib/poiCategories";
import { useMapPoiCategories } from "../lib/useMapPoiCategories";
import type { BusinessPoi } from "../types";
import { PoiManagementMap, type MapPoi } from "./PoiManagementMap";

type POIManagementProps = {
  pois: BusinessPoi[];
  pendingClaimCount: number;
  busy: boolean;
  token: string;
  onRefresh: () => void;
};

type PoiStatus = "Pending" | "Claimed" | "Unclaimed";

type PoiRow = BusinessPoi & {
  category: string;
  status: PoiStatus;
  claimedBy: string | null;
};

function poiCategoryId(poi: BusinessPoi): string {
  if (poi.category?.trim()) return normalizeCategoryId(poi.category);
  const raw = poi.metadata?.category;
  if (typeof raw === "string" && raw.trim()) return normalizeCategoryId(raw);
  return "";
}

function poiStatus(poi: BusinessPoi): PoiStatus {
  if ((poi.pendingClaims ?? 0) > 0) return "Pending";
  if (poi.ownerName) return "Claimed";
  return "Unclaimed";
}

function toPoiRow(poi: BusinessPoi): PoiRow {
  return {
    ...poi,
    category: poiCategoryId(poi),
    status: poiStatus(poi),
    claimedBy: poi.ownerName ?? null,
  };
}

function hasCoordinates(poi: BusinessPoi): poi is BusinessPoi & { latitude: number; longitude: number } {
  return typeof poi.latitude === "number" && typeof poi.longitude === "number";
}

function toMapPoi(poi: PoiRow): MapPoi | null {
  if (!hasCoordinates(poi)) return null;
  return {
    id: poi.id,
    name: poi.name,
    category: poi.category,
    status: poi.status === "Pending" ? "Pending" : poi.status === "Claimed" ? "Claimed" : "Unclaimed",
    latitude: poi.latitude,
    longitude: poi.longitude,
    claimedBy: poi.claimedBy,
  };
}

type PoiFormState = {
  name: string;
  category: string;
  address: string;
  latitude: string;
  longitude: string;
  description: string;
};

const emptyForm: PoiFormState = {
  name: "",
  category: "restaurant",
  address: "",
  latitude: "",
  longitude: "",
  description: "",
};

function formFromPoi(poi: PoiRow): PoiFormState {
  return {
    name: poi.name,
    category: poi.category,
    address: poi.address,
    latitude: poi.latitude != null ? String(poi.latitude) : "",
    longitude: poi.longitude != null ? String(poi.longitude) : "",
    description: poi.description ?? "",
  };
}

export function POIManagement({
  pois,
  pendingClaimCount,
  busy,
  token,
  onRefresh,
}: POIManagementProps) {
  const { categories, loading: categoriesLoading, labelFor, colorFor } = useMapPoiCategories();
  const [viewMode, setViewMode] = useState<"list" | "map">("list");
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("All");
  const [filterCategory, setFilterCategory] = useState("All");
  const [selectedPOI, setSelectedPOI] = useState<string | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingPOI, setEditingPOI] = useState<PoiRow | null>(null);
  const [form, setForm] = useState<PoiFormState>(emptyForm);
  const [actionBusy, setActionBusy] = useState(false);
  const [actionMessage, setActionMessage] = useState("");
  const [detailMediaCount, setDetailMediaCount] = useState<number | null>(null);
  const [categoryMapFocusKey, setCategoryMapFocusKey] = useState(0);
  const [categoryFocusPois, setCategoryFocusPois] = useState<MapPoi[]>([]);

  const poiRows = useMemo(() => pois.map(toPoiRow), [pois]);

  const categoryOptions = useMemo(() => {
    const byId = new Map(categories.map((category) => [category.id, category]));
    for (const poi of poiRows) {
      if (poi.category && !byId.has(poi.category)) {
        byId.set(poi.category, {
          id: poi.category,
          label: labelFor(poi.category),
          color: colorFor(poi.category),
        });
      }
    }
    return Array.from(byId.values()).sort((a, b) => a.label.localeCompare(b.label));
  }, [categories, poiRows, labelFor, colorFor]);

  const filteredPOIs = useMemo(() => {
    return poiRows.filter((poi) => {
      const query = searchTerm.toLowerCase();
      const matchesSearch =
        poi.name.toLowerCase().includes(query) ||
        poi.address.toLowerCase().includes(query) ||
        (poi.claimedBy?.toLowerCase().includes(query) ?? false) ||
        (poi.description?.toLowerCase().includes(query) ?? false);
      const matchesStatus =
        filterStatus === "All" ||
        (filterStatus === "Pending Review" && poi.status === "Pending") ||
        (filterStatus === "Claimed" && poi.status === "Claimed") ||
        (filterStatus === "Unclaimed" && poi.status === "Unclaimed");
      const matchesCategory = filterCategory === "All" || poi.category === filterCategory;
      return matchesSearch && matchesStatus && matchesCategory;
    });
  }, [poiRows, searchTerm, filterStatus, filterCategory]);

  const mapPOIs = useMemo(
    () => filteredPOIs.map(toMapPoi).filter((poi): poi is MapPoi => poi != null),
    [filteredPOIs],
  );

  const mapPoisForCategory = useCallback(
    (categoryId: string): MapPoi[] => {
      const rows =
        categoryId === "All"
          ? poiRows
          : poiRows.filter((poi) => poi.category === categoryId);
      return rows.map(toMapPoi).filter((poi): poi is MapPoi => poi != null);
    },
    [poiRows],
  );

  const triggerCategoryMapFocus = useCallback(
    (categoryId: string) => {
      const focusPois = mapPoisForCategory(categoryId);
      setCategoryFocusPois(focusPois);
      if (focusPois.length > 0) {
        setCategoryMapFocusKey((key) => key + 1);
      }
    },
    [mapPoisForCategory],
  );

  const handleLegendCategoryClick = useCallback(
    (categoryId: string) => {
      const nextCategory = filterCategory === categoryId ? "All" : categoryId;
      setFilterCategory(nextCategory);
      triggerCategoryMapFocus(nextCategory);
    },
    [filterCategory, triggerCategoryMapFocus],
  );

  const handleCategoryDropdownChange = useCallback(
    (categoryId: string) => {
      setFilterCategory(categoryId);
      if (viewMode === "map") {
        triggerCategoryMapFocus(categoryId);
      }
    },
    [viewMode, triggerCategoryMapFocus],
  );

  const prevViewModeRef = useRef(viewMode);

  useEffect(() => {
    if (prevViewModeRef.current !== "map" && viewMode === "map" && filterCategory !== "All") {
      triggerCategoryMapFocus(filterCategory);
    }
    prevViewModeRef.current = viewMode;
  }, [viewMode, filterCategory, triggerCategoryMapFocus]);

  const selectedPOIData = selectedPOI ? poiRows.find((p) => p.id === selectedPOI) ?? null : null;

  const businessOwnedCount = useMemo(
    () => poiRows.filter((poi) => poi.status === "Claimed").length,
    [poiRows],
  );
  const unclaimedCount = useMemo(
    () => poiRows.filter((poi) => poi.status === "Unclaimed").length,
    [poiRows],
  );

  const loadDetail = useCallback(
    async (poiId: string) => {
      setDetailMediaCount(null);
      try {
        const detail = await getBusinessPoiDetail(token, poiId);
        setDetailMediaCount(detail.media.length);
      } catch {
        setDetailMediaCount(null);
      }
    },
    [token],
  );

  const handleSelectPOI = useCallback(
    (poiId: string) => {
      setSelectedPOI(poiId);
      void loadDetail(poiId);
    },
    [loadDetail],
  );

  const parseCoordinate = (value: string): number | undefined => {
    const trimmed = value.trim();
    if (!trimmed) return undefined;
    const parsed = Number.parseFloat(trimmed);
    return Number.isFinite(parsed) ? parsed : undefined;
  };

  const handleSavePOI = async () => {
    setActionBusy(true);
    setActionMessage("");
    try {
      const latitude = parseCoordinate(form.latitude);
      const longitude = parseCoordinate(form.longitude);
      const metadata = { category: form.category };

      if (editingPOI) {
        await updateBusinessPoi(token, editingPOI.id, {
          name: form.name.trim(),
          address: form.address.trim(),
          description: form.description.trim(),
          category: form.category,
          metadata: { ...editingPOI.metadata, ...metadata },
        });
        setActionMessage("POI updated.");
      } else {
        await createBusinessPoi(token, {
          name: form.name.trim(),
          address: form.address.trim(),
          description: form.description.trim(),
          category: form.category,
          latitude,
          longitude,
        });
        setActionMessage("POI created.");
      }
      setShowAddModal(false);
      setEditingPOI(null);
      setForm(emptyForm);
      onRefresh();
    } catch (error) {
      setActionMessage(error instanceof Error ? error.message : "Failed to save POI.");
    } finally {
      setActionBusy(false);
    }
  };

  const openEdit = (poi: PoiRow) => {
    setEditingPOI(poi);
    setForm(formFromPoi(poi));
    setShowAddModal(true);
  };

  const openAdd = () => {
    setEditingPOI(null);
    setForm(emptyForm);
    setShowAddModal(true);
  };

  const closeModal = () => {
    setShowAddModal(false);
    setEditingPOI(null);
    setForm(emptyForm);
  };

  return (
    <div className="p-8 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl mb-2">POI Management</h2>
          <p className="text-muted-foreground">Manage business POIs, ownership, and claim requests</p>
        </div>
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onRefresh}
            disabled={busy}
            className="inline-flex items-center gap-2 px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors disabled:opacity-60"
          >
            <RefreshCw className={`w-4 h-4 ${busy ? "animate-spin" : ""}`} />
            Refresh
          </button>
          <div className="flex items-center gap-1 bg-card border border-border rounded-lg p-1">
            <button
              type="button"
              onClick={() => setViewMode("list")}
              className={`px-4 py-2 rounded-md transition-colors flex items-center gap-2 ${
                viewMode === "list" ? "bg-primary text-primary-foreground" : "hover:bg-accent"
              }`}
            >
              <List className="w-4 h-4" />
              List
            </button>
            <button
              type="button"
              onClick={() => setViewMode("map")}
              className={`px-4 py-2 rounded-md transition-colors flex items-center gap-2 ${
                viewMode === "map" ? "bg-primary text-primary-foreground" : "hover:bg-accent"
              }`}
            >
              <MapIcon className="w-4 h-4" />
              Map
            </button>
          </div>
          <button
            type="button"
            onClick={openAdd}
            className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            Add POI
          </button>
        </div>
      </div>

      {actionMessage && (
        <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4 text-sm text-blue-800 dark:text-blue-300">
          {actionMessage}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Total POIs</h3>
            <MapPin className="w-5 h-5 text-chart-1" />
          </div>
          <div className="text-2xl mb-1">{poiRows.length}</div>
          <p className="text-xs text-muted-foreground">Registered business places</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Pending Claims</h3>
            <Clock className="w-5 h-5 text-yellow-600" />
          </div>
          <div className="text-2xl mb-1">{pendingClaimCount}</div>
          <p className="text-xs text-muted-foreground">Awaiting review in Business Accounts</p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Business Owned</h3>
            <Building2 className="w-5 h-5 text-purple-600" />
          </div>
          <div className="text-2xl mb-1">{businessOwnedCount}</div>
          <p className="text-xs text-muted-foreground">
            {poiRows.length > 0
              ? `${Math.round((businessOwnedCount / poiRows.length) * 100)}% of total`
              : "No POIs yet"}
          </p>
        </div>

        <div className="bg-card border border-border rounded-lg p-5">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm">Unclaimed</h3>
            <MapPin className="w-5 h-5 text-muted-foreground" />
          </div>
          <div className="text-2xl mb-1">{unclaimedCount}</div>
          <p className="text-xs text-muted-foreground">Available for business claims</p>
        </div>
      </div>

      <div className="bg-card border border-border rounded-lg">
        <div className="p-4 border-b border-border">
          <div className="flex flex-col lg:flex-row gap-3">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
              <input
                type="text"
                placeholder="Search POIs by name, address, owner, or description..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
              />
            </div>
            <div className="flex gap-2">
              <select
                value={filterCategory}
                onChange={(e) => handleCategoryDropdownChange(e.target.value)}
                className="px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="All">All Categories</option>
                {categoryOptions.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.label}
                  </option>
                ))}
              </select>
              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                className="px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
              >
                <option value="All">All Status</option>
                <option value="Claimed">Claimed</option>
                <option value="Pending Review">Pending Review</option>
                <option value="Unclaimed">Unclaimed</option>
              </select>
              <button
                type="button"
                className="px-4 py-2 bg-secondary text-secondary-foreground rounded-lg hover:bg-secondary/80 transition-colors flex items-center gap-2"
              >
                <Filter className="w-4 h-4" />
                {filteredPOIs.length} results
              </button>
            </div>
          </div>
        </div>

        {viewMode === "map" ? (
          <div className="relative">
            <PoiManagementMap
              pois={mapPOIs}
              selectedPoiId={selectedPOI}
              onSelectPoi={handleSelectPOI}
              active={viewMode === "map"}
              categoryColor={colorFor}
              focusBoundsPois={categoryFocusPois}
              focusBoundsKey={categoryMapFocusKey}
            />

            <div className="absolute top-4 right-4 z-10 flex max-h-[min(70vh,28rem)] w-56 flex-col rounded-lg border border-border bg-card p-3 shadow-lg">
              <div className="mb-3 flex items-center gap-2">
                <Layers className="h-4 w-4 text-muted-foreground" />
                <span className="text-sm">Map POI Categories</span>
              </div>
              <div className="min-h-0 flex-1 space-y-1 overflow-y-auto pr-1 text-xs">
                {categoriesLoading ? (
                  <span className="text-muted-foreground">Loading categories…</span>
                ) : (
                  categoryOptions.map((category) => {
                    const active = filterCategory === category.id;
                    return (
                      <button
                        key={category.id}
                        type="button"
                        onClick={() => handleLegendCategoryClick(category.id)}
                        className={`flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left transition-colors hover:bg-accent/80 ${
                          active ? "bg-accent font-medium" : ""
                        }`}
                      >
                        <div
                          className="h-3 w-3 flex-shrink-0 rounded-full"
                          style={{ backgroundColor: category.color }}
                        />
                        <span className="truncate">{category.label}</span>
                      </button>
                    );
                  })
                )}
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

            <div className="absolute bottom-4 left-4 bg-card border border-border rounded-lg px-3 py-2 shadow-lg z-10 pointer-events-none">
              <div className="flex items-center gap-2 text-sm">
                <Navigation className="w-4 h-4 text-chart-1" />
                <span className="text-muted-foreground">On map</span>
                <span className="font-medium">
                  {mapPOIs.length} of {filteredPOIs.length} POIs
                </span>
              </div>
            </div>

            {selectedPOIData && (
              <div className="absolute bottom-8 left-1/2 -translate-x-1/2 z-20 bg-card border border-border rounded-lg shadow-2xl w-96 overflow-hidden animate-in slide-in-from-bottom-4">
                <div className="p-4 border-b border-border bg-accent/30">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <h3>{selectedPOIData.name}</h3>
                        {selectedPOIData.status === "Claimed" && (
                          <CheckCircle className="w-4 h-4 text-green-600" />
                        )}
                      </div>
                      <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <MapPin className="w-4 h-4" />
                        {selectedPOIData.address}
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={() => setSelectedPOI(null)}
                      className="p-1 hover:bg-accent rounded transition-colors"
                    >
                      <XCircle className="w-5 h-5" />
                    </button>
                  </div>
                  <div className="flex items-center gap-2">
                    <span
                      className="px-2 py-1 rounded text-xs text-white"
                      style={{ backgroundColor: colorFor(selectedPOIData.category) }}
                    >
                      {labelFor(selectedPOIData.category)}
                    </span>
                    <StatusBadge status={selectedPOIData.status} pendingClaims={selectedPOIData.pendingClaims ?? 0} />
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
                  {selectedPOIData.description && (
                    <p className="text-sm text-muted-foreground line-clamp-3">{selectedPOIData.description}</p>
                  )}
                  <div className="grid grid-cols-2 gap-3 text-center text-sm">
                    <div>
                      <div className="font-medium mb-1">{detailMediaCount ?? "—"}</div>
                      <p className="text-xs text-muted-foreground">Media items</p>
                    </div>
                    <div>
                      <div className="font-medium mb-1">{selectedPOIData.pendingClaims ?? 0}</div>
                      <p className="text-xs text-muted-foreground">Pending claims</p>
                    </div>
                  </div>
                  <div className="flex gap-2 pt-2">
                    <button
                      type="button"
                      onClick={() => openEdit(selectedPOIData)}
                      className="flex-1 px-3 py-2 border border-border rounded-lg hover:bg-accent transition-colors text-sm"
                    >
                      <Edit className="w-4 h-4 inline mr-1" />
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => void loadDetail(selectedPOIData.id)}
                      className="flex-1 px-3 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors text-sm"
                    >
                      Refresh Details
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        ) : (
          <div className="p-6">
            {filteredPOIs.length === 0 ? (
              <div className="text-center py-12 text-muted-foreground">
                No business POIs match your filters.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-border">
                      <th className="text-left py-3 px-4 text-sm text-muted-foreground">POI Name</th>
                      <th className="text-left py-3 px-4 text-sm text-muted-foreground">Category</th>
                      <th className="text-left py-3 px-4 text-sm text-muted-foreground">Business Owner</th>
                      <th className="text-left py-3 px-4 text-sm text-muted-foreground">Address</th>
                      <th className="text-left py-3 px-4 text-sm text-muted-foreground">Status</th>
                      <th className="text-left py-3 px-4 text-sm text-muted-foreground">Claims</th>
                      <th className="text-right py-3 px-4 text-sm text-muted-foreground">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredPOIs.map((poi) => (
                      <tr key={poi.id} className="border-b border-border hover:bg-accent/50 transition-colors">
                        <td className="py-4 px-4">
                          <div className="flex items-center gap-3">
                            <div
                              className="w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0"
                              style={{ backgroundColor: colorFor(poi.category) }}
                            >
                              <MapPin className="w-4 h-4 text-white" />
                            </div>
                            <div>
                              <div className="font-medium flex items-center gap-2">
                                {poi.name}
                                {poi.status === "Claimed" && (
                                  <CheckCircle className="w-4 h-4 text-green-600" />
                                )}
                              </div>
                              {poi.externalRef && (
                                <div className="text-xs text-muted-foreground truncate max-w-[200px]">
                                  {poi.externalRef}
                                </div>
                              )}
                            </div>
                          </div>
                        </td>
                        <td className="py-4 px-4">
                          <span className="px-3 py-1 bg-secondary text-secondary-foreground rounded-full text-sm">
                            {labelFor(poi.category)}
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
                          <StatusBadge status={poi.status} pendingClaims={poi.pendingClaims ?? 0} />
                        </td>
                        <td className="py-4 px-4">
                          {(poi.pendingClaims ?? 0) > 0 ? (
                            <span className="px-3 py-1 bg-orange-100 dark:bg-orange-900/30 text-orange-800 dark:text-orange-300 rounded-full text-sm">
                              {poi.pendingClaims} pending
                            </span>
                          ) : (
                            <span className="text-muted-foreground">None</span>
                          )}
                        </td>
                        <td className="py-4 px-4">
                          <div className="flex items-center justify-end gap-2">
                            <button
                              type="button"
                              onClick={() => openEdit(poi)}
                              className="p-2 hover:bg-accent rounded-lg transition-colors"
                              title="Edit POI"
                            >
                              <Edit className="w-4 h-4" />
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                handleSelectPOI(poi.id);
                                setViewMode("map");
                              }}
                              className="p-2 hover:bg-accent rounded-lg transition-colors"
                              title="View on map"
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
            )}

            <div className="flex items-center justify-between mt-6 pt-4 border-t border-border">
              <p className="text-sm text-muted-foreground">
                Showing {filteredPOIs.length} of {poiRows.length} POIs
              </p>
            </div>
          </div>
        )}
      </div>

      {showAddModal && (
        <div
          className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
          onClick={closeModal}
        >
          <div
            className="bg-card border border-border rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="text-xl mb-4">{editingPOI ? "Edit POI" : "Add New POI"}</h3>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="col-span-2">
                  <label className="block text-sm mb-2">POI Name</label>
                  <input
                    type="text"
                    value={form.name}
                    onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
                    className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                    placeholder="Enter POI name"
                  />
                </div>
                <div>
                  <label className="block text-sm mb-2">Category</label>
                  <select
                    value={form.category}
                    onChange={(e) => setForm((prev) => ({ ...prev, category: e.target.value }))}
                    className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                  >
                    {categoryOptions.map((category) => (
                      <option key={category.id} value={category.id}>
                        {category.label}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-span-2">
                  <label className="block text-sm mb-2">Address</label>
                  <input
                    type="text"
                    value={form.address}
                    onChange={(e) => setForm((prev) => ({ ...prev, address: e.target.value }))}
                    className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                    placeholder="Enter full address"
                  />
                </div>
                <div>
                  <label className="block text-sm mb-2">Latitude</label>
                  <input
                    type="text"
                    value={form.latitude}
                    onChange={(e) => setForm((prev) => ({ ...prev, latitude: e.target.value }))}
                    className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                    placeholder="40.7580"
                  />
                </div>
                <div>
                  <label className="block text-sm mb-2">Longitude</label>
                  <input
                    type="text"
                    value={form.longitude}
                    onChange={(e) => setForm((prev) => ({ ...prev, longitude: e.target.value }))}
                    className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring"
                    placeholder="-73.9855"
                  />
                </div>
                <div className="col-span-2">
                  <label className="block text-sm mb-2">Description</label>
                  <textarea
                    value={form.description}
                    onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
                    className="w-full px-4 py-2 bg-input-background border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring h-24"
                    placeholder="Enter POI description..."
                  />
                </div>
              </div>
              <div className="flex gap-3 pt-4">
                <button
                  type="button"
                  onClick={closeModal}
                  className="flex-1 px-4 py-2 border border-border rounded-lg hover:bg-accent transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  disabled={actionBusy || !form.name.trim() || !form.address.trim()}
                  onClick={() => void handleSavePOI()}
                  className="flex-1 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-60"
                >
                  {actionBusy ? "Saving..." : editingPOI ? "Save Changes" : "Add POI"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function StatusBadge({ status, pendingClaims }: { status: PoiStatus; pendingClaims: number }) {
  if (status === "Pending") {
    return (
      <span className="px-3 py-1 rounded-full text-sm flex items-center gap-1 w-fit bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300">
        <Clock className="w-3 h-3" />
        Pending ({pendingClaims})
      </span>
    );
  }
  if (status === "Claimed") {
    return (
      <span className="px-3 py-1 rounded-full text-sm flex items-center gap-1 w-fit bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300">
        <CheckCircle className="w-3 h-3" />
        Claimed
      </span>
    );
  }
  return (
    <span className="px-3 py-1 rounded-full text-sm flex items-center gap-1 w-fit bg-secondary text-secondary-foreground">
      Unclaimed
    </span>
  );
}
