package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

public class ReaderTool {
    public static String read(String filePath) {
        try {
            FileInputStream in = new FileInputStream(new File(filePath));
            byte[] bytes = new byte[in.available()];
            in.read(bytes, 0, in.available());
            return new String(bytes).replaceAll("\r", "");
        }catch (FileNotFoundException e) {
            System.out.println("No such file:" + Paths.get(filePath).toAbsolutePath().toString());
            return "";
        }catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
