package fileDiff.file;

import fileDiff.Diff;

/**
 * Created by kvirus on 2019/4/21 13:29
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public interface Update {
    public void update(Diff diff);

    public void parseFieldRelation(Diff diff);

    public void parseInterfaceRelation(Diff diff);
}
