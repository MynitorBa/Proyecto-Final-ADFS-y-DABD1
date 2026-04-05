package org.example.dtos;

import java.util.List;

/**
 * DTO con los datos necesarios para registrar un nuevo usuario en el sistema.
 * El pais y la ciudad se envian como nombres en texto. Si no existen, el servicio los crea.
 * La fecha de nacimiento debe enviarse en formato YYYY-MM-DD.
 */
public class UsuarioValidacionRequestDTO {

    private String       username;
    private String       correo;
    private String       contrasena;
    private String       pasaporte;
    private String       nombre;
    private String       apellido;
    private String       telefono;
    private String       fechaNacimiento;      // "YYYY-MM-DD"
    private String       pais;                 // nombre del pais como string
    private String       ciudad;               // nombre de la ciudad como string
    private List<String> nacionalidades;       // lista de nombres como strings

    /**
     * Retorna el nombre de usuario elegido para el registro.
     * @return username del usuario.
     */
    public String getUsername() { return username; }

    /**
     * Asigna el nombre de usuario elegido para el registro.
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
     * Retorna la contrasena del usuario.
     * @return contrasena del usuario.
     */
    public String getContrasena() { return contrasena; }

    /**
     * Asigna la contrasena del usuario.
     * @param contrasena contrasena del usuario.
     */
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

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
     * Retorna la fecha de nacimiento del usuario en formato YYYY-MM-DD.
     * @return fecha de nacimiento.
     */
    public String getFechaNacimiento() { return fechaNacimiento; }

    /**
     * Asigna la fecha de nacimiento del usuario en formato YYYY-MM-DD.
     * @param fechaNacimiento fecha de nacimiento.
     */
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    /**
     * Retorna el nombre del pais de residencia del usuario.
     * @return nombre del pais.
     */
    public String getPais() { return pais; }

    /**
     * Asigna el nombre del pais de residencia del usuario.
     * @param pais nombre del pais.
     */
    public void setPais(String pais) { this.pais = pais; }

    /**
     * Retorna el nombre de la ciudad de residencia del usuario.
     * @return nombre de la ciudad.
     */
    public String getCiudad() { return ciudad; }

    /**
     * Asigna el nombre de la ciudad de residencia del usuario.
     * @param ciudad nombre de la ciudad.
     */
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    /**
     * Retorna la lista de nacionalidades del usuario como nombres en texto.
     * @return lista de nacionalidades.
     */
    public List<String> getNacionalidades() { return nacionalidades; }

    /**
     * Asigna la lista de nacionalidades del usuario como nombres en texto.
     * @param nacionalidades lista de nacionalidades.
     */
    public void setNacionalidades(List<String> nacionalidades) { this.nacionalidades = nacionalidades; }
}