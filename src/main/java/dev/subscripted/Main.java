package dev.subscripted;

import dev.subscripted.dbBox_v1.MySQL.src.DatasourceManagerMySQL;
import dev.subscripted.dbBox_v1.MySQL.src.DatasourceMySQL;
import dev.subscripted.dbBox_v1.MySQL.table.Table;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        String[] data = {"*"};

        DatasourceMySQL db = new DatasourceMySQL("localhost", 3306, "dashboard", "root", "");
        DatasourceManagerMySQL managerMySQL = new DatasourceManagerMySQL(db);
        Table table = new Table(managerMySQL, "user", "id");

        table.pselect(data, "user","auto_login", "0")
                .thenAccept(resultSet -> {
                    try (resultSet) {
                        while (resultSet.next()) {
                            System.out.println("ID: " + resultSet.getString("id"));
                            System.out.println("password: " + resultSet.getString("password"));
                            System.out.println("auto_login: " + resultSet.getBoolean("auto_login"));
                        }
                    }
                })
                .join();

        managerMySQL.shutdown();


    }

}
