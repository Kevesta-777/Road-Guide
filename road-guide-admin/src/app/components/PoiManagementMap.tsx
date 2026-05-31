import { useEffect, useMemo, useRef, useState } from "react";
import maplibregl, { type GeoJSONSource, type Map as MapLibreMap } from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
import {
  loadMapViewport,
  loadResolvedHeadwayStyle,
  OSM_FALLBACK_STYLE,
  type MapViewport,
} from "../lib/mapConfig";

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
};

const POI_SOURCE_ID = "admin-business-pois";
const POI_LAYER_ID = "admin-business-pois-circles";
const POI_SELECTED_LAYER_ID = "admin-business-pois-selected";

const CATEGORY_COLORS: Record<string, string> = {
  Restaurant: "#ef4444",
  Shop: "#3b82f6",
  Entertainment: "#a855f7",
  Services: "#22c55e",
  Other: "#6b7280",
};

function categoryColor(category: string): string {
  return CATEGORY_COLORS[category] ?? CATEGORY_COLORS.Other;
}

function poisToGeoJson(pois: MapPoi[]): GeoJSON.FeatureCollection {
  return {
    type: "FeatureCollection",
    features: pois.map((poi) => ({
      type: "Feature",
      geometry: {
        type: "Point",
        coordinates: [poi.longitude, poi.latitude],
      },
      properties: {
        id: poi.id,
        name: poi.name,
        category: poi.category,
        status: poi.status,
        claimedBy: poi.claimedBy ?? "",
        color: categoryColor(poi.category),
      },
    })),
  };
}

function boundsFromPois(pois: MapPoi[]): maplibregl.LngLatBoundsLike | null {
  if (pois.length === 0) return null;
  let west = pois[0].longitude;
  let east = pois[0].longitude;
  let south = pois[0].latitude;
  let north = pois[0].latitude;
  for (const poi of pois) {
    west = Math.min(west, poi.longitude);
    east = Math.max(east, poi.longitude);
    south = Math.min(south, poi.latitude);
    north = Math.max(north, poi.latitude);
  }
  return [
    [west, south],
    [east, north],
  ];
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

function upsertPoiLayers(map: MapLibreMap) {
  if (!map.getSource(POI_SOURCE_ID)) {
    map.addSource(POI_SOURCE_ID, {
      type: "geojson",
      data: { type: "FeatureCollection", features: [] },
    });
  }

  if (!map.getLayer(POI_LAYER_ID)) {
    map.addLayer({
      id: POI_LAYER_ID,
      type: "circle",
      source: POI_SOURCE_ID,
      paint: {
        "circle-radius": ["interpolate", ["linear"], ["zoom"], 8, 5, 14, 10],
        "circle-color": ["get", "color"],
        "circle-stroke-width": 2,
        "circle-stroke-color": "#ffffff",
        "circle-opacity": 0.92,
      },
    });
  }

  if (!map.getLayer(POI_SELECTED_LAYER_ID)) {
    map.addLayer({
      id: POI_SELECTED_LAYER_ID,
      type: "circle",
      source: POI_SOURCE_ID,
      filter: ["==", ["get", "id"], ""],
      paint: {
        "circle-radius": ["interpolate", ["linear"], ["zoom"], 8, 8, 14, 14],
        "circle-color": ["get", "color"],
        "circle-stroke-width": 3,
        "circle-stroke-color": "#ffffff",
        "circle-opacity": 1,
      },
    });
  }
}

function finalizeMapView(map: MapLibreMap, viewport: MapViewport | null, pois: MapPoi[]) {
  upsertPoiLayers(map);
  const source = map.getSource(POI_SOURCE_ID) as GeoJSONSource | undefined;
  source?.setData(poisToGeoJson(pois));

  const poiBounds = boundsFromPois(pois);
  if (poiBounds) {
    map.fitBounds(poiBounds, { padding: 72, maxZoom: 15, duration: 0 });
  } else if (viewport) {
    applyViewport(map, viewport);
  }
}

export function PoiManagementMap({ pois, selectedPoiId, onSelectPoi, active }: PoiManagementMapProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<MapLibreMap | null>(null);
  const viewportRef = useRef<MapViewport | null>(null);
  const poisRef = useRef(pois);
  const onSelectRef = useRef(onSelectPoi);
  const [mapStatus, setMapStatus] = useState<"loading" | "ready" | "fallback" | "error">("loading");
  const [mapMessage, setMapMessage] = useState("Loading map…");

  const geoJson = useMemo(() => poisToGeoJson(pois), [pois]);
  poisRef.current = pois;
  onSelectRef.current = onSelectPoi;

  useEffect(() => {
    let cancelled = false;
    const container = containerRef.current;
    if (!container) return;

    let map: MapLibreMap | null = null;

    const mapContainer = container

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
          container: mapContainer,
          style,
          center: [-0.12, 51.5],
          zoom: 10,
          attributionControl: { compact: true },
        });
        mapRef.current = map;

        map.addControl(new maplibregl.NavigationControl({ showCompass: false }), "top-left");

        map.on("load", () => {
          if (cancelled || !map) return;
          finalizeMapView(map, viewportRef.current, poisRef.current);
          setMapStatus(headwayStyle ? "ready" : "fallback");
          if (headwayStyle) setMapMessage("");
          requestAnimationFrame(() => map?.resize());
        });

        map.on("style.load", () => {
          if (cancelled || !map) return;
          finalizeMapView(map, viewportRef.current, poisRef.current);
        });

        map.on("click", POI_LAYER_ID, (event) => {
          const feature = event.features?.[0];
          const poiId = feature?.properties?.id;
          if (typeof poiId === "string" && poiId) {
            onSelectRef.current(poiId);
          }
        });

        map.on("mouseenter", POI_LAYER_ID, () => {
          map?.getCanvas().style.setProperty("cursor", "pointer");
        });
        map.on("mouseleave", POI_LAYER_ID, () => {
          map?.getCanvas().style.removeProperty("cursor");
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
      map?.remove();
      mapRef.current = null;
    };
  }, []);

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !active) return;
    requestAnimationFrame(() => {
      map.resize();
    });
  }, [active]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !map.isStyleLoaded()) return;
    const source = map.getSource(POI_SOURCE_ID) as GeoJSONSource | undefined;
    source?.setData(geoJson);
  }, [geoJson]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !map.isStyleLoaded()) return;
    if (map.getLayer(POI_SELECTED_LAYER_ID)) {
      map.setFilter(POI_SELECTED_LAYER_ID, ["==", ["get", "id"], selectedPoiId ?? ""]);
    }
    if (!selectedPoiId) return;
    const selected = pois.find((poi) => poi.id === selectedPoiId);
    if (!selected) return;
    map.easeTo({
      center: [selected.longitude, selected.latitude],
      zoom: Math.max(map.getZoom(), 13),
      duration: 450,
    });
  }, [selectedPoiId, pois]);

  return (
    <div className={active ? "relative h-[600px]" : "relative h-0 overflow-hidden"}>
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
