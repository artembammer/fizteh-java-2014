package ru.fizteh.fivt.students.artem_gritsay.Tests;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import ru.fizteh.fivt.students.artem_gritsay.TableManagerFactory;

public class TableManagerFactoryTest {
    private final Path testDir = Paths.get(System.getProperty("java.io.tmpdir"), "DbTestDir");

    @Before
    public void run() {
        testDir.toFile().mkdir();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTableManagerFactoryThrowsExceptionCreatedNullTableManager() {
        TableProviderFactory test = new TableManagerFactory();
        test.create(null);
    }

    @Test
    public void CreatedNewTableManager() {
        TableProviderFactory test = new TableManagerFactory();
        TableProvider testProvider = test.create(testDir.toString());
        testProvider.createTable("testTable");
        assertTrue(testDir.resolve("testTable").toFile().exists());
    }

    @After
    public void tear() {
        for (File curFile : testDir.toFile().listFiles()) {
            if (curFile.isDirectory()) {
                for (File subFile : curFile.listFiles()) {
                    subFile.delete();
                }
            }
            curFile.delete();
        }
        testDir.toFile().delete();
    }

}
