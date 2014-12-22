package ru.fizteh.fivt.students.artem_gritsay;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import ru.fizteh.fivt.students.artem_gritsay.Interpretator.Command;
import ru.fizteh.fivt.students.artem_gritsay.Interpretator.Interpretator;
import ru.fizteh.fivt.students.artem_gritsay.Interpretator.StopException;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

public final class Main {
    private static void execute(final DataBaseState currentstate, final String[] args) {
        Interpretator interpretator = new Interpretator(currentstate, new Command[] {
                new Command("put", 2 , new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object o, String[] strings) {
                        Table table = ((DataBaseState) currentstate).getTable();
                        if (table != null) {
                            String oldValue = table.put(args[0], args[1]);
                            if (oldValue != null) {
                                System.out.println("overwrite");
                                System.out.println(oldValue);
                            } else {
                                System.out.println("new");
                            }
                        } else {
                            throw new StopException("There is no such table");
                        }
                    }
                }),
                new Command("get", 1, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object state, String[] args) {
                        Table table = ((DataBaseState) state).getTable();
                        if (table != null) {
                            String value = table.get(args[0]);
                            if (value != null) {
                                System.out.println("found");
                                System.out.println(value);
                            } else {
                                System.out.println("not found");
                            }
                        } else {
                            throw new StopException("There is no such table");
                        }
                    }
                }),
                new Command("remove", 1, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object state, String[] args) {
                        Table table = ((DataBaseState) state).getTable();
                        if (table != null) {
                            String removedValue = table.remove(args[0]);
                            if (removedValue != null) {
                                System.out.println("removed");
                            } else {
                                System.out.println("not found");
                            }
                        } else {
                            throw new StopException("There is no such table");
                        }
                    }
                }),
                new Command("list", 0, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object state, String[] args) {
                        Table table = ((DataBaseState) state).getTable();
                        if (table != null) {
                            System.out.println(String.join(", ", table.list()));
                        } else {
                            throw new StopException("There is no such table");
                        }
                    }
                }),
                new Command("size", 0, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object state, String[] args) {
                        Table link = ((DataBaseState) state).getTable();
                        if (link != null) {
                            System.out.println(link.size());
                        } else {
                            throw new StopException("There is no such table");
                        }
                    }
                }),
                new Command("commit", 0, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object state, String[] args) {
                        Table table = ((DataBaseState) state).getTable();
                        if (table != null) {
                            System.out.println(table.commit());
                        } else {
                            throw new StopException("There is no such table");
                        }
                    }
                }),
                new Command("rollback", 0, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object state, String[] args) {
                        Table link = ((DataBaseState) state).getTable();
                        if (link != null) {
                            System.out.println(link.rollback());
                        } else {
                            throw new StopException("There is no such table");
                        }
                    }
                }),
                new Command("create", 1, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object state, String[] args) {
                        TableProvider provider = ((DataBaseState) state).getProvider();
                        if (provider.createTable(args[0]) != null) {
                            System.out.println("created");
                        } else {
                            throw new StopException(args[0] + " exists");
                        }
                    }
                }),
                new Command("use", 1, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object state, String[] args) {
                        DataBaseState currentstate = ((DataBaseState) state);
                        TableProvider provider = currentstate.getProvider();
                        Table newTable = provider.getTable(args[0]);
                        DataBaseTable currentTable = (DataBaseTable) currentstate.getTable();
                        if (newTable != null) {
                            if (currentTable != null && (currentTable.getNumberofChages() > 0)) {
                                System.out.println(currentTable.getNumberofChages()
                                        + " unsaved changes");
                            } else {
                                currentstate.setTable(newTable);
                                System.out.println("using " + args[0]);
                            }
                        } else {
                            throw new StopException(args[0] + " not exists");
                        }
                    }
                }),
                new Command("drop", 1, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object state, String[] args) {
                        DataBaseState currentState = ((DataBaseState) state);
                        TableProvider provider = currentState.getProvider();
                        Table currenttable = currentState.getTable();
                        if (currenttable != null && currenttable.getName().equals(args[0])) {
                            currentState.setTable(null);
                        }
                        try {
                            provider.removeTable(args[0]);
                            System.out.println("dropped");
                        } catch (IllegalStateException e) {
                            throw new StopException("tablename not exists");
                        }
                    }
                }),
                new Command("show", 1, new BiConsumer<Object, String[]>() {
                    @Override
                    public void accept(Object state, String[] args) {
                        if (args[0].equals("tables")) {
                            DataBaseState currentstate = ((DataBaseState) state);
                            TableManager provider = (TableManager) currentstate.getProvider();
                            List<String> tableNames = provider.getTableNames();
                            System.out.println("table_name row_count");
                            for (String name : tableNames) {
                                Table curTable = provider.getTable(name);
                                System.out.println(curTable.getName() + " " + curTable.size());
                            }
                        } else {
                            throw new StopException("This cmd doesn't exist"
                                    + "show " + args[0]);
                        }
                    }
                })
        });
        interpretator.setExitFlags(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DataBaseTable table = (DataBaseTable) currentstate.getTable();
                if (table != null && (table.getNumberofChages() > 0)) {
                    System.out.println(table.getNumberofChages()
                            + " unsaved changes");
                    return false;
                }
                return true;
            }

        });
        try {
            if (interpretator.run(args)) {
                System.exit(1);
            } else {
                System.exit(0);
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                System.err.println(e.getMessage());
            } else {
                System.err.println("Something goes wrong");
                e.printStackTrace();
            }
            System.exit(1);
        }
    }


    public static void main(String[] args) {
        String dbTablePath = System.getProperty("fizteh.db.dir");
        if (dbTablePath == null) {
            System.err.println("Incorrect directory");
            System.exit(1);
        }
        TableProviderFactory factory = new TableManagerFactory();
        DataBaseState currentstate = null;
        try {
            currentstate = new DataBaseState(factory.create(dbTablePath));
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        execute(currentstate, args);
    }

}

