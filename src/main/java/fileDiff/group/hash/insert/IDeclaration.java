package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.DeclarationVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;

/**
 * Created by kvirus on 2019/6/16 8:19
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IDeclaration extends InsertHash {

    private final int ACTION    = 0;
    private final int STATEMENT = 1;
    private final int PARENT    = 2;
    private final int DATATYPE  = 3;
    private final int VARIABLE  = 4;
    private final int INIT      = 5;
    private final int KEY       = 6;


    public IDeclaration(SourceCodeChange change) {
        assert (change instanceof Insert || change instanceof Delete) &&
                change.getChangedEntity().getType() == JavaEntityType.VARIABLE_DECLARATION_STATEMENT;

        //Insert insert = (Insert) change;

        hashes = new int[7];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementTypeHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());

        DeclarationVisitor v = getVisitor(change);
        hashes[DATATYPE]    = v.dataType.hashCode();
        hashes[VARIABLE]    = v.variableName.hashCode();
        hashes[INIT]        = v.init;
        hashes[KEY]         = 0;
    }

    private int statementTypeHash() {
        return getCode(ASTNode.VARIABLE_DECLARATION_STATEMENT);
    }

    private DeclarationVisitor getVisitor(SourceCodeChange insert) {
        String code = insert.getChangedEntity().getUniqueName();
        parser.setKind(ASTParser.K_STATEMENTS);
        parser.setSource(code.toCharArray());
        Block block = (Block) parser.createAST(null);

        DeclarationVisitor visitor = new DeclarationVisitor();
        block.accept(visitor);

        return visitor;
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!(hash instanceof IDeclaration)) return false;

        IDeclaration de = (IDeclaration) hash;

        if (strict) {
            int len = hashes.length;
            for (int i = 0; i < len; i++)
                if (de.hashes[i] != hashes[i]) return false;
            return true;
        } else {
            return hashes[DATATYPE] == de.hashes[DATATYPE] ||
                    hashes[VARIABLE] == de.hashes[VARIABLE];
        }
    }
}