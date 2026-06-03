package routers

import (
	"net/http"

	"ROAD-GUIDE-BACKEND/config"
	"ROAD-GUIDE-BACKEND/controllers"
	"ROAD-GUIDE-BACKEND/middleware"
	"ROAD-GUIDE-BACKEND/models"
	"ROAD-GUIDE-BACKEND/services"
	"ROAD-GUIDE-BACKEND/utils"

	"github.com/gin-gonic/gin"
)

func Setup(cfg config.Config, ctrl *controllers.Controllers, svc *services.Services) *gin.Engine {
	gin.SetMode(gin.ReleaseMode)
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(utils.InjectGinContext())
	r.Use(middleware.CORS())

	r.GET("/healthz", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "ok"})
	})
	r.StaticFS("/uploads", http.Dir(cfg.UploadDir))

	v1 := r.Group("/api/v1")
	{
		v1.POST("/auth/register", utils.GinWrap(ctrl.Auth.Register))
		v1.POST("/auth/login", utils.GinWrap(ctrl.Auth.Login))
		v1.GET("/places/panoramas", utils.GinWrap(ctrl.Place.ListPlacePanoramas))
		v1.GET("/places/detail", utils.GinWrap(ctrl.Place.GetPlaceDetail))

		authed := v1.Group("")
		authed.Use(middleware.RequireAuth(svc))
		{
			authed.GET("/auth/me", utils.GinWrap(ctrl.Auth.Me))
			authed.GET("/subscriptions/status", utils.GinWrap(ctrl.Subscription.GetStatus))

			authed.GET("/business-claims/:poiID", utils.GinWrap(ctrl.Business.ClaimStatus))
			authed.POST("/business-claims/:poiID/requests", utils.GinWrap(ctrl.Business.CreateClaimRequest))
			authed.POST("/business-pois/resolve", utils.GinWrap(ctrl.Business.ResolveBusinessPOI))
			authed.GET("/business-pois/mine", utils.GinWrap(ctrl.Business.ListMyBusinessPOIs))
			authed.GET("/business-pois/:poiID", utils.GinWrap(ctrl.Business.GetBusinessPOI))
			authed.PUT("/business-pois/:poiID", utils.GinWrap(ctrl.Business.UpdateBusinessPOI))
			authed.POST("/business-pois/:poiID/media", utils.GinWrap(ctrl.Business.UploadPOIMedia))
			authed.DELETE("/business-pois/:poiID/media/:mediaID", utils.GinWrap(ctrl.Business.DeletePOIMedia))

			authed.GET("/friends", utils.GinWrap(ctrl.Friend.ListFriends))
			authed.POST("/friends", utils.GinWrap(ctrl.Friend.AddFriend))
			authed.DELETE("/friends/:profileID", utils.GinWrap(ctrl.Friend.RemoveFriend))
			authed.GET("/users/profile/:profileID", utils.GinWrap(ctrl.Friend.LookupUserProfile))

			authed.GET("/companion/driver-posts", utils.GinWrap(ctrl.Companion.ListDriverPosts))
			authed.POST("/companion/driver-posts", utils.GinWrap(ctrl.Companion.CreateDriverPost))
			authed.PATCH("/companion/driver-posts/:postID", utils.GinWrap(ctrl.Companion.UpdateDriverPost))
			authed.POST("/companion/driver-posts/:postID/book", utils.GinWrap(ctrl.Companion.BookDriverPost))
			authed.GET("/companion/passenger-requests", utils.GinWrap(ctrl.Companion.ListPassengerRequests))
			authed.POST("/companion/passenger-requests", utils.GinWrap(ctrl.Companion.CreatePassengerRequest))
			authed.PATCH("/companion/passenger-requests/:requestID", utils.GinWrap(ctrl.Companion.UpdatePassengerRequest))
			authed.GET("/companion/matches/suggested", utils.GinWrap(ctrl.Companion.ListSuggestedMatches))

			admin := authed.Group("/admin")
			admin.Use(middleware.RequireRole(models.RoleAdmin))
			{
				admin.GET("/registration-requests", utils.GinWrap(ctrl.Admin.ListRegistrationRequests))
				admin.POST("/registration-requests/:requestID/approve", utils.GinWrap(ctrl.Admin.ApproveRegistrationRequest))
				admin.POST("/registration-requests/:requestID/reject", utils.GinWrap(ctrl.Admin.RejectRegistrationRequest))
				admin.GET("/users", utils.GinWrap(ctrl.Admin.ListUsers))
				admin.PUT("/users/:userID/role", utils.GinWrap(ctrl.Admin.SetUserRole))
				admin.PUT("/users/:userID/assignments", utils.GinWrap(ctrl.Admin.SetUserAssignments))
				admin.GET("/business-pois", utils.GinWrap(ctrl.Admin.ListBusinessPOIs))
				admin.POST("/business-pois", utils.GinWrap(ctrl.Admin.CreateBusinessPOI))
				admin.GET("/panoramas", utils.GinWrap(ctrl.Admin.ListPanoramas))
				admin.POST("/panoramas/:mediaID/approve", utils.GinWrap(ctrl.Admin.ApprovePanorama))
				admin.POST("/panoramas/:mediaID/reject", utils.GinWrap(ctrl.Admin.RejectPanorama))

				admin.GET("/companion/driver-posts", utils.GinWrap(ctrl.Admin.AdminListCompanionDriverPosts))
				admin.GET("/companion/passenger-requests", utils.GinWrap(ctrl.Admin.AdminListCompanionPassengerRequests))
				admin.GET("/companion/bookings", utils.GinWrap(ctrl.Admin.AdminListCompanionBookings))
				admin.GET("/companion/matches", utils.GinWrap(ctrl.Admin.AdminListCompanionMatches))
				admin.POST("/companion/matches/:matchID/notify", utils.GinWrap(ctrl.Admin.AdminCompanionMatchNotify))
				admin.POST("/companion/matches/:matchID/dismiss", utils.GinWrap(ctrl.Admin.AdminCompanionMatchDismiss))
				admin.PATCH("/companion/driver-posts/:postID/status", utils.GinWrap(ctrl.Admin.AdminCompanionDriverPostStatus))
				admin.PATCH("/companion/passenger-requests/:requestID/status", utils.GinWrap(ctrl.Admin.AdminCompanionPassengerRequestStatus))
				admin.POST("/companion/matches/recompute", utils.GinWrap(ctrl.Admin.AdminCompanionRecomputeMatches))

				admin.GET("/subscriptions/plans", utils.GinWrap(ctrl.Admin.AdminListSubscriptionPlans))
				admin.PUT("/subscriptions/plans/:planID", utils.GinWrap(ctrl.Admin.AdminUpdateSubscriptionPlan))
				admin.GET("/subscriptions/premium-content", utils.GinWrap(ctrl.Admin.AdminListPremiumContent))
				admin.PUT("/subscriptions/premium-content/:contentID", utils.GinWrap(ctrl.Admin.AdminUpdatePremiumContent))
				admin.GET("/subscriptions/users", utils.GinWrap(ctrl.Admin.AdminListUserSubscriptions))
				admin.POST("/subscriptions/users", utils.GinWrap(ctrl.Admin.AdminUpsertUserSubscription))
				admin.PATCH("/subscriptions/users/:subscriptionID", utils.GinWrap(ctrl.Admin.AdminPatchUserSubscription))
			}
		}
	}

	return r
}
