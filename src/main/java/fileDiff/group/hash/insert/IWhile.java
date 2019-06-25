package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.visitor.WhileVisitor;
import fileDiff.modify.type.refactor.Lambda;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

/**
 * Created by kvirus on 2019/6/20 20:54
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IWhile extends InsertHash{

    private final int ACTION = 0;
    private final int STATEMENT = 1;
    private final int PARENT = 2;
    private final int CONDITION = 3;
    private final int KEY = 4;

    public IWhile(SourceCodeChange change) {
        assert (change instanceof Insert || change instanceof Delete) &&
            change.getChangedEntity().getType() == JavaEntityType.WHILE_STATEMENT;
        hashes = new int[5];
        hashes[ACTION] = typeHash(change);
        hashes[STATEMENT] = getCode(ASTNode.WHILE_STATEMENT);
        hashes[PARENT] = blockStatementHash(change.getParentEntity().getType());

        WhileVisitor visitor = getVisitor(change);
        hashes[CONDITION] = visitor.condition.hashCode();
    }

    final private WhileVisitor getVisitor(SourceCodeChange change) {
        Block block = getBlock(change);
        WhileVisitor visitor = new WhileVisitor();
        block.accept(visitor);
        return visitor;
    }
}
