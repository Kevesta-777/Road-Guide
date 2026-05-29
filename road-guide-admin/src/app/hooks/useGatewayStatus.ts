import { useEffect, useState } from "react";

/**
 * Shape returned by `GET /api/v1/config/maps`.
 *
 * `tiles` is the only direct upstream the client is expected to call
 * (Martin). Routing, search and transit are gateway-relative paths so the
 * web/mobile clients never need to know about Valhalla, Pelias, or OTP
 * hostnames.
 */
export type MapsConfig = {
  tiles: string;
  style: string;
  routing: string;
  search: string;
  transit: string;
};

export type ServiceState = "loading" | "ok" | "down";

export type GatewayStatus = {
  health: ServiceState;
  healthMessage: string;
  maps: MapsConfig | null;
  mapsState: "loading" | "ready" | "unavailable";
  upstreams: Record<"tiles" | "routing" | "search" | "transit", ServiceState>;
};

const INITIAL: GatewayStatus = {
  health: "loading",
  healthMessage: "",
  maps: null,
  mapsState: "loading",
  upstreams: { tiles: "loading", routing: "loading", search: "loading", transit: "loading" },
};

async function probe(url: string): Promise<ServiceState> {
  try {
    const r = await fetch(url, { method: "GET" });
    // OTP/Pelias respond 200 only on real endpoints; root paths often return
    // 404. We treat anything in [200,499] as "process responded" — DNS or
    // network failures land us in the catch block below.
    return r.status >= 500 ? "down" : "ok";
  } catch {
    return "down";
  }
}

/**
 * Polls the SuperMap gateway:
 *   - `/health` for gateway liveness
 *   - `/api/v1/config/maps` for the recommended map config
 *   - Each upstream (`tiles` direct, `routing` / `search` / `transit` via
 *     the gateway) to render per-service pills in the banner.
 */
export function useGatewayStatus(refreshMs = 15_000): GatewayStatus {
  const [status, setStatus] = useState<GatewayStatus>(INITIAL);

  useEffect(() => {
    let cancelled = false;

    const tick = async () => {
      // Gateway health
      try {
        const r = await fetch("/health");
        const text = await r.text();
        if (!cancelled) {
          setStatus((s) => ({
            ...s,
            health: r.ok ? "ok" : "down",
            healthMessage: text.trim() || (r.ok ? "ok" : `HTTP ${r.status}`),
          }));
        }
      } catch (e) {
        if (!cancelled) {
          setStatus((s) => ({
            ...s,
            health: "down",
            healthMessage: e instanceof Error ? e.message : "unreachable",
          }));
        }
      }

      // Maps config + per-upstream probes
      try {
        const r = await fetch("/api/v1/config/maps");
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        const maps = (await r.json()) as MapsConfig;
        const [tiles, routing, search, transit] = await Promise.all([
          maps.tiles ? probe(maps.tiles) : Promise.resolve<ServiceState>("down"),
          probe(`${maps.routing}/`),
          probe(`${maps.search}/`),
          probe(`${maps.transit}/`),
        ]);
        if (!cancelled) {
          setStatus((s) => ({
            ...s,
            maps,
            mapsState: "ready",
            upstreams: { tiles, routing, search, transit },
          }));
        }
      } catch {
        if (!cancelled) {
          setStatus((s) => ({
            ...s,
            maps: null,
            mapsState: "unavailable",
            upstreams: { tiles: "down", routing: "down", search: "down", transit: "down" },
          }));
        }
      }
    };

    tick();
    const id = window.setInterval(tick, refreshMs);
    return () => {
      cancelled = true;
      window.clearInterval(id);
    };
  }, [refreshMs]);

  return status;
}
