package fileDiff.field;

import fileDiff.Change;

/**
 * Created by kvirus on 2019/4/18 11:59
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class NewField extends FieldDiff{
    public String name;
    public String fullName;
    public String comment;

    public NewField(String name, String fullName, String comment) {
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
        if (!(obj instanceof NewField)) return false;

        NewField temp = (NewField) obj;
        return fullName.equals(temp.fullName);
    }

    @Override
    public String getName() {
        return name;
    }
}
