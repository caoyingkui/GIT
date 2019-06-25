package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by kvirus on 2019/6/16 13:06
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ITry extends InsertHash{
    public final int ACTION     = 0;
    public final int STATEMENT  = 1;
    public final int PARENT     = 2;

    public ITry(SourceCodeChange change) {

        assert (change instanceof Insert || change instanceof Delete) &&
                change.getChangedEntity().getType() == JavaEntityType.TRY_STATEMENT;

        hashes = new int[3];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());
    }

    private int statementHash() {
        return getCode(ASTNode.TRY_STATEMENT);
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!super.equals(hash)) return false;

        return !strict || hashes[PARENT] == hash.hashes[PARENT];
    }
}
