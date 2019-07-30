package git;

import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Created by kvirus on 2019/6/30 15:35
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class MethodBuilder {
    Method method = new Method();

    public MethodBuilder setFullName(String fullName) {
        method.setFullName(fullName);
        return this;
    }

    public MethodBuilder setName(String name) {
        method.setName(name);
        return this;
    }

    public MethodBuilder setStartLine(int startLine) {
        method.setStartLine(startLine);
        return this;
    }

    public MethodBuilder setEndLine(int endLine) {
        method.setEndLine(endLine);
        return this;
    }

    public MethodBuilder setStartPos(int startPos) {
        method.setStartPos(startPos);
        return this;
    }

    public MethodBuilder setEndPos(int endPos) {
        method.setEndPos(endPos);
        return this;
    }

    public MethodBuilder setMethodContent(String methodContent) {
        method.setMethodContent(methodContent);
        return this;
    }

    public MethodBuilder setComment(String comment) {
        method.setComment(comment);
        return this;
    }

    public MethodBuilder setNode(MethodDeclaration node) {
        method.setNode(node);
        return this;
    }

    public Method build() {
        return method;
    }
}
