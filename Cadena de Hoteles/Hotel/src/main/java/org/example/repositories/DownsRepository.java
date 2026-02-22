package org.example.repositories;

import org.example.data.DatabaseManager;

import java.util.List;

public class DownsRepository {

    // -------------- Verificar si el usuario ya tiene un down en este comentario -------------

    public Integer obtenerValorDown(int usuarioId, int comentarioId) {
        String sql = "SELECT Valor FROM Downs WHERE Usuario_ID = ? AND Comentario_ID = ?";
        List<Integer> result = DatabaseManager.executeQuery(
                sql, rs -> rs.getInt("Valor"), usuarioId, comentarioId
        );
        return result.isEmpty() ? null : result.get(0);
    }

    // ---------------------------------------Insertar down ----------------------------

    public void insertarDown(int usuarioId, int comentarioId, int valor) {
        String sql = "INSERT INTO Downs (Usuario_ID, Comentario_ID, Valor, Fecha) " +
                "VALUES (?, ?, ?, SYSDATE)";
        DatabaseManager.executeUpdate(sql, usuarioId, comentarioId, valor);
    }

    //----------------------------- Eliminar down --------------------------------------

    public void eliminarDown(int usuarioId, int comentarioId) {
        String sql = "DELETE FROM Downs WHERE Usuario_ID = ? AND Comentario_ID = ?";
        DatabaseManager.executeUpdate(sql, usuarioId, comentarioId);
    }

    //-------------------------- Sumar al contador de downs del comentario -------------------------------

    public void actualizarContadorDown(int comentarioId, int delta) {
        String sql = "UPDATE Comentario SET Downs = Downs + ? WHERE ID = ?";
        DatabaseManager.executeUpdate(sql, delta, comentarioId);
    }
}