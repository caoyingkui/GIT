package fileDiff.group.hash.update;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import fileDiff.group.hash.StatementHash;

/**
 * Created by kvirus on 2019/6/15 22:04
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class UpdateHash extends StatementHash {
    public UpdateHash(SourceCodeChange change) {
        content = change.getChangedEntity().getUniqueName();
    }
    public static UpdateHash getHash(SourceCodeChange change) {
        return new UUpdate(change);
    }
}
