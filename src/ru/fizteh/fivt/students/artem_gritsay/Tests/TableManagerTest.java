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
    public void TableManagerCreatedForNonexistentDirectory() {
        new TableManager(pathtodir.toString());
    }

    @Test
    public void TableManagerCreatedForExistentDirectory() {
        pathtodir.toFile().mkdir();
        new TableManager(pathtodir.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void TableManagerThrowsExceptionCreatedNotForDirectory() throws IOException {
        pathtodir.toFile().createNewFile();
        new TableManager(pathtodir.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void TableManagerThrowsExceptionCreatedForInvalidPath() {
        new TableManager("\0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void TableManagerThrowsExceptionCreatedForDirectoryWithNondirectoryFile()
            throws IOException {
        pathtodir.toFile().mkdir();
        pathtodir.resolve("fileName").toFile().createNewFile();
        new TableManager(pathtodir.toString());
    }

    @Test
    public void TableManagerCreatedForDirectoryContainedDirectory()
            throws IOException {
        pathtodir.toFile().mkdir();
        pathtodir.resolve(temptestTableName).toFile().mkdir();
        TableProvider test = new TableManager(pathtodir.toString());
        assertNotEquals(null, test.getTable(temptestTableName));
    }

    @Test(expected = IllegalArgumentException.class)
    public void CreateTableThrowsExceptionCalledForNullTableName() {
        TableProvider test = new TableManager(pathtodir.toString());
        test.createTable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void CreateTableThrowsExceptionCalledForWrongTableName() {
        TableProvider test = new TableManager(pathtodir.toString());
        //Wrong table name contains '.', '/' or '\'.
        test.createTable("..");
    }

    @Test
    public void CreateTableCalledForNewTable() {
        TableProvider test = new TableManager(pathtodir.toString());
        assertNotEquals(null, test.createTable(temptestTableName));
        assertTrue(pathtodir.resolve(temptestTableName).toFile().exists());
    }

    @Test
    public void CreateTableCalledForExistentOnDiskTable() {
        pathtodir.resolve(temptestTableName).toFile().mkdirs();
        TableProvider test = new TableManager(pathtodir.toString());
        assertEquals(null, test.createTable(temptestTableName));
        assertTrue(pathtodir.resolve(temptestTableName).toFile().exists());
    }

    @Test(expected = IllegalArgumentException.class)
    public void GetTableThrowsExceptionCalledForNullTableName() throws Exception {
        TableProvider test = new TableManager(pathtodir.toString());
        test.getTable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void GetTableThrowsExceptionCalledForWrongTableName() {
        TableProvider test = new TableManager(pathtodir.toString());
        //Wrong table name contains '.', '/' or '\'.
        test.getTable("ab/cd");
    }

    @Test
    public void GetTableCalledForNonexistentTable() {
        TableProvider test = new TableManager(pathtodir.toString());
        assertEquals(null, test.getTable(temptestTableName));
    }

    @Test
    public void GetTableCalledForExistentTable() {
        TableProvider test = new TableManager(pathtodir.toString());
        assertNotEquals(null, test.createTable(temptestTableName));
        assertNotEquals(null, test.getTable(temptestTableName));
    }

    @Test(expected = IllegalArgumentException.class)
    public void RemoveTableThrowsExceptionCalledForNullTableName() throws Exception {
        TableProvider test = new TableManager(pathtodir.toString());
        test.removeTable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void RemoveTableThrowsExceptionCalledForWrongTableName() {
        TableProvider test = new TableManager(pathtodir.toString());
        //Wrong table name contains '.', '/' or '\'.
        test.removeTable("ab\\cd");
    }

    @Test(expected = IllegalStateException.class)
    public void RemoveTableThrowsExceptionCalledForNonexistentTable() {
        TableProvider test = new TableManager(pathtodir.toString());
        test.removeTable(temptestTableName);
    }

    @Test
    public void RemoveTableCalledForExistentFullTable() {
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

