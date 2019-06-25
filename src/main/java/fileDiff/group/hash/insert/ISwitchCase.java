package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by kvirus on 2019/6/21 7:55
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ISwitchCase extends InsertHash {
    private final int ACTION = 0;
    private final int STATEMENT = 1;
    private final int PARENT = 2;
    private final int EXPRESSION = 3;
    private final int KEY = 4;

    public ISwitchCase(SourceCodeChange change) {
        assert (change instanceof Insert || change instanceof Delete) &&
                change.getChangedEntity().getType() == JavaEntityType.SWITCH_CASE;

        hashes = new int[5];
        hashes[ACTION] = typeHash(change);
        hashes[STATEMENT] = getCode(ASTNode.SWITCH_CASE);
        hashes[PARENT] = blockStatementHash(change.getParentEntity().getType());
        String expression = change.getChangedEntity().getUniqueName();
        hashes[EXPRESSION] = expression.hashCode();
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!(hash instanceof ISwitchCase)) return false;

        if (strict) {
            for (int i = 0; i < hashes.length; i++)
                if (hashes[i] != hash.hashes[i]) return false;
            return true;
        } else {
            return true;
        }
    }
}
