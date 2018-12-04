package util;

import java.io.RandomAccessFile;
import java.util.List;

public class WriterTool {
    public static void append(String filename, String content){
        try {
            RandomAccessFile writer = new RandomAccessFile(filename, "rw");
            writer.seek(writer.length());
            writer.writeBytes(content);
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void append(String filename, List<String> lines){
        try {
            RandomAccessFile writer = new RandomAccessFile(filename, "rw");
            writer.seek(writer.length());
            for(String line: lines){
                writer.writeBytes(line + "\n");
            }
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
