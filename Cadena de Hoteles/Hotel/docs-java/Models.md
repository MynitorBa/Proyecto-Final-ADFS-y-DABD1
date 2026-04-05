# Models

## Usuario

> Modelo que representa un usuario registrado en el sistema. Contiene los datos personales, credenciales de acceso y ubicacion del usuario.

```java
public int getId()
```

- **Returns** - ID unico del usuario.

---

```java
public void setId(int id)
```

- **Param** `id` - ID unico del usuario.

---

```java
public String getCorreo()
```

- **Returns** - correo electronico del usuario.

---

```java
public void setCorreo(String correo)
```

- **Param** `correo` - correo electronico del usuario.

---

```java
public String getContrasena()
```

- **Returns** - contrasena hasheada del usuario.

---

```java
public void setContrasena(String contrasena)
```

- **Param** `contrasena` - contrasena hasheada del usuario.

---

```java
public String getPasaporte()
```

- **Returns** - numero de pasaporte del usuario.

---

```java
public void setPasaporte(String pasaporte)
```

- **Param** `pasaporte` - numero de pasaporte del usuario.

---

```java
public String getUsername()
```

- **Returns** - nombre de usuario unico para iniciar sesion.

---

```java
public void setUsername(String username)
```

- **Param** `username` - nombre de usuario unico para iniciar sesion.

---

```java
public String getNombre()
```

- **Returns** - nombre de pila del usuario.

---

```java
public void setNombre(String nombre)
```

- **Param** `nombre` - nombre de pila del usuario.

---

```java
public String getApellido()
```

- **Returns** - apellido del usuario.

---

```java
public void setApellido(String apellido)
```

- **Param** `apellido` - apellido del usuario.

---

```java
public int getRolId()
```

- **Returns** - ID del rol asignado al usuario.

---

```java
public void setRolId(int rolId)
```

- **Param** `rolId` - ID del rol asignado al usuario.

---

```java
public String getTelefono()
```

- **Returns** - numero de telefono del usuario.

---

```java
public void setTelefono(String telefono)
```

- **Param** `telefono` - numero de telefono del usuario.

---

```java
public void setFechaNacimiento(java.time.LocalDate fechaNacimiento)
```

- **Param** `fechaNacimiento` - fecha de nacimiento del usuario.

---

```java
public Integer getCiudadId()
```

- **Returns** - ID de la ciudad de residencia del usuario, o null si no fue registrada.

---

```java
public void setCiudadId(Integer ciudadId)
```

- **Param** `ciudadId` - ID de la ciudad de residencia del usuario.

---
