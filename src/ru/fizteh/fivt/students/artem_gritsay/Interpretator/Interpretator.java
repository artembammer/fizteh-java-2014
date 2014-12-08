package ru.fizteh.fivt.students.artem_gritsay.Interpretator;

import java.util.*;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;


public class Interpretator {

    private InputStream in;
    private PrintStream out;
    private Map<String, Command> commands;
    private Object func;
    private Callable<Boolean> exitFlag;

    public Interpretator(Object func, Command[] commands, InputStream input, PrintStream output) {
        this.commands = new HashMap<>();
        this.in = input;
        this.out = output;
        this.func = func;
        for (Command cmd : commands) {
            this.commands.put(cmd.name(), cmd);
        }
        if (in == null || out == null) {
            throw new IllegalArgumentException("Stream is null");
        }
    }

    private void parseline(String[] command) throws Exception {
        if (command.length > 0 && !command[0].isEmpty()) {
            String nameofcommand = command[0];
            if (nameofcommand.equals("exit")) {
                if (exitFlag == null) {
                    throw new StopException(false);
                }
                if (exitFlag.call()) {
                    throw new StopException(false);
                } else {
                    throw new StopException(true);
                }
            }
            Command cmd = commands.get(nameofcommand);
            if (cmd == null) {
                throw new StopException("No such command declared: " + nameofcommand);
            } else {
                String[] args = Arrays.copyOfRange(command, 1 , command.length);
                try {
                    cmd.run(func, args);
                } catch (RuntimeException e) {
                    throw new StopException(e.getMessage());
                }
            }
        }
    }
    private boolean execute(String line) {
        String[] cmds = line.split(";");
        try {
            for (String current : cmds) {
                parseline(current.trim().split("\\s+"));
            }
            return false;
        } catch (StopException e) {
            out.println(e.getMessage());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private boolean interactive() throws Exception {
        boolean exitStatus = false;
        try (Scanner in = new Scanner(this.in)) {
            while (true) {
                out.print("$ ");
                try {
                    exitStatus = execute(in.nextLine().trim());
                } catch (NoSuchElementException e) {
                    break;
                } catch (StopException e) {
                    if (e.getStatus()) {
                        break;
                    }
                }
            }
        }
        return exitStatus;
    }

    private boolean batchMode(String[] args) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (String current : args) {
            builder.append(current);
            builder.append(" ");
        }
        boolean exitStatus = execute(builder.toString());
        if (exitFlag != null) {
            exitFlag.call();
        }
        return exitStatus;
    }

    public boolean run(String[] args) throws Exception {
        boolean exitStatus = false;
        try {
            if (args.length == 0) {
                exitStatus = interactive();
            } else {
                exitStatus = batchMode(args);
            }
        } catch (StopException e) {
            exitStatus = e.getStatus();
        }
        return exitStatus;
    }

    public Interpretator(Object func, Command[] commands) {
        this(func, commands, System.in, System.out);
    }
    public void setExitFlags(Callable<Boolean> callable) {
        exitFlag = callable;
    }


}
