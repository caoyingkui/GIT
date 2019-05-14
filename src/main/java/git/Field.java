package git;

/**
 * Created by kvirus on 2019/4/10 9:25
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **     **      **     **
 * |  **            *   *        **  **
 * |  **              *          ***
 * |  **              *          **  **
 * |   *******        *          **     **
 */
public class Field {
    public String type;
    public String fullName;
    public String name;
    public String comment;
    public int startLine;

    public Field(String type, String fullName, String name, String comment, int startLine) {
        this.type = type;
        this.fullName = fullName;
        this.name = name;
        this.comment = comment;
        this.startLine = startLine;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Field) )return false;
        Field term = (Field) obj;
        return fullName.equals(term.fullName);
    }

    public static boolean isBasicType(String type) {
        return (type.equals("int") ||
            type.equals("long") ||
            type.equals("float") ||
            type.equals("double") ||
            type.equals("char") ||
            type.equals("boolean") ||
            type.equals("Byte") ||
            type.equals("short")) ;
    }
}
