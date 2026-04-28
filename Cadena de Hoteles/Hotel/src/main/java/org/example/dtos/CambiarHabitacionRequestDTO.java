package org.example.dtos;

public class CambiarHabitacionRequestDTO {

    private int nuevaHabitacionId;

    public CambiarHabitacionRequestDTO() {}

    public CambiarHabitacionRequestDTO(int nuevaHabitacionId) {
        this.nuevaHabitacionId = nuevaHabitacionId;
    }

    public int getNuevaHabitacionId() { return nuevaHabitacionId; }

    public void setNuevaHabitacionId(int nuevaHabitacionId) { this.nuevaHabitacionId = nuevaHabitacionId; }
}
