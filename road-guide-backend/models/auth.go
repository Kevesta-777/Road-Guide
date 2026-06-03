package models

import "github.com/golang-jwt/jwt/v5"

type ClaimGuidance struct {
	ContactPhone               string `json:"contactPhone"`
	RegistrationAgentAddress   string `json:"registrationAgentAddress"`
	AvailableRegistrationHours string `json:"availableRegistrationHours"`
	AdditionalInstructions     string `json:"additionalInstructions"`
}

type AuthClaims struct {
	UserID string `json:"userId"`
	Role   string `json:"role"`
	jwt.RegisteredClaims
}
