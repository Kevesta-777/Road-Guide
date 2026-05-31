import { useEffect, useMemo, useState } from "react";
import {
  categoryById,
  categoryColor,
  displayCategoryLabel,
  loadMapPoiCategories,
  normalizeCategoryId,
  type PoiCategory,
} from "./poiCategories";

export function useMapPoiCategories() {
  const [categories, setCategories] = useState<PoiCategory[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    loadMapPoiCategories()
      .then((loaded) => {
        if (!cancelled) setCategories(loaded);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const helpers = useMemo(
    () => ({
      normalizeId: normalizeCategoryId,
      labelFor: (raw: string) => displayCategoryLabel(categories, raw),
      colorFor: (raw: string) => {
        const normalized = normalizeCategoryId(raw);
        return categoryById(categories, normalized)?.color ?? categoryColor(normalized);
      },
    }),
    [categories],
  );

  return { categories, loading, ...helpers };
}
