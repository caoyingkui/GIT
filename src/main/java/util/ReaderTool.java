package util;

import java.io.*;
import java.nio.file.Paths;

public class ReaderTool {
    public static String read(String filePath) {
        try {
            FileInputStream in = new FileInputStream(new File(filePath));
            byte[] bytes = new byte[in.available()];
            in.read(bytes, 0, in.available());
            in.close();
            return new String(bytes).replaceAll("\r", "");
        }catch (FileNotFoundException e) {
            System.out.println("No such file:" + Paths.get(filePath).toAbsolutePath().toString());
            return "";
        }catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String read(InputStream is) {
        try {
            byte[] bytes = new byte[is.available()];
            is.read(bytes, 0, is.available());
            is.close();
            return new String(bytes).replaceAll("\r", "");

        }catch (Exception e) {
            System.out.println("InputStream reading fail!");
            return "";
        }
    }
}
