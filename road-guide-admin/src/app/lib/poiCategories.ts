import type { StyleSpecification } from "maplibre-gl";
import { loadResolvedHeadwayStyle } from "./mapConfig";

export type PoiCategory = {
  id: string;
  label: string;
  color: string;
};

/** OpenMapTiles POI classes from https://openmaptiles.org/schema/#poi */
const OPENMAPTILES_CLASS_COLORS: Record<string, string> = {
  aerialway: "#0891b2",
  alcohol_shop: "#f59e0b",
  art_gallery: "#db2777",
  atm: "#059669",
  attraction: "#e879f9",
  bar: "#dc2626",
  beer: "#b45309",
  bus: "#0284c7",
  cafe: "#d97706",
  campsite: "#65a30d",
  car: "#64748b",
  castle: "#92400e",
  cemetery: "#78716c",
  clothing_store: "#be185d",
  college: "#8b5cf6",
  entrance: "#0ea5e9",
  fast_food: "#ea580c",
  fuel: "#1d4ed8",
  golf: "#16a34a",
  grocery: "#eab308",
  harbor: "#2563eb",
  hospital: "#ef4444",
  ice_cream: "#ec4899",
  laundry: "#14b8a6",
  library: "#7c3aed",
  lodging: "#a855f7",
  music: "#c026d3",
  office: "#475569",
  park: "#22c55e",
  post: "#ca8a04",
  railway: "#0369a1",
  school: "#9333ea",
  shop: "#2563eb",
  stadium: "#15803d",
  swimming: "#06b6d4",
  town_hall: "#4f46e5",
  zoo: "#84cc16",
  airport: "#0284c7",
  bank: "#047857",
  cinema: "#a21caf",
  dentist: "#f43f5e",
  doctors: "#fb7185",
  fire_station: "#b91c1c",
  museum: "#9333ea",
  pharmacy: "#10b981",
  place_of_worship: "#6b7280",
  police: "#1e40af",
  restaurant: "#ef4444",
  theatre: "#7e22ce",
};

/** Legacy admin bucket names mapped to OpenMapTiles class ids. */
const LEGACY_CATEGORY_IDS: Record<string, string> = {
  restaurant: "restaurant",
  shop: "shop",
  entertainment: "attraction",
  services: "office",
  other: "shop",
};

const FALLBACK_CATEGORIES: PoiCategory[] = Object.entries(OPENMAPTILES_CLASS_COLORS)
  .map(([id, color]) => ({ id, label: formatCategoryLabel(id), color }))
  .sort((a, b) => a.label.localeCompare(b.label));

export function formatCategoryLabel(id: string): string {
  return id
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

export function normalizeCategoryId(raw: string): string {
  const trimmed = raw.trim();
  if (!trimmed) return "";
  const legacy = LEGACY_CATEGORY_IDS[trimmed.toLowerCase()];
  if (legacy) return legacy;
  const direct = trimmed.toLowerCase().replace(/\s+/g, "_");
  if (OPENMAPTILES_CLASS_COLORS[direct]) return direct;
  return direct;
}

function hashColor(id: string): string {
  let hash = 0;
  for (let index = 0; index < id.length; index += 1) {
    hash = id.charCodeAt(index) + ((hash << 5) - hash);
  }
  const hue = Math.abs(hash) % 360;
  return `hsl(${hue} 62% 42%)`;
}

export function categoryColor(id: string, styleColors?: Map<string, string>): string {
  const normalized = normalizeCategoryId(id);
  if (!normalized) return "#6b7280";
  return styleColors?.get(normalized) ?? OPENMAPTILES_CLASS_COLORS[normalized] ?? hashColor(normalized);
}

export function buildCategoryRegistry(styleColors?: Map<string, string>): PoiCategory[] {
  const ids = new Set<string>(Object.keys(OPENMAPTILES_CLASS_COLORS));
  styleColors?.forEach((_color, id) => ids.add(id));

  return Array.from(ids)
    .map((id) => ({
      id,
      label: formatCategoryLabel(id),
      color: categoryColor(id, styleColors),
    }))
    .sort((a, b) => a.label.localeCompare(b.label));
}

function isPoiLayer(layer: Record<string, unknown>): boolean {
  return layer["source-layer"] === "poi";
}

function collectFilterValues(node: unknown, field: string, out: Set<string>): void {
  if (!Array.isArray(node)) return;

  const op = node[0];
  if (op === "==" && node[1] === field && typeof node[2] === "string") {
    out.add(node[2]);
  }
  if (op === "in" && node[1] === field) {
    for (let index = 2; index < node.length; index += 1) {
      if (typeof node[index] === "string") out.add(node[index]);
    }
  }
  if (op === "match" || op === "case") {
    for (let index = 2; index < node.length - 1; index += 2) {
      const key = node[index];
      if (typeof key === "string") {
        out.add(key);
      } else if (Array.isArray(key)) {
        for (const item of key) {
          if (typeof item === "string") out.add(item);
        }
      }
    }
  }

  for (const item of node) {
    if (Array.isArray(item)) collectFilterValues(item, field, out);
  }
}

function collectMatchColors(node: unknown, out: Map<string, string>): void {
  if (!Array.isArray(node) || node[0] !== "match") return;

  for (let index = 2; index < node.length - 1; index += 2) {
    const key = node[index];
    const value = node[index + 1];
    if (typeof value !== "string" || !value.startsWith("#")) continue;

    if (typeof key === "string") {
      out.set(normalizeCategoryId(key), value);
    } else if (Array.isArray(key)) {
      for (const item of key) {
        if (typeof item === "string") out.set(normalizeCategoryId(item), value);
      }
    }
  }
}

function extractStyleCategoryColors(style: StyleSpecification): Map<string, string> {
  const colors = new Map<string, string>();
  const classIds = new Set<string>();

  for (const layer of style.layers ?? []) {
    const record = layer as Record<string, unknown>;
    if (!isPoiLayer(record)) continue;

    collectFilterValues(record.filter, "class", classIds);
    collectFilterValues(record.filter, "subclass", classIds);

    const paint = record.paint as Record<string, unknown> | undefined;
    if (paint) {
      for (const key of ["icon-color", "circle-color", "text-color"] as const) {
        const value = paint[key];
        if (typeof value === "string" && value.startsWith("#")) {
          classIds.forEach((id) => {
            if (!colors.has(id)) colors.set(id, value);
          });
        } else {
          collectMatchColors(value, colors);
        }
      }
    }
  }

  classIds.forEach((id) => {
    const normalized = normalizeCategoryId(id);
    if (!colors.has(normalized)) {
      colors.set(normalized, categoryColor(normalized));
    }
  });

  return colors;
}

export async function loadMapPoiCategories(): Promise<PoiCategory[]> {
  const style = await loadResolvedHeadwayStyle();
  if (!style) return FALLBACK_CATEGORIES;
  const styleColors = extractStyleCategoryColors(style);
  return buildCategoryRegistry(styleColors);
}

export function categoryById(categories: PoiCategory[], id: string): PoiCategory | undefined {
  const normalized = normalizeCategoryId(id);
  return categories.find((category) => category.id === normalized);
}

export function displayCategoryLabel(categories: PoiCategory[], raw: string): string {
  const normalized = normalizeCategoryId(raw);
  if (!normalized) return "Unknown";
  return categoryById(categories, normalized)?.label ?? formatCategoryLabel(normalized);
}
