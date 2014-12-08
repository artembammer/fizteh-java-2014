package ru.fizteh.fivt.students.artem_gritsay;

import java.io.IOException;

/**
 * Created by artem on 08.12.14.
 */

public class DataBaseException extends IOException {

        public DataBaseException(String message, Exception cause) {
            super(message, cause);
        }
}

