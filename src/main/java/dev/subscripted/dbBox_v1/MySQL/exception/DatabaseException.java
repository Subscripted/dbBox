package dev.subscripted.dbBox_v1.MySQL.exception;

/**
 * Eine Laufzeitausnahme für alle datenbankspezifischen Fehler.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }
}
