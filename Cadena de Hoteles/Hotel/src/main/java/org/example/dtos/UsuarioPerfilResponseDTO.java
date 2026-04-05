package org.example.dtos;

import java.util.List;

/**
 * DTO con los datos completos del perfil de un usuario autenticado.
 * Incluye informacion personal, ubicacion geografica y nacionalidades registradas.
 */
public class UsuarioPerfilResponseDTO {

    private int          id;
    private String       username;
    private String       correo;
    private String       pasaporte;
    private String       nombre;
    private String       apellido;
    private String       telefono;
    private String       fechaNacimiento;
    private int          rolId;
    private String       pais;
    private String       ciudad;
    private List<String> nacionalidades;

    /**
     * Retorna el identificador unico del usuario.
     * @return ID del usuario.
     */
    public int getId() { return id; }

    /**
     * Asigna el identificador unico del usuario.
     * @param id ID del usuario.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Retorna el nombre de usuario.
     * @return username del usuario.
     */
    public String getUsername() { return username; }

    /**
     * Asigna el nombre de usuario.
     * @param username username del usuario.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Retorna el correo electronico del usuario.
     * @return correo del usuario.
     */
    public String getCorreo() { return correo; }

    /**
     * Asigna el correo electronico del usuario.
     * @param correo correo del usuario.
     */
    public void setCorreo(String correo) { this.correo = correo; }

    /**
     * Retorna el numero de pasaporte del usuario.
     * @return numero de pasaporte.
     */
    public String getPasaporte() { return pasaporte; }

    /**
     * Asigna el numero de pasaporte del usuario.
     * @param pasaporte numero de pasaporte.
     */
    public void setPasaporte(String pasaporte) { this.pasaporte = pasaporte; }

    /**
     * Retorna el nombre del usuario.
     * @return nombre del usuario.
     */
    public String getNombre() { return nombre; }

    /**
     * Asigna el nombre del usuario.
     * @param nombre nombre del usuario.
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Retorna el apellido del usuario.
     * @return apellido del usuario.
     */
    public String getApellido() { return apellido; }

    /**
     * Asigna el apellido del usuario.
     * @param apellido apellido del usuario.
     */
    public void setApellido(String apellido) { this.apellido = apellido; }

    /**
     * Retorna el numero de telefono del usuario.
     * @return telefono del usuario.
     */
    public String getTelefono() { return telefono; }

    /**
     * Asigna el numero de telefono del usuario.
     * @param telefono telefono del usuario.
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * Retorna la fecha de nacimiento del usuario.
     * @return fecha de nacimiento.
     */
    public String getFechaNacimiento() { return fechaNacimiento; }

    /**
     * Asigna la fecha de nacimiento del usuario.
     * @param fechaNacimiento fecha de nacimiento.
     */
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    /**
     * Retorna el ID del rol asignado al usuario.
     * @return ID del rol.
     */
    public int getRolId() { return rolId; }

    /**
     * Asigna el ID del rol asignado al usuario.
     * @param rolId ID del rol.
     */
    public void setRolId(int rolId) { this.rolId = rolId; }

    /**
     * Retorna el pais de residencia del usuario.
     * @return nombre del pais.
     */
    public String getPais() { return pais; }

    /**
     * Asigna el pais de residencia del usuario.
     * @param pais nombre del pais.
     */
    public void setPais(String pais) { this.pais = pais; }

    /**
     * Retorna la ciudad de residencia del usuario.
     * @return nombre de la ciudad.
     */
    public String getCiudad() { return ciudad; }

    /**
     * Asigna la ciudad de residencia del usuario.
     * @param ciudad nombre de la ciudad.
     */
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    /**
     * Retorna la lista de nacionalidades registradas del usuario.
     * @return lista de nacionalidades.
     */
    public List<String> getNacionalidades() { return nacionalidades; }

    /**
     * Asigna la lista de nacionalidades registradas del usuario.
     * @param nacionalidades lista de nacionalidades.
     */
    public void setNacionalidades(List<String> nacionalidades) { this.nacionalidades = nacionalidades; }
}