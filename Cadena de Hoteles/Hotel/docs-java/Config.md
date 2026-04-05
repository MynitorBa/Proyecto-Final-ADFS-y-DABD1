# Config

## ServerConfig

> Configuracion del servidor Javalin. Lee el puerto desde la variable de entorno PORT (default 7000) y habilita CORS para el frontend en localhost:5173.

```java
public static Javalin createServer()
```

Crea y arranca el servidor Javalin con CORS configurado. Permite credenciales desde http://localhost:5173.

- **Returns** - instancia de Javalin ya iniciada en el puerto configurado.

---
