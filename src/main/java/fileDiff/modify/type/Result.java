package fileDiff.modify.type;

/**
 * Created by kvirus on 2019/5/22 15:43
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class Result {
    public int allMethods = 0;
    public int changedMethods = 0;
    public int classified = 0;

    public void add(Result result) {
        allMethods += result.allMethods;
        changedMethods += result.changedMethods;
        classified += result.classified;
    }

    public void print() {
        System.out.println(classified + " in " + changedMethods + " " + allMethods);
    }
}
