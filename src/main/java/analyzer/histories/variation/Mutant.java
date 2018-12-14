package analyzer.histories.variation;

import javax.validation.constraints.NotNull;

/**
 * 一个Mutant对象对应了在利用GumtreeDiff生成的两个AST之间的叶子节点的变动关系
 * 例如，两棵树存在一处参数的变动，则对应的前后两个参数对应了该Mutant实体
 */
public class Mutant {
    @NotNull
    public MutantType type;
    public String before = ""; // 当type为INSERT, before为空
    public String after = "";  // 当type为DELETE, after为空
    public Mutant(MutantType type, String before, String after) {
        this.type = type;
        this.before = before;
        this.after = after;
    }
}
