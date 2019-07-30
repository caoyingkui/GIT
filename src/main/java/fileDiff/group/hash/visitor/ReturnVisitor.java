package fileDiff.group.hash.visitor;

import fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.*;

/**
 * Created by kvirus on 2019/6/16 12:47
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ReturnVisitor extends ASTVisitor {
    public String expression = "";

    @Override
    public boolean visit(ReturnStatement node) {
        Expression exp = node.getExpression();
        if (exp instanceof MethodInvocation) {
            expression = ((MethodInvocation)exp).getName().toString();
        } else {
            node.accept(new RenameVisitor());
            expression = node.getExpression().toString();
        }
        return false;
    }

    public static void main(String[] args) {
        String c = "return new Integer(2);";
        StatementHash.parser.setSource(c.toCharArray());
        Block block = (Block)StatementHash.parser.createAST(null);

        ReturnVisitor v = new ReturnVisitor();
        block.accept(v);

        int a = 2;
    }
}
