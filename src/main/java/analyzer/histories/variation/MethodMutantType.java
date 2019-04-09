package analyzer.histories.variation;

public enum MethodMutantType {
    /**
     类型分为三个子属性：type_call_rely
     type属性表示方法是新增或修改
        可取值为ADDED, CHANGED
     call属性表示方法被调用的方式
        可取值：
            WITHOUT：没有被调用
            INTRA: 被同一文件下的函数调用
            EXTERNAL: 被其他文件下的函数调用
     rely属性表示方法的是否调用了其他被修改的方法
        可取值：
            NONE:不依赖于其他函数修改
            SINGLE:依赖于一个函数修改
            MULTI:依赖于多个函数修改
     */
    ADD_WITHOUT_NONE(111, "新增函数，且函数：1，没有被调用；2，不依赖于其他被修改的函数！"),
    ADD_WITHOUT_SINGLE(112, "新增函数，且函数：1，没有被调用；2，依赖于1个其他被修改的函数！"),
    ADD_WITHOUT_MULTI(113, "新增函数，且函数：1，没有被调用；2，依赖于多个其他被修改的函数！"),
    ADD_INTRA_NONE(121, "新增函数，且函数：1，被内部调用；2，不依赖于其他被修改的函数！"),
    ADD_INTRA_SINGLE(122, "新增函数，且函数：1，被内部调用；2，依赖于1个其他被修改的函数！"),
    ADD_INTRA_MULTI(123, "新增函数，且函数：1，被内部调用；2，依赖于多个其他被修改的函数！"),
    ADD_EXTERNAL_NONE(131, "新增函数，且函数：1，被外部调用；2，不依赖于其他被修改的函数！"),
    ADD_EXTERNAL_SINGLE(132, "新增函数，且函数：1，被外部调用；2，依赖于1个其他被修改的函数！"),
    ADD_EXTERNAL_MULTI(133, "新增函数，且函数：1，被外部调用；2，依赖于多个其他被修改的函数！"),
    CHANGED_WITHOUT_NONE(211, "修改函数，且函数：1，没有被调用；2，不依赖于其他被修改的函数！"),
    CHANGED_WITHOUT_SINGLE(212, "修改函数，且函数：1，没有被调用；2，依赖于1个其他被修改的函数！"),
    CHANGED_WITHOUT_MULTI(213, "修改函数，且函数：1，没有被调用；2，依赖于多个其他被修改的函数！"),
    CHANGED_INTRA_NONE(221, "修改函数，且函数：1，被内部调用；2，不依赖于其他被修改的函数！"),
    CHANGED_INTRA_SINGLE(222, "修改函数，且函数：1，被内部调用；2，依赖于1个其他被修改的函数！"),
    CHANGED_INTRA_MULTI(223, "修改函数，且函数：1，被内部调用；2，依赖于多个其他被修改的函数！"),
    CHANGED_EXTERNAL_NONE(231, "修改函数，且函数：1，被外部调用；2，不依赖于其他被修改的函数！"),
    CHANGED_EXTERNAL_SINGLE(232, "修改函数，且函数：1，被外部调用；2，依赖于1个其他被修改的函数！"),
    CHANGED_EXTERNAL_MULTI(233, "修改函数，且函数：1，被外部调用；2，依赖于多个其他被修改的函数！");

    public int value;
    String description;

    MethodMutantType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public String toString() {
        return description;
    }

    public static MethodMutantType valueOf(int value) {
        for (MethodMutantType type: values()) {
            if (type.value == value)
                return type;
        }
        return null;
    }

    public static MethodMutantType toType(boolean newlyAdded, boolean isCalled, boolean externalCalled, int dependent) {
        int value = newlyAdded ? 100: 200;
        value += isCalled ? (externalCalled ? 30 : 20) : 10;
        value += dependent == 0 ? 1 : (dependent == 1 ? 2 : 3);
        return valueOf(value);
    }
}
