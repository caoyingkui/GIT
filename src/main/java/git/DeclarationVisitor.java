package git;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kvirus on 2019/6/30 14:59
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class DeclarationVisitor extends ASTVisitor {

    String sourceCode = "";

    List<Method> methods    = new ArrayList<>();
    List<Field>  fields     = new ArrayList<>();

    ClassParser parser      = null;

    public DeclarationVisitor(ClassParser parser) {
        this.parser = parser;
        sourceCode = parser.sourceCode;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        return false;
    }

    @Override
    public boolean visit(EnumConstantDeclaration node) {
        String qualifiedName = getDeclarationPath(node);
        String name = node.getName().toString();
        String fullName = qualifiedName.length() > 0 ? qualifiedName + "." : "";
        fullName += name;

        int startLine = parser.getLine(node.getStartPosition());

        Field field = new Field("ENUM", fullName, name, "", startLine);
        fields.add(field);

        return false;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        String qualifiedName = getDeclarationPath(node);
        int startLine = parser.getLine(node.getStartPosition());
        String filedType = node.getType().toString();
        for (Object f : node.fragments()) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) f;
            String name = fragment.getName().toString();
            String fullName = qualifiedName.length() > 0 ? qualifiedName  + "." : "";
            fullName += name;
            Field field = new Field(filedType,
                    fullName,
                    name,
                    "",
                    startLine);
            fields.add(field);
        }
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration node) {

        String methodName       = node.getName().toString();
        String name             = node.getName().toString();
        String parStr           = "";
        List parameters         = node.parameters();
        String qualifiedName    = getDeclarationPath(node);
        if(parameters.size() > 0){
            for(Object par: parameters){

                //在这里不能正常获取 “double... d1”,只能获取double，而不能获取“double...”
                if(par instanceof SingleVariableDeclaration){
                    SingleVariableDeclaration p = (SingleVariableDeclaration) par;

                    p.getName().accept(new ASTVisitor() {
                        @Override
                        public boolean visit(SimpleName node) {
                            node.setIdentifier("__TEST_NAME__");
                            return false;
                        }
                    });

                    String typeName = p.toString().replaceAll("__TEST_NAME__", "");
                    for (Object m: p.modifiers())
                        typeName = typeName.replaceAll(((ASTNode)m).toString(), "");

                    parStr += "," + typeName;
                } else {
                    assert 1 == 2;
                }
            }
            parStr = parStr.substring(1);
        }
        methodName += ("(" + parStr.replaceAll(" ", "") + ")");

        int startPosition = node.getStartPosition();
        int endPosition = startPosition + node.getLength() - 1;
        String fullName = qualifiedName.length() > 0 ? qualifiedName + "." : "";
        fullName += methodName;

        MethodBuilder builder = new MethodBuilder();
        Method method = builder.setFullName(fullName).setName(name)
                .setStartLine(parser.getLine(startPosition))
                .setEndLine(parser.getLine(endPosition))
                .setStartPos(startPosition)
                .setEndPos(endPosition)
                .setMethodContent(sourceCode.substring(startPosition, endPosition + 1))
                .setComment("")
                .setNode(node)
                .build();


        methods.add(method);

        return false;
    }

    public String getDeclarationPath(ASTNode node) {
        String path = "";
        node = node.getParent();
        while (node != null) {
            String name = "";

            if (node instanceof TypeDeclaration)        name = ((TypeDeclaration)(node)).getName().toString();
            else if (node instanceof EnumDeclaration)   name = ((EnumDeclaration)(node)).getName().toString();
            else if (node instanceof CompilationUnit) {
                PackageDeclaration pac = ((CompilationUnit)(node)).getPackage();
                if (pac != null) name = pac.getName().toString();
            }
            else System.out.println("declaration path error");

            if (name.length() > 0 ) {
                if (path.length() > 0) path = "." + path;
                path = name + path;
            }

            node = node.getParent();
        }

        return path;
    }
}
