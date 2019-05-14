package fileDiff.method;

/**
 * Created by kvirus on 2019/4/21 13:45
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public interface MethodUpdate {
    public void parseInterfaceRelation(MethodDiff method);

    public void parseRefactorRelation(MethodDiff method);
}
