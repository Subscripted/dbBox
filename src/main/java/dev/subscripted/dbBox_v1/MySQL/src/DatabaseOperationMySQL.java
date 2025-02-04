package dev.subscripted.dbBox_v1.MySQL.src;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface DatabaseOperationMySQL {
    void executeOperation(Connection connection) throws SQLException, InterruptedException;

}
