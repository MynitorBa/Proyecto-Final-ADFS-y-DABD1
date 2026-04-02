package org.example.dtos;

public class PuedeCancelarDTO {
    private boolean puedeCancelar;
    private String razon;

    public PuedeCancelarDTO(boolean puedeCancelar, String razon) {
        this.puedeCancelar = puedeCancelar;
        this.razon = razon;
    }

    public boolean isPuedeCancelar() { return puedeCancelar; }
    public String getRazon() { return razon; }
}