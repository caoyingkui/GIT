package fileDiff.group.hash.move;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by kvirus on 2019/6/16 13:51
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class MMove extends MoveHash {
    public final int ACTION     = 0;
    public final int STATEMENT  = 1;
    public final int OLDPARENT  = 2;
    public final int NEWPARENT  = 3;
    public final int KEY        = 4;

    public MMove(SourceCodeChange change) {
        assert change instanceof Move;

        Move move = (Move) change;
        hashes              = new int[5];
        hashes[ACTION]      = typeHash();
        hashes[STATEMENT]   = StatementHash.statementHash(move.getNewEntity().getType());
        hashes[OLDPARENT]   = 0;
        hashes[NEWPARENT]   = StatementHash.statementHash(move.getNewParentEntity().getType());
        hashes[KEY]         = 0;
    }

    public int typeHash() {
        return StatementHash.MOVE;
    }



}
