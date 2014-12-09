package ru.fizteh.fivt.students.artem_gritsay;



import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by artem on 25.11.14.
 */
public class DbRecord {
    private int numberoffile;
    private int numberofdir;
    public static final String CODE = "UTF-8";
    public static final int PARTITIONS = 16;
    private Path pathfofile;
    private Map<String, String> data;

    public DbRecord(Path tableDirPath, int numberofdir, int numberoffile) throws DataBaseException {
        this.numberofdir = numberofdir;
        this.numberoffile = numberoffile;
        data = new HashMap<>();
        pathfofile = Paths.get(tableDirPath.toString(), numberofdir + ".dir", numberoffile + ".dat");
        if (pathfofile.toFile().exists()) {
            try {
                readFile();
            } catch (IOException e) {
                throw new DataBaseException("Cannot read file '" + pathfofile.toString() + "': " + e.getMessage(), e);
            }
        }
    }


    private void readFile() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(pathfofile.toString(), "r")) {
            ByteArrayOutputStream bufferBytes = new ByteArrayOutputStream();
            List<Integer> offsets = new LinkedList<>();
            List<String> keys = new LinkedList<>();
            int numberBytes = 0;
            byte b;
            do {
                while ((b = file.readByte()) != 0) {
                   numberBytes++;
                   bufferBytes.write(b);
                }
                numberBytes++;
                offsets.add(file.readInt());
                numberBytes += 4;
                String key = bufferBytes.toString(CODE);
                bufferBytes.reset();
                if (checkkey(key)) {
                    throw new IllegalArgumentException();
                }
                keys.add(key);
            } while (numberBytes < offsets.get(0));
            offsets.add((int) file.length());
            offsets.remove(0);
            Iterator<String> iter = keys.iterator();
            for (int nextoffset : offsets) {
                while (numberBytes < nextoffset) {
                    bufferBytes.write(file.readByte());
                    numberBytes++;
                }
                if (bufferBytes.size() > 0) {
                    if (data.put(iter.next(), bufferBytes.toString(CODE)) != null) {
                        throw new IllegalArgumentException("Key already exist in file");
                    }
                    bufferBytes.reset();
                } else {
                    throw new EOFException();
                }
            }
            bufferBytes.close();
        } catch (UnsupportedEncodingException e) {
            throw new IOException("Can't be encoded to " + CODE, e);
        } catch (IllegalArgumentException e) {
            throw new IOException("Wrong file or directory for key", e);
        } catch (EOFException e) {
            throw new IOException("File breaks", e);
        } catch (IOException e) {
            throw new IOException("Unable to read");
        }
    }
    private boolean checkkey(String key) throws UnsupportedEncodingException {
        int expectednumberofdir = Math.abs(key.getBytes(CODE)[0] % PARTITIONS);
        int expectednumberoffile = Math.abs((key.getBytes(CODE)[0] / PARTITIONS) % PARTITIONS);
        return (numberofdir == expectednumberofdir && numberoffile == expectednumberoffile);
    }
    private void writeToFile() throws IOException {
        pathfofile.getParent().toFile().mkdir();
        try (RandomAccessFile file = new RandomAccessFile(pathfofile.toString(), "rw")) {
            file.setLength(0);
            Set<String> keys = data.keySet();
            List<Integer> offsetsPos = new LinkedList<>();
            for (String currentKey : keys) {
                file.write(currentKey.getBytes(CODE));
                file.write('\0');
                offsetsPos.add((int) file.getFilePointer());
                file.writeInt(0);
            }
            List<Integer> offsets = new LinkedList<>();
            for (String currentKey : keys) {
                offsets.add((int) file.getFilePointer());
                file.write(data.get(currentKey).getBytes(CODE));
            }
            Iterator<Integer> offIter = offsets.iterator();
            for (int offsetPos : offsetsPos) {
                file.seek(offsetPos);
                file.writeInt(offIter.next());
            }
        } catch (FileNotFoundException e) {
            throw new IOException("Unable to create file", e);
        } catch (UnsupportedEncodingException e) {
            throw new IOException("Key or value can't be encoded to " + CODE, e);
        } catch (IOException e) {
            throw new IOException("Unable to write to file", e);
        }
    }

    public Set<String> list() {
        return data.keySet();
    }

    public int getNumberOfRecords() {
        return data.size();
    }

    public String remove(String key) throws UnsupportedEncodingException {
        if (key == null || !checkkey(key)) {
          throw new IllegalArgumentException(key + " not exist in file");
        } else {
            return data.remove(key);
        }
    }

    public String put(String key, String value) throws UnsupportedEncodingException {
        if (key == null || !checkkey(key)) {
            throw new IllegalArgumentException(key + "cannot be replaced");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return data.put(key, value);
    }

    public String get(String key) throws UnsupportedEncodingException {
        if (key == null || !checkkey(key)) {
            throw new IllegalArgumentException("'" + key + "' can't be found in this file");
        }
        return data.get(key);
    }

    public void commit() throws DataBaseException {
        if (getNumberOfRecords() == 0) {
            pathfofile.toFile().delete();
            pathfofile.getParent().toFile().delete();
        } else {
            try {
                writeToFile();
            } catch (IOException e) {
                throw new DataBaseException("Error writing to file '"
                        + pathfofile.toString() + "': " + e.getMessage(), e);
            }
        }
    }




}

