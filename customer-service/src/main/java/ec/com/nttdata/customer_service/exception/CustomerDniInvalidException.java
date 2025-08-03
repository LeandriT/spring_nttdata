package ec.com.nttdata.customer_service.exception;

public class CustomerDniInvalidException extends RuntimeException {

    public CustomerDniInvalidException(String message) {
        super(message);
    }

}