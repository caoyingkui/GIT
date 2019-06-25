package fileDiff.group.hash.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * Created by kvirus on 2019/6/20 22:03
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class WhileVisitor extends ASTVisitor {
    public String condition = "";
    @Override
    public boolean visit(WhileStatement node) {
        condition = node.getExpression().toString();
        return false;
    }
}
