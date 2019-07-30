package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.AssignmentVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

/**
 * Created by kvirus on 2019/6/16 11:24
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IAssignment extends InsertHash {
    private final int ACTION    = 0;
    private final int STATEMENT = 1;
    private final int PARENT    = 2;
    private final int LEFTTYPE  = 3;
    private final int LEFTNAME  = 4;
    private final int RIGHTTYPE = 5;
    private final int OPTIONAL  = 6;
    private final int KEY       = 7;

    public IAssignment(SourceCodeChange change) {
        super(change);
        assert (change instanceof Insert || change instanceof Delete || change instanceof Move) &&
                change.getChangedEntity().getType() == JavaEntityType.ASSIGNMENT;

        //Insert insert = (Insert) change;

        hashes = new int[8];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());

        AssignmentVisitor v = getVisitor(change);
        hashes[LEFTTYPE]    = v.leftType;
        hashes[LEFTNAME]    = v.variableName.hashCode();
        hashes[RIGHTTYPE]   = v.init;
        hashes[OPTIONAL]    = v.optionalName.hashCode();
        hashes[KEY]         = 0;
    }

    private int statementHash() {
        return getCode(ASTNode.ASSIGNMENT);
    }

    private AssignmentVisitor getVisitor(SourceCodeChange insert) {
        String code = insert.getChangedEntity().getUniqueName();
        parser.setKind(ASTParser.K_STATEMENTS);
        parser.setSource(code.toCharArray());
        Block block = (Block) parser.createAST(null);

        AssignmentVisitor v = new AssignmentVisitor();
        block.accept(v);
        return v;
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
            return hashes[LEFTTYPE] == hash.hashes[LEFTTYPE] && (hashes[LEFTNAME] == hash.hashes[LEFTNAME] ||
                    hashes[PARENT] == hash.hashes[PARENT] && hashes[RIGHTTYPE] == hash.hashes[RIGHTTYPE] &&
                            hashes[OPTIONAL] == hash.hashes[OPTIONAL]);
        }
    }
}
