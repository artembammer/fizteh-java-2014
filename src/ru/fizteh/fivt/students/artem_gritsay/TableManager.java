package ru.fizteh.fivt.students.artem_gritsay;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;



public final class TableManager implements TableProvider {
    private static final String TablenameRegex = ".*\\.|\\..*|.*(/|\\\\).*";
    private static final String NullNameTable = "Table name is null";
    private static final String IncorrectTableName = "Incorrect table name";
    private Path pathtotables;
    private List<String> tables;

    public TableManager(String dir) throws IllegalArgumentException {
        pathtotables = Paths.get(dir);
        if (!pathtotables.toFile().exists()) {
            pathtotables.toFile().mkdir();
        }
        if (!pathtotables.toFile().isDirectory()) {
            throw new IllegalArgumentException("not a directory");
        }
        tables = new LinkedList<>();
        String[] tableslist = pathtotables.toFile().list();
        for (String tableName : tableslist) {
            Path pathtotable = pathtotables.resolve(tableName);
            if (pathtotable.toFile().isDirectory()) {
                new DataBaseTable(pathtotable, tableName);
                tables.add(tableName);
            } else {
                throw new IllegalArgumentException("root directory contains non-directory files");
            }
        }
    }


    @Override
    public Table getTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException(NullNameTable);
        }
        try {
            pathtotables.resolve(name);
            if(name.matches(IncorrectTableName)) {
                throw new IllegalArgumentException("IncorrectTableName");
            }
            if(tables.contains(name)) {
                return new DataBaseTable(pathtotables.resolve(name),name);
            } else {
                return null;
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(IncorrectTableName + e.getMessage(),e);
        }
    }

    @Override
    public Table createTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException(NullNameTable);
        }
        if (name.matches(IncorrectTableName)) {
            throw new IllegalArgumentException(IncorrectTableName);
        }

        if (tables.contains(name)) {
                return null;
            }
            Path newpathtotable = pathtotables.resolve(name);
            newpathtotable.toFile().mkdir();
            Table table = new DataBaseTable(newpathtotable,name);
            tables.add(name);
            return table;
    }

    @Override
    public void removeTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException(NullNameTable);
        }
        if (name.matches(IncorrectTableName)) {
            throw new IllegalArgumentException(IncorrectTableName);
        }
        Path pathtotable = pathtotables.resolve(name);
        if (!tables.remove(name)) {
            throw new IllegalStateException("no such table");
        } else {
            try {
                recoursiveDelete(pathtotable.toFile());
            } catch (IOException e) {
                throw new RuntimeException("Table can't be removed: " + e.getMessage(),e);
            }
        }
    }
    private void recoursiveDelete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File currentFile : file.listFiles()) {
                recoursiveDelete(currentFile);
            }
        }
        if (!file.delete()) {
            throw new IOException("Unable to delete: " + file);
        }
    }

    public List<String> getTableNames() {
        return tables;
    }
}
