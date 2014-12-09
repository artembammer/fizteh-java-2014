package ru.fizteh.fivt.students.artem_gritsay.Tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.students.artem_gritsay.TableManager;

public class TableManagerTest {
    private final Path temptestDir = Paths.get(System.getProperty("java.io.tmpdir"), "DataBaseTestDir");
    private final String numberofdir = "test";
    private final Path pathtodir = temptestDir.resolve(numberofdir);
    private final String temptestTableName = "testTable";

    @Before
    public void run() {
        temptestDir.toFile().mkdir();
    }

    @Test
    public void tableManagerCreatedForNonexistentDirectory() {
        new TableManager(pathtodir.toString());
    }

    @Test
    public void tableManagerCreatedForExistentDirectory() {
        pathtodir.toFile().mkdir();
        new TableManager(pathtodir.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void tableManagerThrowsExceptionCreatedNotForDirectory() throws IOException {
        pathtodir.toFile().createNewFile();
        new TableManager(pathtodir.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void tableManagerThrowsExceptionCreatedForInvalidPath() {
        new TableManager("\0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tableManagerThrowsExceptionCreatedForDirectoryWithNondirectoryFile()
            throws IOException {
        pathtodir.toFile().mkdir();
        pathtodir.resolve("fileName").toFile().createNewFile();
        new TableManager(pathtodir.toString());
    }

    @Test
    public void tableManagerCreatedForDirectoryContainedDirectory()
            throws IOException {
        pathtodir.toFile().mkdir();
        pathtodir.resolve(temptestTableName).toFile().mkdir();
        TableProvider test = new TableManager(pathtodir.toString());
        assertNotEquals(null, test.getTable(temptestTableName));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTableThrowsExceptionCalledForNullTableName() {
        TableProvider test = new TableManager(pathtodir.toString());
        test.createTable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTableThrowsExceptionCalledForWrongTableName() {
        TableProvider test = new TableManager(pathtodir.toString());
        //Wrong table name contains '.', '/' or '\'.
        test.createTable("..");
    }

    @Test
    public void createTableCalledForNewTable() {
        TableProvider test = new TableManager(pathtodir.toString());
        assertNotEquals(null, test.createTable(temptestTableName));
        assertTrue(pathtodir.resolve(temptestTableName).toFile().exists());
    }

    @Test
    public void createTableCalledForExistentOnDiskTable() {
        pathtodir.resolve(temptestTableName).toFile().mkdirs();
        TableProvider test = new TableManager(pathtodir.toString());
        assertEquals(null, test.createTable(temptestTableName));
        assertTrue(pathtodir.resolve(temptestTableName).toFile().exists());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTableThrowsExceptionCalledForNullTableName() throws Exception {
        TableProvider test = new TableManager(pathtodir.toString());
        test.getTable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTableThrowsExceptionCalledForWrongTableName() {
        TableProvider test = new TableManager(pathtodir.toString());
        //Wrong table name contains '.', '/' or '\'.
        test.getTable("ab/cd");
    }

    @Test
    public void getTableCalledForNonexistentTable() {
        TableProvider test = new TableManager(pathtodir.toString());
        assertEquals(null, test.getTable(temptestTableName));
    }

    @Test
    public void getTableCalledForExistentTable() {
        TableProvider test = new TableManager(pathtodir.toString());
        assertNotEquals(null, test.createTable(temptestTableName));
        assertNotEquals(null, test.getTable(temptestTableName));
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeTableThrowsExceptionCalledForNullTableName() throws Exception {
        TableProvider test = new TableManager(pathtodir.toString());
        test.removeTable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeTableThrowsExceptionCalledForWrongTableName() {
        TableProvider test = new TableManager(pathtodir.toString());
        //Wrong table name contains '.', '/' or '\'.
        test.removeTable("ab\\cd");
    }

    @Test(expected = IllegalStateException.class)
    public void removeTableThrowsExceptionCalledForNonexistentTable() {
        TableProvider test = new TableManager(pathtodir.toString());
        test.removeTable(temptestTableName);
    }

    @Test
    public void removeTableCalledForExistentFullTable() {
        TableProvider test = new TableManager(pathtodir.toString());
        assertNotEquals(null, test.createTable(temptestTableName));
        Table testTable = test.getTable(temptestTableName);
        assertNotEquals(null, testTable);
        assertEquals(null, testTable.put("key", "value"));
        assertEquals(null, testTable.put("key2", "value"));
        testTable.commit();
        test.removeTable(temptestTableName);
    }

    @After
    public void tear() {
        for (File curFile : temptestDir.toFile().listFiles()) {
            if (curFile.isDirectory()) {
                for (File subFile : curFile.listFiles()) {
                    subFile.delete();
                }
            }
            curFile.delete();
        }
        temptestDir.toFile().delete();
    }
}

