package fileDiff.modify.type.javadoc;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;

/**
 * Created by kvirus on 2019/5/20 9:14
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */

@Order(order = Order.OrderValue.JAVADOC)
public class JavadocDelete extends Modify {
    public static int count = 0;
    public String javadoc;
    public static JavadocDelete match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        JavadocDelete result = new JavadocDelete();
        ChangedMethod cMethod = (ChangedMethod) method;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (change.getChangeType() == ChangeType.DOC_DELETE) {
                Delete delete = (Delete) change;
                result.javadoc = delete.getChangedEntity().getUniqueName();
                s = true;
                break;
            }
        }
        return s && (++count) > 0 ? result : null;
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
