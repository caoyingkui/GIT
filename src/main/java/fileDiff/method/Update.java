package fileDiff.method;

import fileDiff.Diff;

/**
 * Created by kvirus on 2019/4/21 14:40
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
}
