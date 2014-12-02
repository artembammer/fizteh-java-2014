package ru.fizteh.fivt.students.artem_gritsay.DataBase;

import java.io.IOException;
/**
 * Created by artem on 27.11.14.
 */
public class myDataBaseException extends IOException {
    public myDataBaseException(String message, Exception cause) {
        super(message, cause);
    }
}
