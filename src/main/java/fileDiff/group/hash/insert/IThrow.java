package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.ThrowVisitor;
import fileDiff.group.hash.visitor.WhileVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

import java.io.IOException;

/**
 * Created by kvirus on 2019/6/20 22:11
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IThrow extends InsertHash {

    private final int ACTION = 0;
    private final int STATEMENT = 1;
    private final int PARENT = 2;
    private final int EXCEPTION = 3;
    private final int KEY = 4;

    public IThrow(SourceCodeChange change) {
        hashes = new int[5];
        hashes[ACTION] = typeHash(change);
        hashes[STATEMENT] = getCode(ASTNode.THROW_STATEMENT);
        hashes[PARENT] = blockStatementHash(change.getParentEntity().getType());
        ThrowVisitor visitor = getVisitor(change);
        hashes[EXCEPTION] = visitor.exception.hashCode();
    }

    final private ThrowVisitor getVisitor(SourceCodeChange change) {
        Block block = getBlock(change);
        ThrowVisitor visitor = new ThrowVisitor();
        block.accept(visitor);
        return visitor;
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!( hash instanceof IThrow )) return false;

        if (strict) {
            for (int i = 0; i < hashes.length; i++)
                if (hashes[i] != hash.hashes[i]) return false;
            return true;
        } else {
            return true;
        }
    }
}
