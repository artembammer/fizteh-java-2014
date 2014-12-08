package ru.fizteh.fivt.students.artem_gritsay;

import java.io.IOException;

public class MyDataBaseException extends IOException {
    public MyDataBaseException(String message, Exception cause) {
        super(message, cause);
    }
}
