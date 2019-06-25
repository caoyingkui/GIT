package fileDiff.modify.type.tryCatchStatement;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;

/**
 * Created by kvirus on 2019/5/24 21:34
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.TRY)
public class TryCatchDelete extends Modify {

    public static int count = 0;

    public static TryCatchDelete match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        TryCatchDelete result = new TryCatchDelete();
        ChangedMethod cMethod = (ChangedMethod) method;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (!( change instanceof Delete)) continue;

            if (change.getChangedEntity().getType() == JavaEntityType.TRY_STATEMENT) {
                s = true;
                break;
            }
        }

        if (s) {
            count ++;
            return result;
        } else {
            return null;
        }
    }

    @Override
    protected void build() {

    }

    @Override
    public String getContent() {
        return super.getContent();
    }

    @Override
    public void extend(String str) {
        super.extend(str);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
