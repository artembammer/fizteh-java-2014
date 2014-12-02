package ru.fizteh.fivt.students.artem_gritsay.Interpretator;

/**
 * Created by artem on 01.12.14.
 */
public class StopException extends RuntimeException {
    boolean status;
    public StopException(String s) {
        super(s);
    }
    public StopException(boolean status) {
        super();
        this.status = status;
    }
    public boolean getStatus() {
        return status;
    }
}
