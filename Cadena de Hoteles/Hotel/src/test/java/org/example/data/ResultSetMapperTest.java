package org.example.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ResultSetMapper.
 * Valida que la interfaz funcional puede implementarse con lambdas y procesa ResultSets correctamente.
 */
@DisplayName("ResultSetMapper - Interfaz funcional para mapeo de ResultSet")
class ResultSetMapperTest {

    @Test
    @DisplayName("implementacion con lambda mapea String correctamente")
    void implementacion_conLambda_mapeaStringCorrectamente() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("nombre")).thenReturn("Hotel Test");

        ResultSetMapper<String> mapper = resultSet -> resultSet.getString("nombre");
        String resultado = mapper.map(rs);

        assertEquals("Hotel Test", resultado);
    }

    @Test
    @DisplayName("implementacion con lambda mapea entero correctamente")
    void implementacion_conLambda_mapeaEnteroCorrectamente() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("id")).thenReturn(42);

        ResultSetMapper<Integer> mapper = resultSet -> resultSet.getInt("id");
        Integer resultado = mapper.map(rs);

        assertEquals(42, resultado);
    }

    @Test
    @DisplayName("implementacion con lambda mapea double correctamente")
    void implementacion_conLambda_mapeaDoubleCorrectamente() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getDouble("precio")).thenReturn(150.75);

        ResultSetMapper<Double> mapper = resultSet -> resultSet.getDouble("precio");
        Double resultado = mapper.map(rs);

        assertEquals(150.75, resultado, 0.001);
    }

    @Test
    @DisplayName("implementacion con lambda puede construir objeto compuesto")
    void implementacion_conLambda_construyeObjetoCompuesto() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("nombre")).thenReturn("Miku Inn");
        when(rs.getDouble("rating")).thenReturn(4.8);

        ResultSetMapper<Object[]> mapper = resultSet -> new Object[]{
                resultSet.getInt("id"),
                resultSet.getString("nombre"),
                resultSet.getDouble("rating")
        };

        Object[] resultado = mapper.map(rs);

        assertEquals(1,       resultado[0]);
        assertEquals("Miku Inn", resultado[1]);
        assertEquals(4.8,    resultado[2]);
    }

    @Test
    @DisplayName("implementacion propaga SQLException cuando ResultSet falla")
    void implementacion_propagaSQLException_cuandoResultSetFalla() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("nombre")).thenThrow(new SQLException("Columna no encontrada"));

        ResultSetMapper<String> mapper = resultSet -> resultSet.getString("nombre");

        assertThrows(SQLException.class, () -> mapper.map(rs));
    }
}
