package fileDiff.method;

import fileDiff.Diff;

/**
 * Created by kvirus on 2019/4/21 14:25
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public interface FieldUpdate {

    /**
     * MethodDiff首先在初始化只能考虑到当前函数的内容的变化，而无法考虑到全局的，也就是整个一次commit的信息
     * 因此，update传入一个参数diff，diff记录了一次commit中的一些重要信息
     * 用传入的diff来用全局的信息来更新MethodDiff
     * @param diff
     */
    public void fieldUpdate(Diff diff);
}
