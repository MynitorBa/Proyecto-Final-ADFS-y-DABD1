package org.example.dtos;

public class UsuarioValidacionRequestDTO {
    private String username;
    private String correo;
    private String pasaporte;
    private String nombre;
    private String apellido;
    private String telefono;
    private String fechaNacimiento; // "YYYY-MM-DD"
    private Integer ciudadId;
    private Integer rolId;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPasaporte() { return pasaporte; }
    public void setPasaporte(String pasaporte) { this.pasaporte = pasaporte; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public Integer getCiudadId() { return ciudadId; }
    public void setCiudadId(Integer ciudadId) { this.ciudadId = ciudadId; }

    public Integer getRolId() { return rolId; }
    public void setRolId(Integer rolId) { this.rolId = rolId; }
}