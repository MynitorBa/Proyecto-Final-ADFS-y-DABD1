package org.example.dtos;

// CambioFechasRequestDTO.java
public class CambioFechasRequestDTO {

    private String fechaCheckIn;
    private String fechaCheckOut;

    // Constructor vacío (necesario para la serialización de JSON)
    public CambioFechasRequestDTO() {
    }

    // Constructor con parámetros
    public CambioFechasRequestDTO(String fechaCheckIn, String fechaCheckOut) {
        this.fechaCheckIn = fechaCheckIn;
        this.fechaCheckOut = fechaCheckOut;
    }

    // Getters y Setters
    public String getFechaCheckIn() {
        return fechaCheckIn;
    }

    public void setFechaCheckIn(String fechaCheckIn) {
        this.fechaCheckIn = fechaCheckIn;
    }

    public String getFechaCheckOut() {
        return fechaCheckOut;
    }

    public void setFechaCheckOut(String fechaCheckOut) {
        this.fechaCheckOut = fechaCheckOut;
    }
}