package ru.fizteh.fivt.students.artem_gritsay.Storable;

import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.storage.strings.TableProviderFactory;


public final class TableManagerFactory implements TableProviderFactory {
    @Override
    public TableProvider create(String dir) {
        if (dir == null) {
            throw new IllegalArgumentException("Directory name is null");
        }
        return new TableManager(dir);
    }
}
