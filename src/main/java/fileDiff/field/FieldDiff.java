package fileDiff.field;

import fileDiff.Change;
import fileDiff.method.MethodDiff;
import fileDiff.rationale.Rationale;

import java.util.List;

/**
 * Created by kvirus on 2019/4/18 11:21
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public abstract class FieldDiff implements Rationale {
    public String rationale = "";


    //记录该域的改动，影响到了哪些函数
    public List<MethodDiff> affectedMethods;

    public abstract int hashCode() ;

    @Override
    public abstract boolean equals(Object obj);

    public abstract String getName();

    @Override
    public String getRationale() {
        return rationale;
    }

    @Override
    public void addRationale(String str) {
        if (rationale.length() > 0)
            rationale += "\n";
        rationale += str;
        rationale = rationale.trim();
    }

    @Override
    public void setRationale(String str) {
        rationale = str;
    }

    @Override
    public boolean hasRationale() {
        return rationale.length() > 0;
    }
}
