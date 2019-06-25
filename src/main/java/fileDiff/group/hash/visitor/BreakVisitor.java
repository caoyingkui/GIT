package fileDiff.group.hash.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;

/**
 * Created by kvirus on 2019/6/20 22:40
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class BreakVisitor extends ASTVisitor {
    public String identifier = "";

    @Override
    public boolean visit(BreakStatement node) {
        if (node.getLabel() != null)
            identifier = node.getLabel().toString();
        return false;
    }
}
