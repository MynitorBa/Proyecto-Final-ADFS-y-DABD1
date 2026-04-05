package org.example.data;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interfaz funcional para mapear una fila de un ResultSet a un objeto de tipo T.
 * Se usa como parametro en DatabaseManager.executeQuery para convertir resultados SQL a entidades Java.
 */
public interface ResultSetMapper<T> {

    /**
     * Convierte la fila actual del ResultSet en un objeto de tipo T.
     * @param rs ResultSet posicionado en la fila a mapear.
     * @return objeto T construido a partir de los datos de la fila.
     * @throws SQLException si ocurre un error al leer el ResultSet.
     */
    T map(ResultSet rs) throws SQLException;
}