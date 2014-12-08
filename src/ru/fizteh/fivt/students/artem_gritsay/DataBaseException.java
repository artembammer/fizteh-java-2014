package ru.fizteh.fivt.students.artem_gritsay;

import java.io.IOException;

public class DataBaseException extends IOException {

        public DataBaseException(String message, Exception cause) {
            super(message, cause);
        }
}

