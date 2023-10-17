package rabbit.flt.common.exception;

public class QueueFullException extends AgentException {

    public QueueFullException() {
        super("queue is full!");
    }
}
