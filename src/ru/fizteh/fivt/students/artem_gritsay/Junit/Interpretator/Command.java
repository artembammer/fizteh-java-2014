package ru.fizteh.fivt.students.artem_gritsay.Junit.Interpretator;

import java.util.function.BiConsumer;


public class Command {
    private String name;
    private int numberofargs;
    private BiConsumer<Object, String[]> callbackfunc;

    public Command(String name, int number,
                   BiConsumer<Object, String[]> callbackfunc) {
        numberofargs = number;
        this.name = name;
        this.callbackfunc = callbackfunc;
    }

    public final void run(Object func, String[] args) throws Exception {
        if (numberofargs != args.length) {
            throw new StopException("Incorrect number of arguments");
        }
        callbackfunc.accept(func, args);
    }

    public final String name() {
        return name;
    }
}
