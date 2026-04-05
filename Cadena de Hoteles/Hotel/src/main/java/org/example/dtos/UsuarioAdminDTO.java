package org.example.dtos;

/**
 * DTO con los datos completos de un usuario para el panel de administracion.
 * Incluye informacion personal, rol asignado y ubicacion geografica.
 */
public class UsuarioAdminDTO {

    private int    id;
    private String username;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String fechaNacimiento;
    private int    rolId;
    private String rolNombre;
    private String ciudad;
    private String pais;

    /**
     * Retorna el identificador unico del usuario.
     * @return ID del usuario.
     */
    public int getId() { return id; }

    /**
     * Retorna el nombre de usuario.
     * @return username del usuario.
     */
    public String getUsername() { return username; }

    /**
     * Retorna el nombre del usuario.
     * @return nombre del usuario.
     */
    public String getNombre() { return nombre; }

    /**
     * Retorna el apellido del usuario.
     * @return apellido del usuario.
     */
    public String getApellido() { return apellido; }

    /**
     * Retorna el correo electronico del usuario.
     * @return correo del usuario.
     */
    public String getCorreo() { return correo; }

    /**
     * Retorna el numero de telefono del usuario.
     * @return telefono del usuario.
     */
    public String getTelefono() { return telefono; }

    /**
     * Retorna la fecha de nacimiento del usuario.
     * @return fecha de nacimiento del usuario.
     */
    public String getFechaNacimiento() { return fechaNacimiento; }

    /**
     * Retorna el ID del rol asignado al usuario.
     * @return ID del rol.
     */
    public int getRolId() { return rolId; }

    /**
     * Retorna el nombre del rol asignado al usuario.
     * @return nombre del rol.
     */
    public String getRolNombre() { return rolNombre; }

    /**
     * Retorna la ciudad de residencia del usuario.
     * @return nombre de la ciudad.
     */
    public String getCiudad() { return ciudad; }

    /**
     * Retorna el pais de residencia del usuario.
     * @return nombre del pais.
     */
    public String getPais() { return pais; }

    /**
     * Asigna el identificador unico del usuario.
     * @param id ID del usuario.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Asigna el nombre de usuario.
     * @param username username del usuario.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Asigna el nombre del usuario.
     * @param nombre nombre del usuario.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Asigna el apellido del usuario.
     * @param apellido apellido del usuario.
     */
    public void setApellido(String apellido) { this.apellido = apellido; }

    /**
     * Asigna el correo electronico del usuario.
     * @param correo correo del usuario.
     */
    public void setCorreo(String correo) { this.correo = correo; }

    /**
     * Asigna el numero de telefono del usuario.
     * @param telefono telefono del usuario.
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * Asigna la fecha de nacimiento del usuario.
     * @param fechaNacimiento fecha de nacimiento del usuario.
     */
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    /**
     * Asigna el ID del rol asignado al usuario.
     * @param rolId ID del rol.
     */
    public void setRolId(int rolId) { this.rolId = rolId; }

    /**
     * Asigna el nombre del rol asignado al usuario.
     * @param rolNombre nombre del rol.
     */
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }

    /**
     * Asigna la ciudad de residencia del usuario.
     * @param ciudad nombre de la ciudad.
     */
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    /**
     * Asigna el pais de residencia del usuario.
     * @param pais nombre del pais.
     */
    public void setPais(String pais) { this.pais = pais; }
}