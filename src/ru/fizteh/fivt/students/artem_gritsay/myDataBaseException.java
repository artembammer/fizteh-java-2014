package ru.fizteh.fivt.students.artem_gritsay;

import java.io.IOException;


public class myDataBaseException extends IOException {
    public myDataBaseException(String message, Exception cause) {
        super(message, cause);
    }
}
