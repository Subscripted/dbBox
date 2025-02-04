# MySQL Data Access Framework

Welcome to the **MySQL Data Access Framework** – a lightweight, robust, and asynchronous library designed to simplify working with MySQL databases in Java. This framework provides a clean API for managing connection pools, executing queries and updates, handling transactions, and even building and modifying tables on the fly. It comes with extensive error handling, logging, and resource management features to ensure your database interactions are both efficient and reliable.

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

## Getting Started

1. **Setup**  
   Add the framework to your project via your preferred dependency management tool (Maven, Gradle, etc.) or simply include the source code in your project.

2. **Configuration**  
   Create a `DatasourceMySQL` instance with your database connection details (host, port, database name, user, and password).

3. **Usage**  
   Use the provided API methods to execute queries and updates asynchronously:
   
   ```java
   DatasourceMySQL datasource = new DatasourceMySQL("localhost", 3306, "my_database", "user", "password");
   DatasourceManagerMySQL dbManager = new DatasourceManagerMySQL(datasource);
   
   // Asynchronous Query
   dbManager.executeQuery("SELECT * FROM my_table WHERE id = ?", 1)
            .thenAccept(result -> {
                while (result.next()) {
                    System.out.println("Value: " + result.getString("column_name"));
                }
                result.close();
            });
   
   // Asynchronous Update
   dbManager.executeUpdate("UPDATE my_table SET column_name = ? WHERE id = ?", "newValue", 1)
            .thenRun(() -> System.out.println("Update successful!"));
