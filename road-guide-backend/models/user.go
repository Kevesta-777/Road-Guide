package models

import "time"

type User struct {
	ID         string    `json:"id"`
	Identifier string    `json:"identifier"`
	Name       string    `json:"name"`
	Role       string    `json:"role"`
	CreatedAt  time.Time `json:"createdAt"`
	UpdatedAt  time.Time `json:"updatedAt"`
}
