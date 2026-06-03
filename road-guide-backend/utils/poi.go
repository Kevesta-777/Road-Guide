package utils

import "strings"

func NormalizedCategory(raw string) string {
	return strings.TrimSpace(raw)
}

func CategoryFromMetadata(metadata map[string]any) string {
	if metadata == nil {
		return ""
	}
	raw, _ := metadata["category"].(string)
	return NormalizedCategory(raw)
}

func MetadataWithCategory(existing map[string]any, category string) map[string]any {
	meta := existing
	if meta == nil {
		meta = map[string]any{}
	}
	if category = NormalizedCategory(category); category != "" {
		meta["category"] = category
	}
	return meta
}

func WithPOICategory(poi map[string]any) map[string]any {
	if cat, ok := poi["category"].(string); ok && NormalizedCategory(cat) != "" {
		poi["category"] = NormalizedCategory(cat)
		return poi
	}
	metadata, _ := poi["metadata"].(map[string]any)
	poi["category"] = CategoryFromMetadata(metadata)
	return poi
}

func PanoramaStatusLabel(status string) string {
	switch strings.ToLower(strings.TrimSpace(status)) {
	case "approved":
		return "Approved"
	case "rejected":
		return "Rejected"
	default:
		return "Pending"
	}
}
