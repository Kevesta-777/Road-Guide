import {
  AlertTriangle,
  CheckCircle2,
  Compass,
  Loader2,
  MapPin,
  Route,
  Train,
} from "lucide-react";
import { useGatewayStatus, type ServiceState } from "../hooks/useGatewayStatus";

/**
 * Live status pills rendered above each page header.
 *
 * The architecture is read straight off `GET /api/v1/config/maps`:
 *   - `tiles`   → Martin (direct)
 *   - `routing` → /api/v1/routing  (Valhalla through the gateway)
 *   - `search`  → /api/v1/search   (Pelias through the gateway)
 *   - `transit` → /api/v1/transit  (OTP through the gateway)
 */
export function GatewayStatusBanner() {
  const status = useGatewayStatus();

  return (
    <div className="flex flex-wrap items-center gap-2 text-xs">
      <Pill state={status.health} icon="health" label={gatewayLabel(status.health, status.healthMessage)} />
      <Pill state={status.upstreams.tiles} icon="tiles" label={upstreamLabel("Tiles (Martin)", status.upstreams.tiles)} />
      <Pill state={status.upstreams.routing} icon="routing" label={upstreamLabel("Routing (Valhalla)", status.upstreams.routing)} />
      <Pill state={status.upstreams.search} icon="search" label={upstreamLabel("Search (Pelias)", status.upstreams.search)} />
      <Pill state={status.upstreams.transit} icon="transit" label={upstreamLabel("Transit (OTP)", status.upstreams.transit)} />
    </div>
  );
}

function gatewayLabel(state: ServiceState, message: string): string {
  if (state === "loading") return "Checking gateway…";
  if (state === "ok") return `Gateway: ${message || "ok"}`;
  return `Gateway down: ${message || "unreachable"}`;
}

function upstreamLabel(name: string, state: ServiceState): string {
  if (state === "loading") return `${name}: checking…`;
  if (state === "ok") return `${name}: ready`;
  return `${name}: unavailable`;
}

function Pill({
  state,
  icon,
  label,
}: {
  state: ServiceState;
  icon: "health" | "tiles" | "routing" | "search" | "transit";
  label: string;
}) {
  const tone =
    state === "loading"
      ? "text-muted-foreground"
      : state === "ok"
        ? "text-green-600 dark:text-green-400"
        : "text-yellow-600 dark:text-yellow-400";

  const Icon = iconFor(icon, state);
  return (
    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-card border border-border">
      <Icon className={`w-4 h-4 ${tone}`} />
      <span className="text-muted-foreground">{label}</span>
    </span>
  );
}

function iconFor(kind: "health" | "tiles" | "routing" | "search" | "transit", state: ServiceState) {
  if (state === "loading") return Loader2WithSpin;
  if (kind === "health") {
    return state === "ok" ? CheckCircle2 : AlertTriangle;
  }
  switch (kind) {
    case "tiles":
      return MapPin;
    case "routing":
      return Route;
    case "search":
      return Compass;
    case "transit":
      return Train;
  }
}

function Loader2WithSpin(props: React.ComponentProps<typeof Loader2>) {
  return <Loader2 {...props} className={`${props.className ?? ""} animate-spin`} />;
}
