package org.example.dtos;

import java.util.List;

public class CambioFechasMultipleRequestDTO {

    private List<CambioDetalle> cambios;

    public List<CambioDetalle> getCambios() { return cambios; }
    public void setCambios(List<CambioDetalle> cambios) { this.cambios = cambios; }

    public static class CambioDetalle {
        private int    detalleId;
        private String fechaCheckIn;
        private String fechaCheckOut;

        public int    getDetalleId()    { return detalleId; }
        public String getFechaCheckIn() { return fechaCheckIn; }
        public String getFechaCheckOut(){ return fechaCheckOut; }

        public void setDetalleId(int detalleId)          { this.detalleId = detalleId; }
        public void setFechaCheckIn(String fechaCheckIn) { this.fechaCheckIn = fechaCheckIn; }
        public void setFechaCheckOut(String fechaCheckOut){ this.fechaCheckOut = fechaCheckOut; }
    }
}