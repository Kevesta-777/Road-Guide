package utils

import (
	"context"
	"net/http"

	"github.com/gin-gonic/gin"
)

const GinContextKey contextKey = "ginCtx"

type contextKey string

func URLParam(r *http.Request, name string) string {
	if c, ok := r.Context().Value(GinContextKey).(*gin.Context); ok && c != nil {
		return c.Param(name)
	}
	return ""
}

func GinWrap(h func(http.ResponseWriter, *http.Request)) gin.HandlerFunc {
	return func(c *gin.Context) {
		ctx := context.WithValue(c.Request.Context(), GinContextKey, c)
		h(c.Writer, c.Request.WithContext(ctx))
	}
}

func InjectGinContext() gin.HandlerFunc {
	return func(c *gin.Context) {
		ctx := context.WithValue(c.Request.Context(), GinContextKey, c)
		c.Request = c.Request.WithContext(ctx)
		c.Next()
	}
}
