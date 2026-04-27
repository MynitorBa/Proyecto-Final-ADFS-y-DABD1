package org.example.dtos;

/**
 * DTO optimizado para validación de tokens.
 * Usa Strings para las fechas para facilitar la integración directa con
 * los inputs de fecha de Svelte y evitar conflictos de tipos en el ResultSet.
 */
public class TokenValidacionResponseDTO {

    private String ciudad;
    private String pais;
    private double porcentajeDescuento;
    private String fechaExpiracion;
    private String fechaIda;    // Formato esperado: YYYY-MM-DD
    private String fechaVuelta; // Formato esperado: YYYY-MM-DD

    /**
     * Constructor vacío por defecto
     */
    public TokenValidacionResponseDTO() {
    }

    /**
     * Constructor completo para el Repository
     */
    public TokenValidacionResponseDTO(String ciudad, String pais, double porcentajeDescuento,
                                      String fechaExpiracion, String fechaIda, String fechaVuelta) {
        this.ciudad = ciudad;
        this.pais = pais;
        this.porcentajeDescuento = porcentajeDescuento;
        this.fechaExpiracion = fechaExpiracion;
        this.fechaIda = fechaIda;
        this.fechaVuelta = fechaVuelta;
    }

    // --- GETTERS ---

    public String getCiudad() {
        return ciudad;
    }

    public String getPais() {
        return pais;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public String getFechaExpiracion() {
        return fechaExpiracion;
    }

    public String getFechaIda() {
        return fechaIda;
    }

    public String getFechaVuelta() {
        return fechaVuelta;
    }

    // --- SETTERS ---

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public void setFechaExpiracion(String fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public void setFechaIda(String fechaIda) {
        this.fechaIda = fechaIda;
    }

    public void setFechaVuelta(String fechaVuelta) {
        this.fechaVuelta = fechaVuelta;
    }

    @Override
    public String toString() {
        return "TokenValidacionResponseDTO{" +
                "ciudad='" + ciudad + '\'' +
                ", pais='" + pais + '\'' +
                ", porcentajeDescuento=" + porcentajeDescuento +
                ", fechaIda='" + fechaIda + '\'' +
                ", fechaVuelta='" + fechaVuelta + '\'' +
                '}';
    }
}