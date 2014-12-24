package ru.fizteh.fivt.students.artem_gritsay.Tests;

import static org.junit.Assert.*;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.students.artem_gritsay.DataBaseTable;
import ru.fizteh.fivt.students.artem_gritsay.DbRecord;

public class DataBaseTableTest {
    private final Path temppathtoroot = Paths.get(System.getProperty("java.io.tmpdir"), "DbTestDir");
    private final String tableName = "table1";
    private String correctKey;
    private final String testValue = "val";
    private final int offset = 4;
    private final int numberofdir = 1;
    private final int numberoffile = 1;
    private final String requiredSubdirectoryName = numberofdir + ".dir";
    private final String requiredSubfileName = numberoffile + ".dat";
    private final String testKey = "key";
    private final String wrongSubfileName = "sdfsdf";

    @Before
    public void run() {
        temppathtoroot.toFile().mkdir();
        byte[] b = {numberofdir + numberoffile * DbRecord.PARTITIONS, 'k', 'e', 'y'};
        correctKey = new String(b);
    }

    @Test
    public void createdForNonexistentDirectory() {
        new DataBaseTable(temppathtoroot, tableName);
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionLoadedDirectoryWithWrongNamedSubdirectory() {
        temppathtoroot.resolve(wrongSubfileName).toFile().mkdir();
        new DataBaseTable(temppathtoroot, tableName);
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionLoadedDirectoryWithEmptySubdirectory() {
        temppathtoroot.resolve(requiredSubdirectoryName).toFile().mkdir();
        new DataBaseTable(temppathtoroot, tableName);
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionLoadedDirectoryWithSubfileNotInSubdirectory()
            throws IOException {
        temppathtoroot.resolve(requiredSubdirectoryName).toFile().createNewFile();
        new DataBaseTable(temppathtoroot, tableName);
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionLoadedDirectoryWithWrongNamedFileInSubdirectory()
            throws IOException {
        Path subdirectoryPath = temppathtoroot.resolve(requiredSubdirectoryName);
        subdirectoryPath.toFile().mkdir();
        subdirectoryPath.resolve(wrongSubfileName).toFile().createNewFile();
        new DataBaseTable(temppathtoroot, tableName);
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionLoadedDirectoryWithDirectoryInSubdirectory()
            throws IOException {
        Path subdirectoryPath = temppathtoroot.resolve(requiredSubdirectoryName);
        subdirectoryPath.toFile().mkdir();
        subdirectoryPath.resolve(requiredSubfileName).toFile().mkdir();
        new DataBaseTable(temppathtoroot, tableName);
    }

    @Test
    public void loadedCorrectNonemptyDirectory() throws IOException {
        Path subdirectoryPath = temppathtoroot.resolve(requiredSubdirectoryName);
        subdirectoryPath.toFile().mkdir();
        Path subfilePath = subdirectoryPath.resolve(requiredSubfileName);
        try (DataOutputStream file
                     = new DataOutputStream(new FileOutputStream(subfilePath.toString()))) {
            file.write(correctKey.getBytes(DbRecord.CODE));
            file.write('\0');
            file.writeInt(correctKey.length() + 1 + offset);
            file.write(testValue.getBytes(DbRecord.CODE));
        }
        new DataBaseTable(temppathtoroot, tableName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void putThrowsExceptionCalledForNullKeyAndNonNullValue() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        test.put(null, testValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void putThrowsExceptionCalledForNonNullKeyAndNullValue() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        test.put(testKey, null);
    }

    @Test
    public void commitPuttingNonNullKeyAndValue() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.put(testKey, testValue));
    }

    @Test
    public void commitPuttingTwiceNonNullKeyAndValue() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.put(testKey, testValue));
        assertEquals(testValue, test.put(testKey, testValue));
    }

    @Test
    public void commitOverwritingCommitedKey() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.put(testKey, testValue));
        assertEquals(testValue, test.put(testKey, testValue));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getThrowsExceptionCalledForNullKey() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        test.get(null);
    }

    @Test
    public void getCalledForNonexistentKey() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.get(testKey));
    }

    @Test
    public void getCalledForNonComittedExistentKey() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.put(testKey, testValue));
        assertEquals(testValue, test.get(testKey));
    }

    @Test
    public void getCalledForComittedExistentKey() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.put(testKey, testValue));
        assertEquals(testValue, test.get(testKey));
    }

    @Test
    public void rollbackAfterPuttingNewKey() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(0, test.size());
        assertEquals(null, test.put(testKey, testValue));
        assertEquals(1, test.size());
        test.rollback();
        assertEquals(0, test.size());
        assertEquals(null, test.get(testKey));
    }

    @Test
    public void rollbackWithoutAnyChanges() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.put(testKey, testValue));
        test.rollback();
        assertEquals(0, test.size());
        test.rollback();
        assertEquals(0, test.size());
    }

    @Test(expected = RuntimeException.class)
    public void testRemoveThrowsExceptionCalledForNullKey() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        test.remove(null);
    }

    @Test
    public void commitRemovingNonexistentKeyFromNonCommitedFile() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.remove(testKey));
    }

    @Test
    public void testCommitRemovingExistentKeyFromNonCommitedFile() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.put(testKey, testValue));
        assertEquals(testValue, test.remove(testKey));
    }

    @Test
    public void commitRemovingExistentKeyFromCommitedFile() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.put(testKey, testValue));
        assertEquals(testValue, test.remove(testKey));
    }

    @Test
    public void commitRemovingNonexistentKeyFromCommitedFile() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.remove(testKey));
    }


    @Test
    public void commitEmptiedAfterLoadingTable() {
        Table test = new DataBaseTable(temppathtoroot, tableName);
        assertEquals(null, test.put(testKey, testValue));
        assertEquals(testValue, test.remove(testKey));
        String subdirectoryName = testKey.getBytes()[0] % 16 + ".dir";
        String fileName = (testKey.getBytes()[0] / 16) % 16 + ".dat";
        Path filePath = Paths.get(temppathtoroot.toString(), subdirectoryName, fileName);
        assertFalse(filePath.toFile().exists());
    }

    @After
    public void tearDown() {
        for (File curFile : temppathtoroot.toFile().listFiles()) {
            if (curFile.isDirectory()) {
                for (File subFile : curFile.listFiles()) {
                    subFile.delete();
                }
            }
            curFile.delete();
        }
        temppathtoroot.toFile().delete();
    }
}


