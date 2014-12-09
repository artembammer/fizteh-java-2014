package ru.fizteh.fivt.students.artem_gritsay;


import ru.fizteh.fivt.storage.strings.Table;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.*;

public class DataBaseTable implements Table {
    private String nameoftable;
    public static final int PARTITIONS = 16;
    private Map<Integer, DbRecord> records;
    private Path pathtotable;
    private Map<String, String> currentDataRecords;
    public static final String CODE = "UTF-8";
    private static final String DIR_NAME = "([0-9]|1[0-5])\\.dir";
    private static final String FILE_NAME = "([0-9]|1[0-5])\\.dat";

    public DataBaseTable(Path pathtotable, String name) {
        records = new HashMap<>();
        currentDataRecords = new HashMap<>();
        this.pathtotable = pathtotable;
        nameoftable = name;
        try {
            readTable();
        } catch (DataBaseException e) {
            throw new RuntimeException("Error reading table '" + getName()
                    + "': " + e.getMessage(), e);
        }

    }

    private void readTable() throws DataBaseException {
        String[] listDir = pathtotable.toFile().list();
        for (String dir : listDir) {
            Path currentDir = pathtotable.resolve(dir);
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
                int numberofdir = Integer.parseInt(dir.substring(0, dir.length() - 4));
                int numberoffile = Integer.parseInt(file.substring(0, file.length() - 4));
                DbRecord record = new DbRecord(pathtotable, numberofdir, numberoffile);
                records.put(numberofdir * 100 + numberoffile, record);
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
        int numberoffile;
        int numberofdir;
        try {
            numberofdir = Math.abs(key.getBytes(CODE)[0] % PARTITIONS);
            numberoffile = Math.abs((key.getBytes(CODE)[0] / PARTITIONS) % PARTITIONS);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unable to encode key to " + CODE, e);
        }
        return numberofdir * PARTITIONS + numberoffile;
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

    @Override
    public String remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key is null");
        }
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
    public String put(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key or value is a null-string");
        }
        String oldValue;
        if (!currentDataRecords.containsKey(key)) {
            DbRecord record = records.get(getNumberofRecords(key));
            if (record == null) {
                oldValue = null;
            } else {
                try {
                    oldValue = record.get(key);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        } else {
            oldValue = currentDataRecords.remove(key);
        }
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
        return nameoftable;
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
                        int numberofdir = Math.abs(pair.getKey().getBytes(CODE)[0] % PARTITIONS);
                        int numberoffile = Math.abs((pair.getKey().getBytes(CODE)[0] / PARTITIONS) % PARTITIONS);
                        record = new DbRecord(pathtotable, numberofdir, numberoffile);
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
