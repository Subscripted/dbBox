# dbBox API

Welcome to the **dbBox API** – a lightweight, robust, and asynchronous library designed to simplify working with MySQL databases in Java. This framework provides a clean API for managing connection pools, executing queries and updates, handling transactions, and even building and modifying tables on the fly. It comes with extensive error handling, logging, and resource management features to ensure your database interactions are both efficient and reliable.

## Key Features

- **Asynchronous Operations**  
  Perform queries and updates asynchronously using `CompletableFuture` and a dedicated `ExecutorService`. This helps keep your application responsive and scalable.

- **Connection Pool Management**  
  A built-in connection pool efficiently manages up to 10 connections, ensuring optimal performance by reusing database connections and reducing connection overhead.

- **Secure & Reliable Operations**  
  All database operations are wrapped in a secure execution method that retries failed operations and logs detailed warnings for any issues encountered.

- **Custom Exception Handling**  
  The framework leverages a custom `DatabaseException` class to encapsulate all database-related errors, making debugging and error tracing easier.

- **Transaction Support**  
  Easily execute multiple database operations within a single transaction, with built-in commit/rollback functionality to maintain data integrity.

- **Dynamic Table Building**  
  The included `TableBuilder` allows for the dynamic creation of tables with automatic validation and safe query generation, reducing the risk of SQL injection.

- **AutoCloseable Resource Management**  
  Both the query results (via `DatabaseResultMySQL`) and database connections are managed using AutoCloseable patterns to prevent resource leaks.

## Usage Example

The following example demonstrates how to use the framework. It shows how to initialize the database connection, create a table, retrieve data from the table, and shut down the manager properly.

```java
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Example {
    public static void main(String[] args) {
        // Initialize the database connection details.
        DatabaseInfo info = new DatabaseInfo("localhost", 3306, "website", "username", "secret");
        DatabaseManager manager = new DatabaseManager(info);
        
        // Create table "groups" with columns "uuid", "name", and "permissions"
        // Note: The create() method may execute asynchronously. If needed, wait for its completion.
        manager.createTable("groups")
               .addString("uuid")
               .addString("name")
               .addString("permissions")
               .create();
        
        // Retrieve the table instance (using "uuid" as the identifier column).
        Table groups = manager.getTable("groups", "uuid");
        
        // Generate a random UUID.
        UUID uuid = UUID.randomUUID();
        
        // Attempt to retrieve the value of the "name" column for the given UUID.
        // This call blocks until the asynchronous operation completes.
        try {
            String name = groups.get("name", uuid.toString())
                                .get()  // Blocks until the result is available
                                .asString();
            System.out.println("Name: " + name);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        
        // Shutdown the database manager to close all connections and release resources.
        manager.shutdown();
    }
}

```



## preparedSelect


```java
import dev.subscripted.dbBox_v1.MySQL.src.DatasourceManagerMySQL;
import dev.subscripted.dbBox_v1.MySQL.src.DatasourceMySQL;
import dev.subscripted.dbBox_v1.MySQL.table.Table;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        String[] data = {"id", "password", "auto_login"};

        DatasourceMySQL db = new DatasourceMySQL("localhost", 3306, "dashboard", "root", "");
        DatasourceManagerMySQL managerMySQL = new DatasourceManagerMySQL(db);
        Table table = new Table(managerMySQL, "user", "id");

        // Blockiere, bis die Abfrage fertig ist und schließe das Ergebnis danach
        table.pselect(data, "user", "id", "1087700921808597063")
             .thenAccept(resultSet -> {
                 try {
                     while (resultSet.next()) {
                         System.out.println("ID: " + resultSet.getInt("id"));
                         System.out.println("password: " + resultSet.getString("password"));
                         System.out.println("auto_login: " + resultSet.getBoolean("auto_login"));
                     }
                 } finally {
                     resultSet.close();
                 }
             })
             .join();

        managerMySQL.shutdown();
    }
    ```

