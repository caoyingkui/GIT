package fileDiff.modify.type;

/**
 * Created by kvirus on 2019/4/23 22:29
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public abstract class Modify {
    protected StringBuilder content = new StringBuilder();

    protected StringBuilder extension = new StringBuilder();

    protected abstract void build();

    public String getContent() {
        String result = content.toString();
        if (extension.length() > 0)
            result += ("\n") + extension;
        return result;
    }

    public void extend(String str) {
        if (extension.length() > 0)
            extension.append("\n");
        extension.append(str);
    }

    public abstract boolean equals(Object obj);
}
