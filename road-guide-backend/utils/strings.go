package utils

import "strings"

func UniqueStrings(items []string) []string {
	seen := map[string]struct{}{}
	result := make([]string, 0, len(items))
	for _, item := range items {
		item = strings.TrimSpace(item)
		if item == "" {
			continue
		}
		if _, ok := seen[item]; ok {
			continue
		}
		seen[item] = struct{}{}
		result = append(result, item)
	}
	return result
}

func NormalizeIdentifier(value string) string {
	return strings.ToLower(strings.TrimSpace(value))
}

func NormalizePlaceName(name string) string {
	return strings.ToLower(strings.TrimSpace(name))
}

func MathAbs(v float64) float64 {
	if v < 0 {
		return -v
	}
	return v
}
