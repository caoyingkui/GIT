package fileDiff.type;

/**
 * Created by kvirus on 2019/4/9 21:23
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **     **      **     **
 * |  **            *   *        **  **
 * |  **              *          ***
 * |  **              *          **  **
 * |   *******        *          **     **
 */
public enum DiffType {
    Field_Add(101, "newly added field"),
    Field_Changed(102, "changed field"),
    Field_Delete(103, "deleted field"),
    Method_Add(201, "添加一个函数"),
    Method_Add_In_Interface(202, "一个接口中添加一个函数"),
    Method_Add_By_Interface(203, "实现的接口中增加一个函数导致其增加一个函数"),
    Method_Add_By_Refactor(204, "方法是由于对其他函数的重构，导致添加的"),
    Method_Changed(211, ""),
    Method_Refactor(221, ""),
    Method_Delete(231, ""),
    Method_Delete_In_Interface(232, ""),
    Method_Delete_Interface(233, ""),
    Class_New(301, ""),
    Class_Changed(311, ""),
    Class_Delete(321, ""),
    Interface_Add(401, ""),
    Interface_Changed(411, ""),
    Interface_Deleted(421,"");

    int value;
    String description;

    DiffType(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
