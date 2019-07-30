package fileDiff.group.hash.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;

/**
 * Created by kvirus on 2019/7/24 23:43
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ClassInstanceCreationVisitor extends ASTVisitor {
    public String type = "";

    @Override
    public boolean visit(ClassInstanceCreation node) {
        type = node.getType().toString();
        return false;
    }
}
