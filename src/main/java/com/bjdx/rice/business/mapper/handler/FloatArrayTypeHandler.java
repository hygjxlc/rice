package com.bjdx.rice.business.mapper.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler for float[] array
 * Converts float array to/from BLOB for MySQL storage
 */
@MappedTypes(float[].class)
public class FloatArrayTypeHandler extends BaseTypeHandler<float[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
        try {
            // Convert float array to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(parameter.length);
            for (float v : parameter) {
                dos.writeFloat(v);
            }
            dos.flush();
            byte[] bytes = baos.toByteArray();
            
            // Set as binary stream
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ps.setBinaryStream(i, bais, bytes.length);
        } catch (Exception e) {
            throw new SQLException("Error converting float array to bytes", e);
        }
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return bytesToFloatArray(rs.getBytes(columnName));
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return bytesToFloatArray(rs.getBytes(columnIndex));
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return bytesToFloatArray(cs.getBytes(columnIndex));
    }

    private float[] bytesToFloatArray(byte[] bytes) throws SQLException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bais);
            int length = dis.readInt();
            float[] result = new float[length];
            for (int i = 0; i < length; i++) {
                result[i] = dis.readFloat();
            }
            return result;
        } catch (Exception e) {
            throw new SQLException("Error converting bytes to float array", e);
        }
    }
}
