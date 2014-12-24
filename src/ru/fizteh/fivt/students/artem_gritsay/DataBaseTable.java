package ru.fizteh.fivt.students.artem_gritsay;


import ru.fizteh.fivt.storage.strings.Table;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.*;

public class DataBaseTable implements Table {

    private String nameOfTable;
    private Map<Integer, DbRecord> records;
    private Path pathToTable;
    private Map<String, String> currentDataRecords;
    private static final String DIR_NAME = "([0-9]|1[0-5])\\.dir";
    private static final String FILE_NAME = "([0-9]|1[0-5])\\.dat";

    public DataBaseTable(Path pathToTable, String name) {
        records = new HashMap<>();
        currentDataRecords = new HashMap<>();
        this.pathToTable = pathToTable;
        nameOfTable = name;
        try {
            readTable();
        } catch (DataBaseException e) {
            throw new RuntimeException("Error reading table '" + getName()
                    + "': " + e.getMessage(), e);
        }

    }

    private void readTable() throws DataBaseException {
        String[] listDir = pathToTable.toFile().list();
        for (String dir : listDir) {
            Path currentDir = pathToTable.resolve(dir);
            if (!currentDir.toFile().isDirectory() || !dir.matches(DIR_NAME)) {
                throw new DataBaseException("File '" + dir + "' is not directory", null);
            }
            String[] filelist = currentDir.toFile().list();
            if (filelist.length == 0) {
                throw new DataBaseException("Directory '" + dir + "' is empty", null);
            }
            for (String file : filelist) {
                Path pathtofile = currentDir.resolve(file);
                if (!file.matches(FILE_NAME) || !pathtofile.toFile().isFile()) {
                    throw new DataBaseException("Name of file '" + file + "' is not supported", null);
                }
                int numberOfDir = Integer.parseInt(dir.substring(0, dir.length() - DbRecord.DIR.length()));
                int numberOfFile = Integer.parseInt(file.substring(0, file.length() - DbRecord.DIR.length()));
                DbRecord record = new DbRecord(pathToTable, numberOfDir, numberOfFile);
                records.put(numberOfDir * 100 + numberOfFile, record);
            }
        }
    }

    private void writeTable() throws DataBaseException {
        Iterator<Map.Entry<Integer, DbRecord>> it = records.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, DbRecord> record = it.next();
            record.getValue().commit();
            if (record.getValue().getNumberOfRecords() == 0) {
                it.remove();
            }
        }
    }

    public int getNumberofChages() {
        return currentDataRecords.size();
    }
    private int getNumberofRecords(String key) {
        int numberOfFile;
        int numberOfDir;
        try {
            numberOfDir = Math.abs(key.getBytes(DbRecord.CODE)[0] % DbRecord.PARTITIONS);
            numberOfFile = Math.abs((key.getBytes(DbRecord.CODE)[0] / DbRecord.PARTITIONS) % DbRecord.PARTITIONS);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unable to encode key to " + DbRecord.CODE, e);
        }
        return numberOfDir * DbRecord.PARTITIONS + numberOfFile;
    }

    @Override
    public List<String> list() {
        List<String> keys = new LinkedList<>();
        for (Map.Entry<Integer, DbRecord> pair : records.entrySet()) {
            keys.addAll(pair.getValue().list());
        }
        for (Map.Entry<String, String> pair : currentDataRecords.entrySet()) {
            if (pair.getValue() == null) {
                keys.remove(pair.getKey());
            } else {
                keys.add(pair.getKey());
            }
        }
        return keys;
    }

    @Override
    public int size() {
        int numberOfRecords = 0;
        for (Map.Entry<Integer, DbRecord> record : records.entrySet()) {
            numberOfRecords += record.getValue().getNumberOfRecords();
        }
        for (Map.Entry<String, String> record : currentDataRecords.entrySet()) {
            if (record.getValue() == null) {
                numberOfRecords--;
            } else {
                numberOfRecords++;
            }
        }
        return numberOfRecords;
    }
    public String changeValue(String key) {
        String oldvalue;
        if (!currentDataRecords.containsKey(key)) {
            DbRecord record = records.get(getNumberofRecords(key));
            if (record == null) {
                oldvalue = null;
            } else {
                try {
                    oldvalue = record.get(key);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                currentDataRecords.put(key, null);
            }
        } else {
            oldvalue = currentDataRecords.remove(key);
        }
        return oldvalue;
    }
    @Override
    public String remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key is null");
        }
        String oldvalue = changeValue(key);
        return oldvalue;
    }

    @Override
    public String put(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value is a null-string");
        }
        String oldValue = changeValue(key);
        currentDataRecords.put(key, value);
        return oldValue;
    }

    @Override
    public String get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key is null");
        }
        String value;
        if (currentDataRecords.containsKey(key)) {
            value = currentDataRecords.get(key);
        } else {
            DbRecord part = records.get(getNumberofRecords(key));
            if (part == null) {
                value = null;
            } else {
                try {
                    value = part.get(key);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return value;
    }

    @Override
    public String getName() {
        return nameOfTable;
    }

    @Override
    public int rollback() {
        int rolledChanges = currentDataRecords.size();
        currentDataRecords.clear();
        return rolledChanges;
    }

    @Override
    public int commit() {
        int savedChanges = currentDataRecords.size();
        try {
            for (Map.Entry<String, String> pair : currentDataRecords.entrySet()) {
                DbRecord record = records.get(getNumberofRecords(pair.getKey()));
                if (pair.getValue() == null) {
                    record.remove(pair.getKey());
                } else {
                    if (record == null) {
                        int numberOfDir = Math.abs(pair.getKey().getBytes(DbRecord.CODE)[0]
                                % DbRecord.PARTITIONS);
                        int numberOfFile = Math.abs((pair.getKey().getBytes(DbRecord.CODE)[0]
                                / DbRecord.PARTITIONS) % DbRecord.PARTITIONS);
                        record = new DbRecord(pathToTable, numberOfDir, numberOfFile);
                        records.put(getNumberofRecords(pair.getKey()), record);
                    }
                    record.put(pair.getKey(), pair.getValue());
                }
            }
            currentDataRecords.clear();
            writeTable();
        } catch (IOException e) {
            throw new RuntimeException("Error writing table '" + getName()
                    + "' to its directory: " + e.getMessage(), e);
        }
        return savedChanges;
    }
}
