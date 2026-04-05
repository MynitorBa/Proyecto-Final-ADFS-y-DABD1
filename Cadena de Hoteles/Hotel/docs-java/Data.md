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

> Gestor central de acceso a la base de datos Oracle. Proporciona metodos estaticos para ejecutar consultas, actualizaciones e inserciones usando JDBC con PreparedStatement.

```java
public static <T> List<T> executeQuery(
```

Ejecuta una consulta SQL y mapea los resultados a una lista de objetos.

- **Param** `sql` - sentencia SQL con parametros posicionales.
- **Param** `mapper` - funcion que convierte cada fila del ResultSet en un objeto T.
- **Param** `params` - valores que reemplazan los parametros de la sentencia.
- **Returns** - lista de objetos mapeados desde el ResultSet.

---

```java
public static int executeUpdate(
```

Ejecuta una sentencia SQL de modificacion (INSERT, UPDATE, DELETE).

- **Param** `sql` - sentencia SQL con parametros posicionales.
- **Param** `params` - valores que reemplazan los parametros de la sentencia.
- **Returns** - numero de filas afectadas.

---

```java
public static int executeInsertReturnId(
```

Ejecuta un INSERT y retorna el ID generado automaticamente por la base de datos.

- **Param** `sql` - sentencia INSERT con parametros posicionales.
- **Param** `idColumnName` - nombre de la columna que contiene el ID generado.
- **Param** `params` - valores que reemplazan los parametros de la sentencia.
- **Returns** - ID generado tras el INSERT exitoso.

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
