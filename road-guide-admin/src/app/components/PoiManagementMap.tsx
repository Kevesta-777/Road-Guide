import { useCallback, useEffect, useRef, useState } from "react";
import maplibregl, { type Map as MapLibreMap, type Marker } from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
import {
  loadMapViewport,
  loadResolvedHeadwayStyle,
  OSM_FALLBACK_STYLE,
  type MapViewport,
} from "../lib/mapConfig";
import { categoryColor as defaultCategoryColor } from "../lib/poiCategories";

export type MapPoi = {
  id: string;
  name: string;
  category: string;
  status: "Pending" | "Claimed" | "Unclaimed";
  latitude: number;
  longitude: number;
  claimedBy: string | null;
};

type PoiManagementMapProps = {
  pois: MapPoi[];
  selectedPoiId: string | null;
  onSelectPoi: (poiId: string) => void;
  active: boolean;
  categoryColor?: (category: string) => string;
  focusBoundsPois?: MapPoi[];
  focusBoundsKey?: number;
};

const MIN_BOUNDS_SPAN = 0.004;
const FOCUS_PADDING = { top: 96, bottom: 96, left: 96, right: 300 };

const PIN_ICON_SVG = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" width="18" height="18" aria-hidden="true"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>`;
const BUILDING_ICON_SVG = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" width="11" height="11" aria-hidden="true"><rect width="16" height="20" x="4" y="2" rx="2" ry="2"/><path d="M9 22v-4h6v4"/><path d="M8 6h.01"/><path d="M16 6h.01"/><path d="M12 6h.01"/><path d="M12 10h.01"/><path d="M12 14h.01"/><path d="M16 10h.01"/><path d="M16 14h.01"/><path d="M8 10h.01"/><path d="M8 14h.01"/></svg>`;
const CLOCK_ICON_SVG = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" width="11" height="11" aria-hidden="true"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`;

function lngLatBoundsForPois(pois: MapPoi[]): maplibregl.LngLatBounds | null {
  if (pois.length === 0) return null;
  const bounds = new maplibregl.LngLatBounds();
  for (const poi of pois) {
    bounds.extend([poi.longitude, poi.latitude]);
  }
  const ne = bounds.getNorthEast();
  const sw = bounds.getSouthWest();
  const lngSpan = ne.lng - sw.lng;
  const latSpan = ne.lat - sw.lat;
  if (lngSpan < MIN_BOUNDS_SPAN) {
    const midLng = (ne.lng + sw.lng) / 2;
    bounds.extend([midLng - MIN_BOUNDS_SPAN / 2, ne.lat]);
    bounds.extend([midLng + MIN_BOUNDS_SPAN / 2, sw.lat]);
  }
  if (latSpan < MIN_BOUNDS_SPAN) {
    const midLat = (ne.lat + sw.lat) / 2;
    bounds.extend([ne.lng, midLat + MIN_BOUNDS_SPAN / 2]);
    bounds.extend([sw.lng, midLat - MIN_BOUNDS_SPAN / 2]);
  }
  return bounds;
}

function focusMapOnPois(map: MapLibreMap, pois: MapPoi[]) {
  if (pois.length === 0) return;

  if (pois.length === 1) {
    map.easeTo({
      center: [pois[0].longitude, pois[0].latitude],
      zoom: 16,
      duration: 650,
    });
    return;
  }

  const bounds = lngLatBoundsForPois(pois);
  if (!bounds) return;

  const camera = map.cameraForBounds(bounds, {
    padding: FOCUS_PADDING,
    maxZoom: 16,
  });
  if (camera) {
    map.easeTo({
      center: camera.center,
      zoom: camera.zoom,
      bearing: camera.bearing ?? 0,
      duration: 650,
    });
    return;
  }

  map.fitBounds(bounds, {
    padding: FOCUS_PADDING,
    maxZoom: 16,
    duration: 650,
  });
}

function applyViewport(map: MapLibreMap, viewport: MapViewport) {
  if (viewport.bounds) {
    map.fitBounds(viewport.bounds, { padding: 48, maxZoom: 14, duration: 0 });
    return;
  }
  if (viewport.center) {
    map.jumpTo({
      center: viewport.center,
      zoom: viewport.zoom ?? 10,
    });
  }
}

function createStatusBadge(kind: "claimed" | "pending"): HTMLDivElement {
  const badge = document.createElement("div");
  badge.className = "poi-map-badge";
  if (kind === "claimed") {
    badge.style.backgroundColor = "#9333ea";
    badge.style.top = "-4px";
    badge.style.right = "-4px";
    badge.innerHTML = BUILDING_ICON_SVG;
    badge.title = "Business owned";
  } else {
    badge.style.backgroundColor = "#f59e0b";
    badge.style.bottom = "-4px";
    badge.style.right = "-4px";
    badge.innerHTML = CLOCK_ICON_SVG;
    badge.title = "Pending review";
  }
  return badge;
}

function renderPoiMarkerContent(
  container: HTMLElement,
  poi: MapPoi,
  color: string,
  selected: boolean,
) {
  container.replaceChildren();
  container.className = "poi-map-marker";
  container.dataset.poiId = poi.id;
  container.title = poi.name;

  const pin = document.createElement("div");
  pin.className = "poi-map-pin";
  pin.style.backgroundColor = color;
  if (selected) {
    pin.classList.add("poi-map-pin--selected");
  }
  pin.innerHTML = PIN_ICON_SVG;

  if (poi.status === "Claimed") {
    pin.appendChild(createStatusBadge("claimed"));
  } else if (poi.status === "Pending") {
    pin.appendChild(createStatusBadge("pending"));
  }

  container.appendChild(pin);
}

function createPoiMarkerElement(
  poi: MapPoi,
  color: string,
  selected: boolean,
  onSelect: (poiId: string) => void,
): HTMLElement {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "poi-map-marker-hit";
  renderPoiMarkerContent(button, poi, color, selected);
  button.addEventListener("click", (event) => {
    event.stopPropagation();
    onSelect(poi.id);
  });
  return button;
}

function fitInitialViewport(map: MapLibreMap, viewport: MapViewport | null, pois: MapPoi[]) {
  const bounds = lngLatBoundsForPois(pois);
  if (bounds) {
    const camera = map.cameraForBounds(bounds, { padding: FOCUS_PADDING, maxZoom: 15 });
    if (camera) {
      map.jumpTo({ center: camera.center, zoom: camera.zoom });
      return;
    }
    map.fitBounds(bounds, { padding: FOCUS_PADDING, maxZoom: 15, duration: 0 });
    return;
  }
  if (viewport) applyViewport(map, viewport);
}

export function PoiManagementMap({
  pois,
  selectedPoiId,
  onSelectPoi,
  active,
  categoryColor: categoryColorProp,
  focusBoundsPois = [],
  focusBoundsKey = 0,
}: PoiManagementMapProps) {
  const resolveColor = categoryColorProp ?? defaultCategoryColor;
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<MapLibreMap | null>(null);
  const viewportRef = useRef<MapViewport | null>(null);
  const poisRef = useRef(pois);
  const onSelectRef = useRef(onSelectPoi);
  const resolveColorRef = useRef(resolveColor);
  const focusBoundsRef = useRef({ key: 0, pois: [] as MapPoi[] });
  const markersRef = useRef<Map<string, Marker>>(new Map());
  const selectedPoiIdRef = useRef(selectedPoiId);
  const [mapStatus, setMapStatus] = useState<"loading" | "ready" | "fallback" | "error">("loading");
  const [mapMessage, setMapMessage] = useState("Loading map…");

  poisRef.current = pois;
  onSelectRef.current = onSelectPoi;
  resolveColorRef.current = resolveColor;
  focusBoundsRef.current = { key: focusBoundsKey, pois: focusBoundsPois };
  selectedPoiIdRef.current = selectedPoiId;

  const applyCategoryFocus = useCallback(() => {
    const map = mapRef.current;
    if (!map || !active) return;
    const { key, pois: focusPois } = focusBoundsRef.current;
    if (key === 0 || focusPois.length === 0) return;

    const runFocus = () => {
      map.resize();
      focusMapOnPois(map, focusPois);
    };

    if (map.isStyleLoaded()) {
      requestAnimationFrame(runFocus);
      return;
    }
    map.once("load", () => requestAnimationFrame(runFocus));
  }, [active]);

  const applyCategoryFocusRef = useRef(applyCategoryFocus);
  applyCategoryFocusRef.current = applyCategoryFocus;

  const syncMarkers = useCallback(() => {
    const map = mapRef.current;
    if (!map || !map.isStyleLoaded()) return;

    const markerMap = markersRef.current;
    const nextIds = new Set(pois.map((poi) => poi.id));

    for (const [id, marker] of markerMap) {
      if (!nextIds.has(id)) {
        marker.remove();
        markerMap.delete(id);
      }
    }

    for (const poi of pois) {
      const color = resolveColor(poi.category);
      const selected = poi.id === selectedPoiIdRef.current;
      const existing = markerMap.get(poi.id);

      if (existing) {
        const element = existing.getElement();
        renderPoiMarkerContent(element, poi, color, selected);
        existing.setLngLat([poi.longitude, poi.latitude]);
        continue;
      }

      const element = createPoiMarkerElement(poi, color, selected, (poiId) => {
        onSelectRef.current(poiId);
      });
      const marker = new maplibregl.Marker({ element, anchor: "center" })
        .setLngLat([poi.longitude, poi.latitude])
        .addTo(map);
      markerMap.set(poi.id, marker);
    }
  }, [pois, resolveColor]);

  useEffect(() => {
    let cancelled = false;
    const container = containerRef.current;
    if (!container) return;

    let map: MapLibreMap | null = null;

    async function initMap() {
      try {
        const [viewport, headwayStyle] = await Promise.all([
          loadMapViewport(),
          loadResolvedHeadwayStyle(),
        ]);
        if (cancelled) return;

        viewportRef.current = viewport;
        const style = headwayStyle ?? OSM_FALLBACK_STYLE;
        if (!headwayStyle) {
          setMapStatus("fallback");
          setMapMessage("Headway tileserver unavailable — using OpenStreetMap fallback.");
        }

        map = new maplibregl.Map({
          container: container as HTMLElement,
          style,
          center: [-0.12, 51.5],
          zoom: 10,
          attributionControl: { compact: true },
        });
        mapRef.current = map;

        map.addControl(new maplibregl.NavigationControl({ showCompass: false }), "top-left");

        map.on("load", () => {
          if (cancelled || !map) return;
          if (focusBoundsRef.current.key === 0) {
            fitInitialViewport(map, viewportRef.current, poisRef.current);
          }
          setMapStatus(headwayStyle ? "ready" : "fallback");
          if (headwayStyle) setMapMessage("");
          requestAnimationFrame(() => {
            map?.resize();
            syncMarkers();
            applyCategoryFocusRef.current();
          });
        });

        map.on("style.load", () => {
          if (cancelled || !map) return;
          requestAnimationFrame(() => {
            syncMarkers();
            applyCategoryFocusRef.current();
          });
        });
      } catch {
        if (!cancelled) {
          setMapStatus("error");
          setMapMessage("Unable to load map.");
        }
      }
    }

    void initMap();

    return () => {
      cancelled = true;
      for (const marker of markersRef.current.values()) {
        marker.remove();
      }
      markersRef.current.clear();
      map?.remove();
      mapRef.current = null;
    };
  }, [syncMarkers]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !active) return;
    requestAnimationFrame(() => {
      map.resize();
      syncMarkers();
    });
  }, [active, syncMarkers]);

  useEffect(() => {
    syncMarkers();
  }, [syncMarkers, selectedPoiId]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !map.isStyleLoaded() || !selectedPoiId) return;
    const selected = poisRef.current.find((poi) => poi.id === selectedPoiId);
    if (!selected) return;
    map.easeTo({
      center: [selected.longitude, selected.latitude],
      zoom: Math.max(map.getZoom(), 15),
      duration: 450,
    });
  }, [selectedPoiId]);

  useEffect(() => {
    applyCategoryFocus();
  }, [active, focusBoundsKey, focusBoundsPois, mapStatus, applyCategoryFocus]);

  return (
    <div className={active ? "relative h-[600px]" : "relative h-0 overflow-hidden"}>
      <style>{`
        .poi-map-marker-hit {
          border: none;
          background: transparent;
          padding: 0;
          cursor: pointer;
          line-height: 0;
        }
        .poi-map-marker-hit:focus-visible {
          outline: 2px solid #3b82f6;
          outline-offset: 2px;
          border-radius: 9999px;
        }
        .poi-map-pin {
          position: relative;
          display: flex;
          align-items: center;
          justify-content: center;
          width: 2.5rem;
          height: 2.5rem;
          border-radius: 9999px;
          border: 2px solid #ffffff;
          box-shadow: 0 4px 14px rgba(0, 0, 0, 0.28);
          transition: transform 150ms ease, box-shadow 150ms ease;
        }
        .poi-map-marker-hit:hover .poi-map-pin {
          transform: scale(1.08);
        }
        .poi-map-pin--selected {
          transform: scale(1.14);
          box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.95), 0 6px 18px rgba(0, 0, 0, 0.35);
        }
        .poi-map-badge {
          position: absolute;
          display: flex;
          align-items: center;
          justify-content: center;
          width: 1.25rem;
          height: 1.25rem;
          border-radius: 9999px;
          border: 2px solid #ffffff;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
          pointer-events: none;
        }
      `}</style>
      <div ref={containerRef} className="absolute inset-0 min-h-[600px]" />
      {active && mapMessage && (
        <div className="absolute top-4 left-4 z-10 max-w-sm rounded-lg border border-border bg-card/95 px-3 py-2 text-xs text-muted-foreground shadow-lg backdrop-blur">
          {mapMessage}
        </div>
      )}
      {active && mapStatus === "error" && (
        <div className="absolute inset-0 flex items-center justify-center bg-background/80 text-sm text-muted-foreground">
          Unable to load map.
        </div>
      )}
      {active && pois.length === 0 && mapStatus !== "loading" && (
        <div className="pointer-events-none absolute bottom-4 left-1/2 z-10 -translate-x-1/2 rounded-lg border border-border bg-card/95 px-4 py-2 text-sm text-muted-foreground shadow-lg">
          No POIs with coordinates to display on the map.
        </div>
      )}
    </div>
  );
}
