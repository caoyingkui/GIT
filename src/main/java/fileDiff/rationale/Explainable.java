package fileDiff.rationale;

import description.Description;

import java.util.List;

/**
 * Created by kvirus on 2019/5/2 14:16
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public interface Explainable {
    public void matchDescription(List<Description> desList);
}
