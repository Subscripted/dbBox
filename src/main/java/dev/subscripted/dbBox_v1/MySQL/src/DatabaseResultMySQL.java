package dev.subscripted.dbBox_v1.MySQL.src;

import dev.subscripted.dbBox_v1.MySQL.exception.DatabaseException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Hüllt ein ResultSet ein und stellt Methoden zum Auslesen bereit.
 */
public record DatabaseResultMySQL(ResultSet result) implements AutoCloseable {

    public boolean next() {
        try {
            return result.next();
        } catch (SQLException exception) {
            throw new DatabaseException("Error while navigating through the ResultSet.", exception);
        }
    }

    public int getColumnCount() {
        try {
            return result.getMetaData().getColumnCount();
        } catch (SQLException exception) {
            throw new DatabaseException("Error while retrieving column count from ResultSet metadata.", exception);
        }
    }

    public String getColumnName(int index) {
        try {
            return result.getMetaData().getColumnName(index);
        } catch (SQLException exception) {
            throw new DatabaseException("Error while retrieving column name from ResultSet metadata.", exception);
        }
    }

    public String getColumnType(int index) {
        try {
            return result.getMetaData().getColumnTypeName(index);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage(), e);
        }
    }

    public String getString(String name) {
        try {
            return result.getString(name);
        } catch (SQLException exception) {
            throw new DatabaseException("Error while retrieving String for column '" + name + "'", exception);
        }
    }

    public int getInt(String name) {
        try {
            return result.getInt(name);
        } catch (SQLException exception) {
            throw new DatabaseException("Error retrieving int value for column '" + name + "'", exception);
        }
    }

    public long getLong(String name) {
        try {
            return result.getLong(name);
        } catch (SQLException exception) {
            throw new DatabaseException("Error retrieving long value for column '" + name + "'", exception);
        }
    }

    public float getFloat(String name) {
        try {
            return result.getFloat(name);
        } catch (SQLException exception) {
            throw new DatabaseException("Error retrieving float value for column '" + name + "'", exception);
        }
    }

    public double getDouble(String name) {
        try {
            return result.getDouble(name);
        } catch (SQLException exception) {
            throw new DatabaseException("Error retrieving double value for column '" + name + "'", exception);
        }
    }

    public boolean getBoolean(String name) {
        try {
            return result.getBoolean(name);
        } catch (SQLException exception) {
            throw new DatabaseException("Error retrieving boolean value for column '" + name + "'", exception);
        }
    }

    @Override
    public void close() {
        try {
            result.close();
        } catch (SQLException exception) {
            throw new DatabaseException("Error occurred while trying to close the ResultSet.", exception);
        }
    }
}
