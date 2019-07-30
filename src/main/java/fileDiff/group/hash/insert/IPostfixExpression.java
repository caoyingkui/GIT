package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.MethodVisitor;
import fileDiff.group.hash.visitor.PostfixExpressionVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

/**
 * Created by kvirus on 2019/6/20 20:28
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IPostfixExpression extends InsertHash {
    private final int ACTION    = 0;
    private final int STATEMENT = 1;
    private final int PARENT    = 2;
    private final int VARIABLE  = 3;
    private final int OP        = 4;
    private final int KEY       = 5;

    public IPostfixExpression(SourceCodeChange change) {
        super(change);
        assert (change instanceof Insert || change instanceof Delete || change instanceof Move) &&
                change.getChangedEntity().getType() == JavaEntityType.POSTFIX_EXPRESSION;

        hashes = new int[5];
        hashes[ACTION] = typeHash(change);
        hashes[STATEMENT] = getCode(ASTNode.POSTFIX_EXPRESSION);
        hashes[PARENT] = blockStatementHash(change.getParentEntity().getType());

        PostfixExpressionVisitor visitor = getVisitor(change);
        hashes[OP] = visitor.op.hashCode();
        hashes[VARIABLE] = visitor.expression.hashCode();
    }

    public PostfixExpressionVisitor getVisitor(SourceCodeChange change) {
        Block block = InsertHash.getBlock(change);
        PostfixExpressionVisitor visitor = new PostfixExpressionVisitor();
        block.accept(visitor);
        return visitor;
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!(hash instanceof IPostfixExpression || hash instanceof IPrefixExpression)) return false;

        if (strict) {
            int len = hashes.length;
            for (int i = 0; i < len; i++)
                if (hashes[i] != hash.hashes[i]) return false;
            return true;
        } else {
            return hashes[OP] == hash.hashes[OP];
        }
    }
}
