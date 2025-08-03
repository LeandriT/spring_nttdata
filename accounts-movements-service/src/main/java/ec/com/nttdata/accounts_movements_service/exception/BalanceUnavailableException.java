package ec.com.nttdata.accounts_movements_service.exception;

public class BalanceUnavailableException extends RuntimeException {

    public BalanceUnavailableException(String message) {
        super(message);
    }

}