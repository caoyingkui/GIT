package fileDiff.group.hash.visitor;

import fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.*;

/**
 * Created by kvirus on 2019/6/16 9:06
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class DeclarationVisitor extends ASTVisitor {
    public String dataType = "";
    public String variableName = "";

    public int init = 0;

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        dataType = node.getType().toString();

        VariableDeclarationFragment f = (VariableDeclarationFragment)(node.fragments().get(0));

        variableName = f.getName().toString();

        init = StatementHash.getCode(f.getInitializer());

        return true;
    }
}
