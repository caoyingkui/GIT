package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.ClassInstanceCreationVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import util.CompileTool;

/**
 * Created by kvirus on 2019/7/24 23:35
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IClassInstanceCreation extends InsertHash{

    private final int ACTION    = 0;
    private final int STATEMENT = 1;
    private final int PARENT    = 2;
    private final int DATATYPE  = 3;

    public IClassInstanceCreation(SourceCodeChange change) {
        super(change);
        assert (change instanceof Insert || change instanceof Delete || change instanceof Move) &&
                change.getChangedEntity().getType() == JavaEntityType.CLASS_INSTANCE_CREATION;

        hashes = new int[4];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());

        ClassInstanceCreationVisitor visitor = getVisitor(change);
        hashes[DATATYPE]    = visitor.type.hashCode();
    }

    private int statementHash() {
        return getCode(ASTNode.CLASS_INSTANCE_CREATION);
    }

    private ClassInstanceCreationVisitor getVisitor(SourceCodeChange change) {
        Block block = CompileTool.getBlock(change.getChangedEntity().getUniqueName());

        ClassInstanceCreationVisitor visitor = new ClassInstanceCreationVisitor();
        block.accept(visitor);

        return visitor;
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!( hashes[0] == hash.hashes[0] && hashes[1] == hash.hashes[1])) return false;

        if (strict) {
            int len = hashes.length;
            for (int i = 2; i < len; i++)
                if (hashes[i] != hash.hashes[i]) return false;
            return true;
        } else {
            return hashes[DATATYPE] == hash.hashes[DATATYPE];
        }
    }
}
