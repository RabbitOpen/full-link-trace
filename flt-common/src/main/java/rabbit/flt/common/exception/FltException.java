package rabbit.flt.common.exception;

public class FltException extends RuntimeException {

    public FltException(Throwable cause) {
        super(cause);
    }

    public FltException(String message) {
        super(message);
    }
}
