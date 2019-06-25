package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.CatchVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

/**
 * Created by kvirus on 2019/6/16 13:19
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ICatch extends InsertHash{
    public final int ACTION = 0;
    public final int STATEMENT = 1;
    public final int PARENT = 2;
    public final int EXCEPTION = 3;

    public ICatch(SourceCodeChange change) {
        assert (change instanceof Insert || change instanceof Delete) &&
                change.getChangedEntity().getType() == JavaEntityType.CATCH_CLAUSE;

        //Insert insert = (Insert) change;
        hashes = new int[4];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());

        hashes[EXCEPTION]   = change.getChangedEntity().getUniqueName().hashCode();
    }

    private int statementHash() {
        return getCode(ASTNode.CATCH_CLAUSE);
    }

    private CatchVisitor getVisitor(SourceCodeChange insert) {
        String code = insert.getChangedEntity().getUniqueName();
        parser.setKind(ASTParser.K_STATEMENTS);
        parser.setSource(code.toCharArray());
        Block block = (Block)parser.createAST(null);

        CatchVisitor v = new CatchVisitor();
        block.accept(v);
        return v;
    }




}
