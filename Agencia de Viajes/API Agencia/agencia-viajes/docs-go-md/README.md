# Movent API - Documentacion Tecnica Backend

Backend desarrollado en Go/Gin para la agencia de viajes Movent.
Proyecto final UNIS 2026.

## Descripcion

API REST que integra multiples proveedores de vuelos y hoteles.
Desarrollada con Go, Gin y MySQL.

## Tecnologias

- Go / Gin
- MySQL
- JWT para autenticacion
- Arquitectura por capas: controllers, services, repositories

## Paquetes

- [Controllers](Controllers) - Manejadores HTTP de cada endpoint
- [Services](Services) - Logica de negocio
- [Repositories](Repositories) - Acceso a base de datos
- [DTO](DTO) - Data Transfer Objects
- [Models](Models) - Estructuras de datos
- [Middlewares](Middlewares) - Autenticacion y roles
- [Helpers](Helpers) - Email, PDF, tokens, hash
- [Config](Config) - Configuracion del servidor
- [Database](Database) - Conexion a MySQL
- [Server](Server) - Punto de entrada principal