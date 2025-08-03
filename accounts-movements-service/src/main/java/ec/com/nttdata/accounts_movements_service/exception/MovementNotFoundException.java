package ec.com.nttdata.accounts_movements_service.exception;


public class MovementNotFoundException extends RuntimeException {

    public MovementNotFoundException(String message) {
        super(message);
    }

}