package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import fileDiff.group.hash.visitor.MethodVisitor;
import fileDiff.method.MethodDiff;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

/**
 * Created by kvirus on 2019/6/16 10:34
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IMethodInvocation extends InsertHash{

    private final int ACTION        = 0;
    private final int STATEMENT     = 1;
    private final int PARENT        = 2;
    private final int QUALIFIEDNAME = 3;
    private final int METHODNAME    = 4;
    private final int KEY           = 5;

    public IMethodInvocation(SourceCodeChange change) {
        assert change instanceof Insert &&
                change.getChangedEntity().getType() == JavaEntityType.METHOD_INVOCATION;

        //Insert insert = (Insert) change;

        hashes = new int[6];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());

        MethodVisitor visitor = getVisitor(change);
        hashes[QUALIFIEDNAME]   = visitor.qualifiedName.hashCode();
        hashes[METHODNAME]            = visitor.methodName.hashCode();
        hashes[KEY]             = 0;
    }

    private int statementHash() {
        return getCode(ASTNode.METHOD_INVOCATION);
    }

    private MethodVisitor getVisitor(SourceCodeChange insert) {
        Block block =  getBlock(insert);

        MethodVisitor visitor = new MethodVisitor();
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
            return hashes[METHODNAME] == hash.hashes[METHODNAME];
        }
    }

}
