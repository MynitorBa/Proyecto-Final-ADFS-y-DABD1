package helpers

import (
	"crypto/rand"
	"crypto/sha256"
	"encoding/hex"
)

func GenerarTokenHash() (string, error) {
	bytes := make([]byte, 32)
	if _, err := rand.Read(bytes); err != nil {
		return "", err
	}
	hash := sha256.Sum256(bytes)
	return hex.EncodeToString(hash[:]), nil
}
