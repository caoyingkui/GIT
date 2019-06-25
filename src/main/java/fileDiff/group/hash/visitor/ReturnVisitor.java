package fileDiff.group.hash.visitor;

import fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ReturnStatement;

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
    public int returnType = 0;

    @Override
    public boolean visit(ReturnStatement node) {
        returnType = StatementHash.getCode(node.getExpression());
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
