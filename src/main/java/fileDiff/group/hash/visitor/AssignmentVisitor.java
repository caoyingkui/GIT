package fileDiff.group.hash.visitor;

import fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.*;

/**
 * Created by kvirus on 2019/6/16 11:33
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class AssignmentVisitor extends ASTVisitor {
    public String variableName = "";
    public int init = 0;


    // 该值记录上右值的名称信息;
    // 如果右值为new instance creation, 则记录该类型名
    // 如果右值为method invocation, 则记录该方法名。
    public String optionalName = "";

    private boolean s = false;

    @Override
    public boolean visit(Assignment node) {
        if (!s) {
            variableName = node.getLeftHandSide().toString();
            init = StatementHash.getCode(node.getRightHandSide());
            s = true;
            ASTNode right = node.getRightHandSide();
            if (right instanceof MethodInvocation ||
                right instanceof ClassInstanceCreation) {
                right.accept(this);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        optionalName = node.getType().toString();
        return false;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        optionalName = node.getName().toString();
        return false;
    }

    public static void main(String[] args) {
        String c = "a = b = new Integer(1);";
        StatementHash.parser.setSource(c.toCharArray());
        Block block = (Block)StatementHash.parser.createAST(null);

        AssignmentVisitor v = new AssignmentVisitor();
        block.accept(v);

        int a = 2;
    }

}
