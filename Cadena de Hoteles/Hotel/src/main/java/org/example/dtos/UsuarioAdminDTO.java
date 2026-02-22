package org.example.dtos;

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

    // ── Getters ──────────────────────────────────────────────────────────────

    public int    getId()             { return id; }
    public String getUsername()       { return username; }
    public String getNombre()         { return nombre; }
    public String getApellido()       { return apellido; }
    public String getCorreo()         { return correo; }
    public String getTelefono()       { return telefono; }
    public String getFechaNacimiento(){ return fechaNacimiento; }
    public int    getRolId()          { return rolId; }
    public String getRolNombre()      { return rolNombre; }
    public String getCiudad()         { return ciudad; }
    public String getPais()           { return pais; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                           { this.id = id; }
    public void setUsername(String username)            { this.username = username; }
    public void setNombre(String nombre)                { this.nombre = nombre; }
    public void setApellido(String apellido)            { this.apellido = apellido; }
    public void setCorreo(String correo)                { this.correo = correo; }
    public void setTelefono(String telefono)            { this.telefono = telefono; }
    public void setFechaNacimiento(String f)            { this.fechaNacimiento = f; }
    public void setRolId(int rolId)                     { this.rolId = rolId; }
    public void setRolNombre(String rolNombre)          { this.rolNombre = rolNombre; }
    public void setCiudad(String ciudad)                { this.ciudad = ciudad; }
    public void setPais(String pais)                    { this.pais = pais; }
}