package fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import fileDiff.group.hash.StatementHash;

/**
 * Created by kvirus on 2019/6/16 16:26
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IDefault extends InsertHash{
    public final int ACTION = 0;
    public final int STATEMENT = 1;
    public final int PARENT = 2;

    public IDefault(SourceCodeChange change) {
        super(change);
        if(change.getChangedEntity().getType().isStatement()) {
            System.out.println("Default:" + change.getChangedEntity().getUniqueName());
            System.out.println(change.getChangedEntity().getType());
            System.out.println();
        }

        assert (change instanceof Insert || change instanceof Delete || change instanceof Move);

        hashes = new int[3];
        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = 0;
        hashes[PARENT]      = StatementHash.blockStatementHash(change.getParentEntity().getType());
    }


}
