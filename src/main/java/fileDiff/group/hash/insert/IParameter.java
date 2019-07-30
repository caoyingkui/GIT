package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by kvirus on 2019/7/7 10:07
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IParameter extends InsertHash {
    private final int ACTION    = 0;
    private final int STATEMENT = 1;
    private final int NAME      = 2;
    private final int KEY       = 3;

    public IParameter(SourceCodeChange change) {
        super(change);
        assert(change instanceof Insert || change instanceof Delete || change instanceof Move);

        hashes = new int[4];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[NAME]        = change.getChangedEntity().getUniqueName().hashCode();
    }

    private int statementHash() {
        return getCode(ASTNode.METHOD_DECLARATION);
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!super.equals(hash)) return false;

        return hashes[NAME] == hash.hashes[NAME];
    }
}
