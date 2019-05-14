package fileDiff.field;

import fileDiff.Change;

/**
 * Created by kvirus on 2019/4/18 13:10
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class DelField extends FieldDiff{
    public String comment;
    public String name;
    public String fullName;

    public DelField(String name, String fullName, String comment) {
        this.name = name;
        this.fullName = fullName;
        this.comment = comment;
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof DelField)) return false;

        DelField temp = (DelField) obj;
        return fullName.equals(temp.fullName);
    }

    @Override
    public String getName() {
        return name;
    }
}
