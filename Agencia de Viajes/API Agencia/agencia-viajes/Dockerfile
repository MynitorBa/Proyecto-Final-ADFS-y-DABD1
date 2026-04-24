# ─────────────────────────────────────────────
# Stage 1 – compilar el binario
# ─────────────────────────────────────────────
FROM golang:1.25-alpine AS builder

RUN apk add --no-cache git ca-certificates tzdata

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
    go build -ldflags="-w -s" -o /app/server ./cmd/server

# ─────────────────────────────────────────────
# Stage 2 – imagen final mínima
# ─────────────────────────────────────────────
FROM alpine:3.21

RUN apk add --no-cache ca-certificates tzdata

WORKDIR /app

COPY --from=builder /app/server .

EXPOSE 8080

CMD ["./server"]