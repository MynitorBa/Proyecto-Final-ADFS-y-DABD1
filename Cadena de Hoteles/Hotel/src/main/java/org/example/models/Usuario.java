package org.example.models;

/**
 * Modelo que representa un usuario registrado en el sistema.
 * Contiene los datos personales, credenciales de acceso y ubicacion del usuario.
 */
public class Usuario {
    private int id;
    private String correo;
    private String contrasena;
    private String pasaporte;
    private String username;
    private String nombre;
    private String apellido;
    private int rolId;
    private String telefono;
    private java.time.LocalDate fechaNacimiento;
    private Integer ciudadId;

    /**
     * @return ID unico del usuario.
     */
    public int getId() { return id; }

    /**
     * @param id ID unico del usuario.
     */
    public void setId(int id) { this.id = id; }

    /**
     * @return correo electronico del usuario.
     */
    public String getCorreo() { return correo; }

    /**
     * @param correo correo electronico del usuario.
     */
    public void setCorreo(String correo) { this.correo = correo; }

    /**
     * @return contrasena hasheada del usuario.
     */
    public String getContrasena() { return contrasena; }

    /**
     * @param contrasena contrasena hasheada del usuario.
     */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    /**
     * @return numero de pasaporte del usuario.
     */
    public String getPasaporte() { return pasaporte; }

    /**
     * @param pasaporte numero de pasaporte del usuario.
     */
    public void setPasaporte(String pasaporte) { this.pasaporte = pasaporte; }

    /**
     * @return nombre de usuario unico para iniciar sesion.
     */
    public String getUsername() { return username; }

    /**
     * @param username nombre de usuario unico para iniciar sesion.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * @return nombre de pila del usuario.
     */
    public String getNombre() { return nombre; }

    /**
     * @param nombre nombre de pila del usuario.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * @return apellido del usuario.
     */
    public String getApellido() { return apellido; }

    /**
     * @param apellido apellido del usuario.
     */
    public void setApellido(String apellido) { this.apellido = apellido; }

    /**
     * @return ID del rol asignado al usuario.
     */
    public int getRolId() { return rolId; }

    /**
     * @param rolId ID del rol asignado al usuario.
     */
    public void setRolId(int rolId) { this.rolId = rolId; }

    /**
     * @return numero de telefono del usuario.
     */
    public String getTelefono() { return telefono; }

    /**
     * @param telefono numero de telefono del usuario.
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * @return fecha de nacimiento del usuario.
     */
    public java.time.LocalDate getFechaNacimiento() { return fechaNacimiento; }

    /**
     * @param fechaNacimiento fecha de nacimiento del usuario.
     */
    public void setFechaNacimiento(java.time.LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    /**
     * @return ID de la ciudad de residencia del usuario, o null si no fue registrada.
     */
    public Integer getCiudadId() { return ciudadId; }

    /**
     * @param ciudadId ID de la ciudad de residencia del usuario.
     */
    public void setCiudadId(Integer ciudadId) { this.ciudadId = ciudadId; }
}