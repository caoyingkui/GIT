package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.PrefixExpressionVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import util.CompileTool;

/**
 * Created by kvirus on 2019/7/24 23:57
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IPrefixExpression extends InsertHash {
    private int ACTION      = 0;
    private int STATEMENT   = 1;
    private int PARENT      = 2;
    private int VARIABLE    = 3;
    private int OP    = 4;
    private int KEY         = 5;

    public IPrefixExpression(SourceCodeChange change) {
        super(change);

        hashes = new int[6];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());

        PrefixExpressionVisitor visitor = getVisitor(change);
        hashes[VARIABLE]    = visitor.variable.hashCode();
        hashes[OP]    = visitor.operator.hashCode();

    }

    private int statementHash() {
        return getCode(ASTNode.PREFIX_EXPRESSION);
    }

    private PrefixExpressionVisitor getVisitor(SourceCodeChange change) {
        Block block = CompileTool.getBlock(change.getChangedEntity().getUniqueName());
        PrefixExpressionVisitor visitor = new PrefixExpressionVisitor();
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
