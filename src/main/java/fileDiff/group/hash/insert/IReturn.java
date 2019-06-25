package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.ReturnVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

/**
 * Created by kvirus on 2019/6/16 12:42
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IReturn extends InsertHash{
    public final int ACTION     = 0;
    public final int STATEMENT  = 1;
    public final int PARENT     = 2;
    public final int RETURNTYPE = 3;
    public final int VALUETYPE  = 4;
    public final int KEY        = 5;

    public IReturn(SourceCodeChange change) {
        assert change instanceof Insert &&
                change.getChangedEntity().getType() == JavaEntityType.RETURN_STATEMENT;

        //Insert insert = (Insert) change;
        hashes = new int[6];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());

        ReturnVisitor v = new ReturnVisitor();
        hashes[RETURNTYPE]  = 0; // TODO
        hashes[VALUETYPE]   = v.returnType;
        hashes[KEY]         = 0;
    }

    private int statementHash() {
        return getCode(ASTNode.RETURN_STATEMENT);
    }

    private ReturnVisitor getVisitor(SourceCodeChange insert) {
        String code = insert.getChangedEntity().getUniqueName();
        parser.setKind(ASTParser.K_STATEMENTS);
        parser.setSource(code.toCharArray());
        Block block = (Block) parser.createAST(null);

        ReturnVisitor v = new ReturnVisitor();
        block.accept(v);
        return v;
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!super.equals(hash)) return false;

        if (strict) {
            int len = hashes.length;
            for (int i = 0; i < len; i ++)
                if (hashes[i] != hash.hashes[i]) return false;
            return false;
        } else {
            return hashes[RETURNTYPE] == hash.hashes[RETURNTYPE];
        }
    }
}
