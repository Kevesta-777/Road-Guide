import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { apiGet } from "../lib/api";

/**
 * Result returned by every admin data hook.
 *
 * `data` is always defined (either live API data or the supplied fallback),
 * so screens can render mock fixtures while the gateway is unreachable. UI
 * code can still inspect `loading` / `error` if it wants to show banners.
 */
export type AdminResource<T> = {
  data: T;
  loading: boolean;
  error: string | null;
  refresh: () => void;
};

/**
 * Fetch any admin endpoint and surface a typed `AdminResource<T>`.
 *
 * The hook is intentionally generic: a screen that wants the raw response
 * body (object, not collection) can simply use `useAdmin<MyShape>(...)`.
 */
export function useAdmin<T>(path: string, fallback: T): AdminResource<T> {
  const [data, setData] = useState<T>(fallback);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const fallbackRef = useRef(fallback);
  fallbackRef.current = fallback;

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await apiGet<T>(path);
      setData(result);
    } catch (e) {
      setError(e instanceof Error ? e.message : "unreachable");
      setData(fallbackRef.current);
    } finally {
      setLoading(false);
    }
  }, [path]);

  useEffect(() => {
    load();
  }, [load]);

  return useMemo(() => ({ data, loading, error, refresh: load }), [data, loading, error, load]);
}

/**
 * Convenience wrapper for endpoints that return `{ items: T[] }` — most
 * admin list endpoints follow that envelope. Returns `T[]` directly and the
 * supplied `fallback` doubles as the empty/in-flight state.
 */
export function useAdminCollection<T>(path: string, fallback: T[]): AdminResource<T[]> {
  const wrappedFallback = useMemo(() => ({ items: fallback }), [fallback]);
  const { data, loading, error, refresh } = useAdmin<{ items: T[] }>(path, wrappedFallback);
  return useMemo(
    () => ({ data: data.items ?? fallback, loading, error, refresh }),
    [data, fallback, loading, error, refresh],
  );
}
