package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by kvirus on 2019/6/20 22:30
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IContinue extends InsertHash {
    private final int ACTION = 0;
    private final int STATEMENT = 1;
    private final int PARENT = 2;
    private final int KEY = 3;
    
    public IContinue(SourceCodeChange change) {
        super(change);
        assert (change instanceof Insert || change instanceof Delete || change instanceof Move) &&
                change.getChangedEntity().getType() == JavaEntityType.CONTINUE_STATEMENT;
        
        hashes = new int[4];
        hashes[ACTION] = typeHash(change);
        hashes[STATEMENT] = getCode(ASTNode.CONTINUE_STATEMENT);
        hashes[PARENT] = blockStatementHash(change.getParentEntity().getType());
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!(hash instanceof IContinue)) return false;

        for (int i = 0; i < hashes.length; i++)
            if (hashes[i] != hash.hashes[i]) return false;
        return true;
    }
}
