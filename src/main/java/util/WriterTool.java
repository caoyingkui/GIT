package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.List;

public class WriterTool {
    public static void write(String filename, String content){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
            writer.write(content);
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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
