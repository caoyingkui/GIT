package fileDiff.field;

import fileDiff.Change;

/**
 * Created by kvirus on 2019/4/18 13:12
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ChangedField extends FieldDiff{
    public Change<String> name;

    public Change<String> fullName;

    public Change<String> comment;

    public ChangedField(Change<String> name, Change<String> fullName, Change<String> comment) {
        this.name = name;
        this.fullName = fullName;
        this.comment = comment;
    }

    @Override
    public String getName() {
        return name.NEW;
    }

    @Override
    public int hashCode() {
        return (fullName.NEW + fullName.OLD).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ChangedField)) return false;
        ChangedField temp = (ChangedField) obj;
        return fullName.NEW.equals(temp.fullName.NEW) && fullName.OLD.equals(temp.fullName.OLD);
    }
}
