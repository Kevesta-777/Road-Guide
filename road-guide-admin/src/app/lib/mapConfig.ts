import type { StyleSpecification } from "maplibre-gl";

/** Headway tileserver style — same path as the Android app (MapServerConfig / AppMapStyle). */
export const MAP_STYLE_URL =
  import.meta.env.VITE_MAP_STYLE_URL ?? "/tileserver/styles/basic/style.json";

const MAP_STYLE_CANDIDATES = [
  MAP_STYLE_URL,
  "/tileserver/styles/basic/style.json",
  "/tileserver/style/basic.json",
] as const;

const AREMAP_CANDIDATES = ["/tileserver/areamap", "/tileserver/data/default.json", "/areamap"] as const;

export type MapViewport = {
  bounds?: [[number, number], [number, number]];
  center?: [number, number];
  zoom?: number;
};

/** Rewrite absolute tileserver URLs to same-origin paths so Vite proxy can serve them. */
function rewriteTileserverUrl(value: string): string {
  if (value.startsWith("/tileserver/") || value === "/areamap") {
    return value;
  }

  // MapLibre glyphs/tiles use literal `{fontstack}`, `{z}`, etc. — `new URL()` percent-encodes
  // those braces and MapLibre then rejects the glyphs URL.
  if (value.includes("{")) {
    return value
      .replace(/^https?:\/\/[^/]+(?=\/tileserver\/)/i, "")
      .replace(/^https?:\/\/[^/]+(?=\/areamap(?:[/?#]|$))/i, "");
  }

  try {
    const parsed = new URL(value, window.location.origin);
    if (parsed.pathname.startsWith("/tileserver/") || parsed.pathname === "/areamap") {
      return `${parsed.pathname}${parsed.search}`;
    }
  } catch {
    // fall through to regex replacements
  }
  return value
    .replace(/https?:\/\/[^/]+(\/tileserver\/[^"'`\s]+)/gi, "$1")
    .replace(/https?:\/\/[^/]+(\/areamap)/gi, "$1");
}

function rewriteStyleNode(node: unknown): void {
  if (Array.isArray(node)) {
    for (let index = 0; index < node.length; index += 1) {
      const value = node[index];
      if (typeof value === "string") {
        node[index] = rewriteTileserverUrl(value);
      } else {
        rewriteStyleNode(value);
      }
    }
    return;
  }
  if (!node || typeof node !== "object") return;
  for (const [key, value] of Object.entries(node)) {
    if (typeof value === "string") {
      (node as Record<string, unknown>)[key] = rewriteTileserverUrl(value);
    } else {
      rewriteStyleNode(value);
    }
  }
}

export async function loadResolvedHeadwayStyle(): Promise<StyleSpecification | null> {
  for (const path of MAP_STYLE_CANDIDATES) {
    try {
      const response = await fetch(path, { redirect: "follow" });
      if (!response.ok) continue;
      const style = (await response.json()) as StyleSpecification;
      rewriteStyleNode(style);
      return style;
    } catch {
      continue;
    }
  }
  return null;
}

export async function loadMapViewport(): Promise<MapViewport | null> {
  for (const path of AREMAP_CANDIDATES) {
    try {
      const response = await fetch(path, { redirect: "follow" });
      if (!response.ok) continue;
      const data = (await response.json()) as {
        bounds?: [number, number, number, number];
        center?: [number, number];
        zoom?: number;
      };
      if (Array.isArray(data.bounds) && data.bounds.length === 4) {
        const [west, south, east, north] = data.bounds;
        return {
          bounds: [
            [west, south],
            [east, north],
          ],
        };
      }
      if (Array.isArray(data.center) && data.center.length >= 2) {
        return {
          center: [data.center[0], data.center[1]],
          zoom: typeof data.zoom === "number" ? data.zoom : 10,
        };
      }
    } catch {
      continue;
    }
  }
  return null;
}

export const OSM_FALLBACK_STYLE: StyleSpecification = {
  version: 8,
  sources: {
    osm: {
      type: "raster",
      tiles: ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
      tileSize: 256,
      attribution: "© OpenStreetMap contributors",
    },
  },
  layers: [{ id: "osm", type: "raster", source: "osm" }],
};
