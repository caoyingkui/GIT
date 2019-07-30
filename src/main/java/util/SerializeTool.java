package util;

import java.io.*;

/**
 * Created by kvirus on 2019/7/8 14:35
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class SerializeTool {

    public static <T> T read(File file) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            return (T) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> void writer(File file, T object) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
