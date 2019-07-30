package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.ReturnVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.HashSet;
import java.util.Set;

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

    static Set<Integer> basicTypes = new HashSet<>();
    static {
        basicTypes.add("int".hashCode());       basicTypes.add("Integer".hashCode());
        basicTypes.add("long".hashCode());      basicTypes.add("Long".hashCode());
        basicTypes.add("short".hashCode());     basicTypes.add("Short".hashCode());
        basicTypes.add("float".hashCode());     basicTypes.add("Float".hashCode());
        basicTypes.add("double".hashCode());    basicTypes.add("Double".hashCode());
        basicTypes.add("char".hashCode());      basicTypes.add("Char".hashCode());
        basicTypes.add("byte".hashCode());      basicTypes.add("Byte".hashCode());
        basicTypes.add("boolean".hashCode());   basicTypes.add("Boolean".hashCode());
        basicTypes.add("String".hashCode());    basicTypes.add("void".hashCode());

    }


    public IReturn(SourceCodeChange change, MethodDeclaration newDeclaration, MethodDeclaration oldDeclaration) {
        super(change);
        assert (change instanceof Insert || change instanceof Delete || change instanceof Move) &&
                change.getChangedEntity().getType() == JavaEntityType.RETURN_STATEMENT;

        //Insert insert = (Insert) change;
        hashes = new int[6];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());



        if (newDeclaration == null || oldDeclaration == null) {
            hashes[RETURNTYPE] = 0;
        } else {
            Object type         = change instanceof Insert ? newDeclaration.getReturnType2() : oldDeclaration.getReturnType2();
            hashes[RETURNTYPE]  = type == null ? "".hashCode() : type.toString().hashCode();
        }
        ReturnVisitor v = getVisitor(change);
        hashes[VALUETYPE]   = v.expression.hashCode();
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
            return hashes[RETURNTYPE] == hash.hashes[RETURNTYPE] && !basicTypes.contains(hashes[RETURNTYPE])
                    || hashes[VALUETYPE] == hash.hashes[VALUETYPE];
        }
    }
}
