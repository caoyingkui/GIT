package util;

import java.io.*;
import java.nio.file.Paths;

public class ReaderTool {
    public static String read(String filePath) {

        try {
            StringBuilder builder = new StringBuilder();
            FileInputStream in = new FileInputStream(new File(filePath));
            int available = 0;
            while ((available = in.available()) > 0) {
                if (available > 4096) available = 4096;
                byte[] bytes = new byte[available];
                in.read(bytes, 0, available);
                builder.append(new String(bytes).replaceAll("\r", ""));
            }
            in.close();
            return builder.toString();
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
