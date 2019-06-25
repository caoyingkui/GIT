package fileDiff.modify.type.signatureChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import fileDiff.Change;
import fileDiff.file.ChangedClass;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;

/**
 * Created by kvirus on 2019/5/19 20:23
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class ReturnTypeChange extends Modify {
    public static int count = 0;
    public Change<String> returnType;

    public static ReturnTypeChange match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        ReturnTypeChange result = new ReturnTypeChange();
        ChangedMethod cMethod = (ChangedMethod) method;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (change.getChangeType() == ChangeType.RETURN_TYPE_CHANGE) {

                Update update = (Update)change;
                SourceCodeEntity ne = update.getNewEntity();
                SourceCodeEntity ce = update.getChangedEntity();

                ChangedClass cClass = (ChangedClass)cMethod.file;
                String oldType = cClass.content.OLD.substring(ce.getStartPosition(), ce.getEndPosition() + 1);
                String newType = cClass.content.NEW.substring(ne.getStartPosition(), ce.getEndPosition() + 1);
                result.returnType = new Change<String>(oldType, newType);
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
