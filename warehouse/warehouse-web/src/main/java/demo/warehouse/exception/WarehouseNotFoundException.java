package demo.warehouse.exception;

/**
 * Exception is thrown when a {@link demo.warehouse.domain.Warehouse} could not be found with sufficient inventory
 * to fulfill an {@link demo.order.domain.Order}.
 *
 * @author Kenny Bastani
 */
public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException() {
    }

    public WarehouseNotFoundException(String message) {
        super(message);
    }

    public WarehouseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WarehouseNotFoundException(Throwable cause) {
        super(cause);
    }

    public WarehouseNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean
            writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
