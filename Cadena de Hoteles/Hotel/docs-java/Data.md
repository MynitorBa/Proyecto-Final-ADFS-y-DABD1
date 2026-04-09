# Data

## DataAccessException

> Excepcion personalizada para errores de acceso a datos. Envuelve cualquier excepcion de base de datos en una RuntimeException.

```java
public DataAccessException(String message, Throwable cause)
```

Crea una nueva excepcion de acceso a datos.

- **Param** `message` - descripcion del error ocurrido.
- **Param** `cause` - excepcion original que causo el fallo.

---

## DatabaseManager

```java
public static <T> List<T> executeQuery(
```

---

```java
public static int executeUpdate(
```

---

```java
public static int executeInsertReturnId(
```

---

## DatabaseTest

> Clase de prueba para verificar la conexion a la base de datos Oracle. Solo debe usarse en desarrollo para validar credenciales y URL JDBC.

```java
public static void testConnection()
```

Intenta abrir una conexion JDBC con Oracle usando credenciales fijas. Imprime un mensaje de exito o error segun el resultado.

---

## ResultSetMapper

> Interfaz funcional para mapear una fila de un ResultSet a un objeto de tipo T. Se usa como parametro en DatabaseManager.executeQuery para convertir resultados SQL a entidades Java.

```java
public interface ResultSetMapper<T>
```

Interfaz funcional para mapear una fila de un ResultSet a un objeto de tipo T. Se usa como parametro en DatabaseManager.executeQuery para convertir resultados SQL a entidades Java.

---
