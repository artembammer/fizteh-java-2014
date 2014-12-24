package ru.fizteh.fivt.students.artem_gritsay.Interpretator;

import java.util.function.BiConsumer;


public class Command {
    private String name;
    private int numberOfArguments;
    private BiConsumer<Object, String[]> callBackFunc;

    public Command(String name, int number,
                   BiConsumer<Object, String[]> callBackFunc) {
        numberOfArguments = number;
        this.name = name;
        this.callBackFunc = callBackFunc;
    }

    public final void run(Object func, String[] args) throws Exception {
        if (numberOfArguments != args.length) {
            throw new StopException("Incorrect number of arguments");
        }
        callBackFunc.accept(func, args);
    }

    public final String name() {
        return name;
    }
}
