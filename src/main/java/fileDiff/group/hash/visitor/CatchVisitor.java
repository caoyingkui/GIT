package fileDiff.group.hash.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;

/**
 * Created by kvirus on 2019/6/16 13:16
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class CatchVisitor extends ASTVisitor{
    public String exception = "";

    @Override
    public boolean visit(CatchClause node) {
        exception = node.getException().getType().toString();
        return false;
    }
}
