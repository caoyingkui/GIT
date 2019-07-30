package fileDiff.group.hash.visitor;

import org.eclipse.jdt.core.dom.*;
import util.CompileTool;

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

    Map<String, String> names    = new HashMap<>();
    Map<String, String> strNames = new HashMap<>();
    Map<String, String> numNames = new HashMap<>();
    @Override
    public boolean preVisit2(ASTNode node) {
        if (node instanceof Type) return false;
        if (node instanceof SimpleName) {
            SimpleName snode = (SimpleName) node;
            String name = node.toString();
            char c = name.charAt(0);
            if (Character.isLowerCase(c)|| c == '_') {
                rename(snode);
            }
        }

        return true;
    }

    @Override
    public boolean visit(QualifiedName node) {
        SimpleName snode = node.getName();
        rename(snode);
        return false;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (node.getExpression() != null)
            node.getExpression().accept(this);
        for (Object p: node.arguments())
            ((ASTNode)p).accept(this);
        return false;
    }

    @Override
    public boolean visit(StringLiteral node) {
        rename(node);
        return false;
    }

    @Override
    public boolean visit(NumberLiteral node) {
        rename(node);
        return false;
    }

    private void rename(SimpleName snode) {
        String name      = snode.toString();
        if (names.containsKey(name)) {
            snode.setIdentifier(names.get(name));
        } else {
            String toName = "V" + (names.size() + 1);
            names.put(name, toName);
            snode.setIdentifier(toName);
        }
    }

    private void rename(StringLiteral node) {
        String name = node.toString();
        if (strNames.containsKey(name))
            node.setLiteralValue(strNames.get(name));
        else {
            String toName = "S" + (strNames.size() + 1);
            strNames.put(name, toName);
            node.setLiteralValue(toName);
        }
    }

    private void rename(NumberLiteral node) {
        String name = node.toString();
        if (numNames.containsKey(name))
            node.setToken(numNames.get(name));
        else {
            String toName = (numNames.size() + 1) + "";
            numNames.put(name, toName);
            node.setToken(toName);
        }
    }

    public static String rename(String code, RenameVisitor visitor) {
        Expression expression = CompileTool.getExpression(code);
        if (expression != null) {
            expression.accept(visitor);
            return expression.toString();
        } else {
            Block block = CompileTool.getBlock(code);
            if (block == null) return code;
            else {
                block.accept(visitor);
                return block.toString();
            }
        }
    }
}
