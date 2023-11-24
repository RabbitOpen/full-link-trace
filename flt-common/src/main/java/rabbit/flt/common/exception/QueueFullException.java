package rabbit.flt.common.exception;

public class QueueFullException extends FltException {

    public QueueFullException() {
        super("queue is full!");
    }
}
