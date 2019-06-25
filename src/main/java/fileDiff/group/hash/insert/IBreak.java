package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.BreakVisitor;
import fileDiff.modify.type.refactor.Lambda;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

/**
 * Created by kvirus on 2019/6/20 22:39
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IBreak extends InsertHash {
    private final int ACTION = 0;
    private final int STATEMENT = 1;
    private final int PARENT = 2;
    private final int INDENTIFIER = 3;
    private final int KEY = 4;

    public IBreak(SourceCodeChange change) {
        assert (change instanceof Insert || change instanceof Delete) &&
                change.getChangedEntity().getType() == JavaEntityType.BREAK_STATEMENT;
        hashes = new int[5];
        hashes[ACTION] = typeHash(change);
        hashes[STATEMENT] = getCode(ASTNode.BREAK_STATEMENT);
        hashes[PARENT] = blockStatementHash(change.getParentEntity().getType());

        BreakVisitor visitor = getVisitor(change);
        hashes[INDENTIFIER] = visitor.identifier.hashCode();
    }

    final private BreakVisitor getVisitor(SourceCodeChange change){
        Block block = getBlock(change);
        BreakVisitor visitor = new BreakVisitor();
        block.accept(visitor);
        return visitor;
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!(hash instanceof IBreak)) return false;

        if (strict) {
            for (int i = 0; i < hashes.length; i++)
                if (hashes[i] != hash.hashes[i]) return false;
            return true;
        } else {
            return true;
        }
    }
}
