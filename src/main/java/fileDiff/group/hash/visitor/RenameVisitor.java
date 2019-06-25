package fileDiff.group.hash.visitor;

import org.eclipse.jdt.core.dom.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kvirus on 2019/6/22 13:36
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class RenameVisitor extends ASTVisitor {

    Map<String, String> names = new HashMap<>();
    @Override
    public boolean preVisit2(ASTNode node) {
        if (node instanceof Type) return false;
        if (node instanceof SimpleName) {
            SimpleName snode = (SimpleName) node;
            String name = node.toString();
            char c = name.charAt(0);
            if (Character.isLowerCase(c)|| c == '_') {
                if (names.containsKey(name)) {
                    snode.setIdentifier(names.get(name));
                } else {
                    String toName = "V" + (names.size() + 1);
                    names.put(name, toName);
                    snode.setIdentifier(toName);
                }
            }
        }

        return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        int a = 2;
        if (node.getExpression() != null)
            node.getExpression().accept(this);
        for (Object p: node.arguments())
            ((ASTNode)p).accept(this);
        return false;
    }
}
