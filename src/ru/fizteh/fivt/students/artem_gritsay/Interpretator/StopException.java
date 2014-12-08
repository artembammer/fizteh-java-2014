package ru.fizteh.fivt.students.artem_gritsay.Interpretator;


public class StopException extends RuntimeException {
    private final boolean status;
    public StopException(String s) {
        super(s);
        status = false;
    }
    public StopException(boolean status) {
        super();
        this.status = status;
    }
    public boolean getStatus() {
        return status;
    }
}
