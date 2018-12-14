package analyzer.histories.variation;

import javax.validation.constraints.NotNull;

public enum MutantType {
    UPDATE(1, "变元对应的叶子节点存在置换关系"),
    INSERT(2, "变元对应的是新插入一个叶子节点"),
    DELETE(3, "变元对应的是删除一个叶子节点"),
    CLASSNAME(4, "类名"),
    METHODNAME(5, "方法名");
    int value;
    String description;

    MutantType(int value, String description) {
       this.value = value;
       this.description = description;
    }

    @NotNull
    public static MutantType valueOf(int value) {
        MutantType result = null;
        for (MutantType type: values()) {
            if (type.value == value) {
                result = type;
                break;
            }
        }
        return result;
    }

    public String toString() {
        return this.description;
    }
}
