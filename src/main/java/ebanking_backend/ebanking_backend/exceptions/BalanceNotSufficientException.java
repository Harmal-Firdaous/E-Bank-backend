package ebanking_backend.ebanking_backend.exceptions;


public class BalanceNotSufficientException extends RuntimeException {
    public BalanceNotSufficientException(String message) {
        super(message);
    }
}
