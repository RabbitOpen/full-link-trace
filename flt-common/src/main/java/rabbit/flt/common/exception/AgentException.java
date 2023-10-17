package rabbit.flt.common.exception;

public class AgentException extends RuntimeException {

    public AgentException(Throwable cause) {
        super(cause);
    }

    public AgentException(String message) {
        super(message);
    }
}
