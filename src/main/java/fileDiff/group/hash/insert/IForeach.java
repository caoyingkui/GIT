package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.ForeachVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import util.CompileTool;

/**
 * Created by kvirus on 2019/7/13 23:21
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IForeach extends InsertHash{
    private final int ACTION    = 0;
    private final int STATEMENT = 1;
    private final int PARENT    = 2;
    private final int TYPE      = 3;
    private final int INIT      = 4;
    private final int KEY       = 5;

    public IForeach(SourceCodeChange change) {
        super(change);
        assert (change instanceof Insert || change instanceof Delete || change instanceof Move) &&
                change.getChangedEntity().getType() == JavaEntityType.FOREACH_STATEMENT;


        hashes = new int[6];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = getCode(ASTNode.ENHANCED_FOR_STATEMENT);
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());

        ForeachVisitor visitor = getVisitor(change);
        hashes[TYPE]        = visitor.type.hashCode();
        hashes[INIT]        = visitor.expression.hashCode();
        hashes[KEY]         = 0;
    }

    private ForeachVisitor getVisitor(SourceCodeChange insert) {
        String code = insert.getChangedEntity().getUniqueName();
        code = "for(" + code + ") {}";
        Block block = CompileTool.getBlock(code);

        ForeachVisitor visitor = new ForeachVisitor();
        block.accept(visitor);
        return visitor;
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!super.equals(hash)) return false;
        if (strict) {
            int len = hashes.length;
            for (int i = 0; i < len; i++)
                if (hashes[i] != hash.hashes[i]) return false;
            return true;
        } else {
            return hashes[TYPE] == hash.hashes[TYPE] || hashes[INIT] == hash.hashes[INIT];
        }
    }
}
