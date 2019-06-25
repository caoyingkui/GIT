package fileDiff.group.hash.update;

import gumtreediff.actions.model.*;
import gumtreediff.tree.ITree;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by kvirus on 2019/6/5 20:48
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ActionHash {
    public static final int LOOSE = 1;
    public static final int MEDIUM = 2;
    public static final int STRICT = 3;

    final Action action;

    final int typeHash;
    final int rootHash;
    final int nodeHash;
    final int posHash;
    final int valueHash;

    public ActionHash(Action action, int typeHash, int rootHash, int nodeHash, int posHash, int valueHash) {
        this.action = action;
        this.typeHash = typeHash;
        this.rootHash = rootHash;
        this.nodeHash = nodeHash;
        this.posHash = posHash;
        this.valueHash = valueHash;
    }

    public boolean isSimilar(ActionHash hash, int level) {
        if (level == STRICT) {
            return typeHash == hash.typeHash &&
                    rootHash == hash.rootHash &&
                    nodeHash == hash.nodeHash &&
                    posHash == hash.posHash &&
                    valueHash == hash.valueHash;
        } else if (level == MEDIUM) {
            return typeHash == hash.typeHash &&
                    rootHash == hash.rootHash &&
                    nodeHash == hash.nodeHash &&
                    posHash == hash.posHash;
        } else if (level == LOOSE) {
            return typeHash == hash.typeHash &&
                    Math.abs(rootHash - hash.rootHash) < 1000 &&
                    valueHash == hash.valueHash;
        }

        return false;
    }

    public static int getTypeHash(Action action) {
        if (action instanceof Insert) return 2;
        else if (action instanceof Delete) return 4;
        else if (action instanceof Update) return 8;
        else if (action instanceof Move) return 6;
        else return 0;
    }

    public static int getRootHash(ITree node) {
        if (node == null) return 0;
        int type = node.getType();
        if (type == ASTNode.PACKAGE_DECLARATION) return 100;
        if (type == ASTNode.DIMENSION) return 200;
        if (type == ASTNode.METHOD_REF_PARAMETER) return 300;
        if (type == ASTNode.COMPILATION_UNIT) return 400;
        if (type == ASTNode.MODIFIER) return 500;
        if (type == ASTNode.MEMBER_VALUE_PAIR) return 600;
        if (type == ASTNode.INITIALIZER) return 7010;
        if (type == ASTNode.ENUM_DECLARATION) return 7021;
        if (type == ASTNode.TYPE_DECLARATION) return 7022;
        if (type == ASTNode.ANNOTATION_TYPE_DECLARATION) return 7023;
        if (type == ASTNode.FIELD_DECLARATION) return 7030;
        if (type == ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION) return 7040;
        if (type == ASTNode.ENUM_CONSTANT_DECLARATION) return 7050;
        if (type == ASTNode.METHOD_DECLARATION) return 7060;
        if (type == ASTNode.SINGLE_VARIABLE_DECLARATION) return 8010;
        if (type == ASTNode.VARIABLE_DECLARATION_FRAGMENT) return 8020;
        if (type == ASTNode.NAME_QUALIFIED_TYPE) return 9011;
        if (type == ASTNode.QUALIFIED_TYPE) return 9012;
        if (type == ASTNode.PRIMITIVE_TYPE) return 9013;
        if (type == ASTNode.WILDCARD_TYPE) return 9014;
        if (type == ASTNode.SIMPLE_TYPE) return 9015;
        if (type == ASTNode.UNION_TYPE) return 9020;
        if (type == ASTNode.INTERSECTION_TYPE) return 9030;
        if (type == ASTNode.ARRAY_TYPE) return 9040;
        if (type == ASTNode.PARAMETERIZED_TYPE) return 9050;
        if (type == ASTNode.ANONYMOUS_CLASS_DECLARATION) return 10000;
        if (type == ASTNode.CONSTRUCTOR_INVOCATION) return 11010;
        if (type == ASTNode.CONTINUE_STATEMENT) return 11020;
        if (type == ASTNode.EXPRESSION_STATEMENT) return 11030;
        if (type == ASTNode.LABELED_STATEMENT) return 11040;
        if (type == ASTNode.BLOCK) return 11050;
        if (type == ASTNode.FOR_STATEMENT) return 11060;
        if (type == ASTNode.SYNCHRONIZED_STATEMENT) return 11070;
        if (type == ASTNode.TRY_STATEMENT) return 11080;
        if (type == ASTNode.DO_STATEMENT) return 11090;
        if (type == ASTNode.ASSERT_STATEMENT) return 11100;
        if (type == ASTNode.SWITCH_STATEMENT) return 11110;
        if (type == ASTNode.THROW_STATEMENT) return 11120;
        if (type == ASTNode.IF_STATEMENT) return 11130;
        if (type == ASTNode.TYPE_DECLARATION_STATEMENT) return 11140;
        if (type == ASTNode.ENHANCED_FOR_STATEMENT) return 11150;
        if (type == ASTNode.WHILE_STATEMENT) return 11160;
        if (type == ASTNode.EMPTY_STATEMENT) return 11170;
        if (type == ASTNode.RETURN_STATEMENT) return 11180;
        if (type == ASTNode.SWITCH_CASE) return 11190;
        if (type == ASTNode.VARIABLE_DECLARATION_STATEMENT) return 11200;
        if (type == ASTNode.SUPER_CONSTRUCTOR_INVOCATION) return 11210;
        if (type == ASTNode.BREAK_STATEMENT) return 11220;
        if (type == ASTNode.TAG_ELEMENT) return 12000;
        if (type == ASTNode.FIELD_ACCESS) return 13010;
        if (type == ASTNode.POSTFIX_EXPRESSION) return 13020;
        if (type == ASTNode.CHARACTER_LITERAL) return 13030;
        if (type == ASTNode.BOOLEAN_LITERAL) return 13040;
        if (type == ASTNode.CONDITIONAL_EXPRESSION) return 13050;
        if (type == ASTNode.INSTANCEOF_EXPRESSION) return 13060;
        if (type == ASTNode.ARRAY_INITIALIZER) return 13070;
        if (type == ASTNode.CREATION_REFERENCE) return 13081;
        if (type == ASTNode.TYPE_METHOD_REFERENCE) return 13082;
        if (type == ASTNode.SUPER_METHOD_REFERENCE) return 13083;
        if (type == ASTNode.EXPRESSION_METHOD_REFERENCE) return 13084;
        if (type == ASTNode.THIS_EXPRESSION) return 13090;
        if (type == ASTNode.ASSIGNMENT) return 13100;
        if (type == ASTNode.SUPER_FIELD_ACCESS) return 13110;
        if (type == ASTNode.ARRAY_CREATION) return 13120;
        if (type == ASTNode.MARKER_ANNOTATION) return 13131;
        if (type == ASTNode.NORMAL_ANNOTATION) return 13132;
        if (type == ASTNode.SINGLE_MEMBER_ANNOTATION) return 13133;
        if (type == ASTNode.TYPE_LITERAL) return 13140;
        if (type == ASTNode.METHOD_INVOCATION) return 13150;
        if (type == ASTNode.QUALIFIED_NAME) return 13161;
        if (type == ASTNode.SIMPLE_NAME) return 13162;
        if (type == ASTNode.NUMBER_LITERAL) return 13170;
        if (type == ASTNode.PARENTHESIZED_EXPRESSION) return 13180;
        if (type == ASTNode.STRING_LITERAL) return 13190;
        if (type == ASTNode.INFIX_EXPRESSION) return 13200;
        if (type == ASTNode.NULL_LITERAL) return 13210;
        if (type == ASTNode.SUPER_METHOD_INVOCATION) return 13220;
        if (type == ASTNode.CAST_EXPRESSION) return 13230;
        if (type == ASTNode.VARIABLE_DECLARATION_EXPRESSION) return 13240;
        if (type == ASTNode.ARRAY_ACCESS) return 13250;
        if (type == ASTNode.CLASS_INSTANCE_CREATION) return 13260;
        if (type == ASTNode.LAMBDA_EXPRESSION) return 13270;
        if (type == ASTNode.PREFIX_EXPRESSION) return 13280;
        if (type == ASTNode.IMPORT_DECLARATION) return 14000;
        if (type == ASTNode.MEMBER_REF) return 15000;
        if (type == ASTNode.OPENS_DIRECTIVE) return 16011;
        if (type == ASTNode.EXPORTS_DIRECTIVE) return 16012;
        if (type == ASTNode.USES_DIRECTIVE) return 16020;
        if (type == ASTNode.PROVIDES_DIRECTIVE) return 16030;
        if (type == ASTNode.REQUIRES_DIRECTIVE) return 16040;
        if (type == ASTNode.MODULE_MODIFIER) return 16050;
        if (type == ASTNode.METHOD_REF) return 16060;
        if (type == ASTNode.TEXT_ELEMENT) return 16070;
        if (type == ASTNode.LINE_COMMENT) return 17010;
        if (type == ASTNode.JAVADOC) return 17020;
        if (type == ASTNode.BLOCK_COMMENT) return 17030;
        if (type == ASTNode.CATCH_CLAUSE) return 18000;
        if (type == ASTNode.MODULE_DECLARATION) return 19000;
        if (type == ASTNode.TYPE_PARAMETER) return 20000;
        return 0;
    }

    public static int getPosHash(ITree node, Action action) {
        if (node == null) return 0;
        return action.getNode().getDepth() - node.getDepth();
    }

    public static int getValueHash(Action action) {
        return action.toString().hashCode();
    }

}
