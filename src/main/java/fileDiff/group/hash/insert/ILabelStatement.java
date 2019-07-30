package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by kvirus on 2019/7/25 0:30
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ILabelStatement extends InsertHash{
    private int ACTION      = 0;
    private int STATEMENT   = 1;
    private int PARENT      = 2;
    private int LABEL       = 3;
    private int KEY         = 4;

    public ILabelStatement(SourceCodeChange change) {
        super(change);

        hashes = new int[5];
        hashes = new int[6];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());
        hashes[LABEL]       = change.getChangedEntity().getUniqueName().hashCode();
    }

    private int statementHash() {
        return getCode(ASTNode.LABELED_STATEMENT);
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!( hashes[0] == hash.hashes[0] && hashes[1] == hash.hashes[1])) return false;

        int len = hashes.length;
        for (int i = 2; i < len; i++)
            if (hashes[i] != hash.hashes[i]) return false;
        return true;
    }
}
