package fileDiff.modify.type.tryCatchStatement;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.method.ChangedMethod;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import javafx.util.Pair;
import util.RangeTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kvirus on 2019/5/24 21:21
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.TRY)
public class CatchInsert extends Modify {
    public static int count = 0;

    public static CatchInsert match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        CatchInsert result = new CatchInsert();
        ChangedMethod cMethod = (ChangedMethod) method;
        List<Pair<Integer, Integer>> ranges = new ArrayList<>();
        List<SourceCodeChange> toCheck = new ArrayList<>();

        cMethod.getSourceCodeChanges().stream().filter(change -> {
            if (change.getChangedEntity().getType() == JavaEntityType.CATCH_CLAUSE) return true;

            if (change instanceof Insert && change.getChangedEntity().getType() == JavaEntityType.TRY_STATEMENT) {
                int start = change.getChangedEntity().getStartPosition();
                int end = change.getChangedEntity().getEndPosition();
                ranges.add(new Pair<Integer, Integer>(start, end));
            }
            return false;
        }).forEach(change -> toCheck.add(change));

        if (toCheck.size() > 0) {
            for (SourceCodeChange change : toCheck) {
                int start = change.getChangedEntity().getStartPosition();
                int end = change.getChangedEntity().getEndPosition();
                if (!(RangeTool.contains(ranges, start, end))) {
                    s = true;
                    break;
                }

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
