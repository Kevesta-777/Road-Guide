package utils

import (
	"database/sql"
	"strconv"
	"strings"
)

func RebindPostgres(query string) string {
	if !strings.Contains(query, "?") {
		return query
	}
	var builder strings.Builder
	builder.Grow(len(query) + 12)
	placeholder := 1
	for _, ch := range query {
		if ch == '?' {
			builder.WriteString("$")
			builder.WriteString(strconv.Itoa(placeholder))
			placeholder++
			continue
		}
		builder.WriteRune(ch)
	}
	return builder.String()
}

func TxExec(tx *sql.Tx, query string, args ...any) (sql.Result, error) {
	return tx.Exec(RebindPostgres(query), args...)
}
