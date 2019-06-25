package fileDiff.group.hash.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ThrowStatement;

/**
 * Created by kvirus on 2019/6/20 22:14
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ThrowVisitor extends ASTVisitor {
    public String exception = "";
    @Override
    public boolean visit(ThrowStatement node) {
        node.getExpression().accept(this);
        return false;
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        exception = node.getType().toString();
        return false;
    }
}
