package ru.fizteh.fivt.students.artem_gritsay;


import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;

public class DataBaseState {
    private TableProvider provider;
    private Table currentTable;

    public DataBaseState(TableProvider provider) {
        this.provider = provider;
        currentTable = null;
    }

    public TableProvider getProvider() {
        return provider;
    }

    public void setTable(Table table) {
        currentTable = table;
    }

    public Table getTable() {
        return currentTable;
    }
}
