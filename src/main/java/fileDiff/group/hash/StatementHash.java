package fileDiff.group.hash;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.*;
import fileDiff.Change;
import fileDiff.group.hash.insert.*;
import fileDiff.group.hash.move.MMove;
import fileDiff.group.hash.update.UUpdate;
import fileDiff.method.MethodDiff;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;
import util.ReaderTool;
import util.WriterTool;

import java.io.*;
import java.util.*;

/**
 * Created by kvirus on 2019/6/15 22:02
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public abstract class StatementHash implements Serializable {

    public static ASTParser parser;
    
    public static Map<Integer, Integer> codes;

    public String content;
    
    static {
        parser = ASTParser.newParser(9);
        parser.setKind(ASTParser.K_STATEMENTS);
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
        parser.setCompilerOptions(options);

        encode();
    }

    public static boolean strict = false  ;
    public int[] hashes;

    public static final int INSERT  = 2;
    public static final int DELETE  = 4;
    public static final int UPDATE  = 6;
    public static final int MOVE    = 8;

    public int typeHash(SourceCodeChange change) {
        if (change instanceof Insert)       return INSERT;
        else if (change instanceof Delete)  return DELETE;
        else if (change instanceof Update)  return UPDATE;
        else if (change instanceof Move)    return MOVE;

        assert 1 == 2;
        return 0;
    }

    private static void encode() {
        codes = new HashMap<>();
        codes.put( ASTNode.PACKAGE_DECLARATION, 100);
        codes.put(ASTNode.DIMENSION, 200);
        codes.put(ASTNode.METHOD_REF_PARAMETER, 300);
        codes.put(ASTNode.COMPILATION_UNIT, 400);
        codes.put(ASTNode.MODIFIER, 500);
        codes.put(ASTNode.MEMBER_VALUE_PAIR, 600);
        codes.put(ASTNode.INITIALIZER, 7010);
        codes.put(ASTNode.ENUM_DECLARATION, 7021);
        codes.put(ASTNode.TYPE_DECLARATION, 7022);
        codes.put(ASTNode.ANNOTATION_TYPE_DECLARATION, 7023);
        codes.put(ASTNode.FIELD_DECLARATION, 7030);
        codes.put(ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION, 7040);
        codes.put(ASTNode.ENUM_CONSTANT_DECLARATION, 7050);
        codes.put(ASTNode.METHOD_DECLARATION, 7060);
        codes.put(ASTNode.SINGLE_VARIABLE_DECLARATION, 8010);
        codes.put(ASTNode.VARIABLE_DECLARATION_FRAGMENT, 8020);
        codes.put(ASTNode.NAME_QUALIFIED_TYPE, 9011);
        codes.put(ASTNode.QUALIFIED_TYPE, 9012);
        codes.put(ASTNode.PRIMITIVE_TYPE, 9013);
        codes.put(ASTNode.WILDCARD_TYPE, 9014);
        codes.put(ASTNode.SIMPLE_TYPE, 9015);
        codes.put(ASTNode.UNION_TYPE, 9020);
        codes.put(ASTNode.INTERSECTION_TYPE, 9030);
        codes.put(ASTNode.ARRAY_TYPE, 9040);
        codes.put(ASTNode.PARAMETERIZED_TYPE, 9050);
        codes.put(ASTNode.ANONYMOUS_CLASS_DECLARATION, 10000);
        codes.put(ASTNode.CONSTRUCTOR_INVOCATION, 11010);
        codes.put(ASTNode.CONTINUE_STATEMENT, 11020);
        codes.put(ASTNode.EXPRESSION_STATEMENT, 11030);
        codes.put(ASTNode.LABELED_STATEMENT, 11040);
        codes.put(ASTNode.BLOCK, 11050);
        codes.put(ASTNode.FOR_STATEMENT, 11060);
        codes.put(ASTNode.SYNCHRONIZED_STATEMENT, 11070);
        codes.put(ASTNode.TRY_STATEMENT, 11080);
        codes.put(ASTNode.DO_STATEMENT, 11090);
        codes.put(ASTNode.ASSERT_STATEMENT, 11100);
        codes.put(ASTNode.SWITCH_STATEMENT, 11110);
        codes.put(ASTNode.THROW_STATEMENT, 11120);
        codes.put(ASTNode.IF_STATEMENT, 11130);
        codes.put(ASTNode.TYPE_DECLARATION_STATEMENT, 11140);
        codes.put(ASTNode.ENHANCED_FOR_STATEMENT, 11150);
        codes.put(ASTNode.WHILE_STATEMENT, 11160);
        codes.put(ASTNode.EMPTY_STATEMENT, 11170);
        codes.put(ASTNode.RETURN_STATEMENT, 11180);
        codes.put(ASTNode.SWITCH_CASE, 11190);
        codes.put(ASTNode.VARIABLE_DECLARATION_STATEMENT, 11200);
        codes.put(ASTNode.SUPER_CONSTRUCTOR_INVOCATION, 11210);
        codes.put(ASTNode.BREAK_STATEMENT, 11220);
        codes.put(ASTNode.TAG_ELEMENT, 12000);
        codes.put(ASTNode.FIELD_ACCESS, 13010);
        codes.put(ASTNode.POSTFIX_EXPRESSION, 13020);
        codes.put(ASTNode.CHARACTER_LITERAL, 13030);
        codes.put(ASTNode.BOOLEAN_LITERAL, 13040);
        codes.put(ASTNode.CONDITIONAL_EXPRESSION, 13050);
        codes.put(ASTNode.INSTANCEOF_EXPRESSION, 13060);
        codes.put(ASTNode.ARRAY_INITIALIZER, 13070);
        codes.put(ASTNode.CREATION_REFERENCE, 13081);
        codes.put(ASTNode.TYPE_METHOD_REFERENCE, 13082);
        codes.put(ASTNode.SUPER_METHOD_REFERENCE, 13083);
        codes.put(ASTNode.EXPRESSION_METHOD_REFERENCE, 13084);
        codes.put(ASTNode.THIS_EXPRESSION, 13090);
        codes.put(ASTNode.ASSIGNMENT, 13100);
        codes.put(ASTNode.SUPER_FIELD_ACCESS, 13110);
        codes.put(ASTNode.ARRAY_CREATION, 13120);
        codes.put(ASTNode.MARKER_ANNOTATION, 13131);
        codes.put(ASTNode.NORMAL_ANNOTATION, 13132);
        codes.put(ASTNode.SINGLE_MEMBER_ANNOTATION, 13133);
        codes.put(ASTNode.TYPE_LITERAL, 13140);
        codes.put(ASTNode.METHOD_INVOCATION, 13150);
        codes.put(ASTNode.QUALIFIED_NAME, 13161);
        codes.put(ASTNode.SIMPLE_NAME, 13162);
        codes.put(ASTNode.NUMBER_LITERAL, 13170);
        codes.put(ASTNode.PARENTHESIZED_EXPRESSION, 13180);
        codes.put(ASTNode.STRING_LITERAL, 13190);
        codes.put(ASTNode.INFIX_EXPRESSION, 13200);
        codes.put(ASTNode.NULL_LITERAL, 13210);
        codes.put(ASTNode.SUPER_METHOD_INVOCATION, 13220);
        codes.put(ASTNode.CAST_EXPRESSION, 13230);
        codes.put(ASTNode.VARIABLE_DECLARATION_EXPRESSION, 13240);
        codes.put(ASTNode.ARRAY_ACCESS, 13250);
        codes.put(ASTNode.CLASS_INSTANCE_CREATION, 13260);
        codes.put(ASTNode.LAMBDA_EXPRESSION, 13270);
        codes.put(ASTNode.PREFIX_EXPRESSION, 13280);
        codes.put(ASTNode.IMPORT_DECLARATION, 14000);
        codes.put(ASTNode.MEMBER_REF, 15000);
        codes.put(ASTNode.OPENS_DIRECTIVE, 16011);
        codes.put(ASTNode.EXPORTS_DIRECTIVE, 16012);
        codes.put(ASTNode.USES_DIRECTIVE, 16020);
        codes.put(ASTNode.PROVIDES_DIRECTIVE, 16030);
        codes.put(ASTNode.REQUIRES_DIRECTIVE, 16040);
        codes.put(ASTNode.MODULE_MODIFIER, 16050);
        codes.put(ASTNode.METHOD_REF, 16060);
        codes.put(ASTNode.TEXT_ELEMENT, 16070);
        codes.put(ASTNode.LINE_COMMENT, 17010);
        codes.put(ASTNode.JAVADOC, 17020);
        codes.put(ASTNode.BLOCK_COMMENT, 17030);
        codes.put(ASTNode.CATCH_CLAUSE, 18000);
        codes.put(ASTNode.MODULE_DECLARATION, 19000);
        codes.put(ASTNode.TYPE_PARAMETER, 20000);
    }
    
    public boolean equals(StatementHash hash) {
        if (this instanceof IDefault || hash instanceof IDefault) return false;
        try {
            return hashes[0] == hash.hashes[0] && hashes[1] == hash.hashes[1];
        } catch (Exception e) {
            return false;
        }
    }

    public static int getCode(ASTNode node) {
        return node == null ? 0 : codes.getOrDefault(node.getNodeType(), 0);
    }

    public static int getCode(int type) {
        return codes.getOrDefault(type, 0);
    }

    public static int blockStatementHash(EntityType type){
        if (type == JavaEntityType.CATCH_CLAUSE)                return getCode(ASTNode.CATCH_CLAUSE);
        else if (type == JavaEntityType.DO_STATEMENT)           return getCode(ASTNode.DO_STATEMENT);
        else if (type == JavaEntityType.FINALLY)                return 0; // 有问题
        else if (type == JavaEntityType.FOR_STATEMENT)          return getCode(ASTNode.FOR_STATEMENT);
        else if (type == JavaEntityType.IF_STATEMENT)           return getCode(ASTNode.IF_STATEMENT);
        else if (type == JavaEntityType.SWITCH_STATEMENT)       return getCode(ASTNode.SWITCH_STATEMENT);
        else if (type == JavaEntityType.SWITCH_CASE)            return getCode(ASTNode.SWITCH_CASE);
        else if (type == JavaEntityType.SYNCHRONIZED_STATEMENT) return getCode(ASTNode.SYNCHRONIZED_STATEMENT);
        else if (type == JavaEntityType.TRY_STATEMENT)          return getCode(ASTNode.TRY_STATEMENT);
        else if (type == JavaEntityType.WHILE_STATEMENT)        return getCode(ASTNode.WHILE_STATEMENT);
        else if (type == JavaEntityType.FOREACH_STATEMENT)      return getCode(ASTNode.ENHANCED_FOR_STATEMENT);
        else if (type == JavaEntityType.METHOD)                 return getCode(ASTNode.METHOD_DECLARATION);

        assert 1 == 2;
        return 0;
    }

    public static int statementHash(EntityType type) {
        if (type == JavaEntityType.ASSERT_STATEMENT)            return getCode(ASTNode.ASSIGNMENT);
        if (type == JavaEntityType.ASSIGNMENT)                  return getCode(ASTNode.ASSIGNMENT);
        if (type == JavaEntityType.BREAK_STATEMENT)             return getCode(ASTNode.BREAK_STATEMENT);
        if (type == JavaEntityType.CATCH_CLAUSE)                return getCode(ASTNode.CATCH_CLAUSE);
        if (type == JavaEntityType.CLASS_INSTANCE_CREATION)     return getCode(ASTNode.CLASS_INSTANCE_CREATION);
        if (type == JavaEntityType.CONSTRUCTOR_INVOCATION)      return getCode(ASTNode.CONSTRUCTOR_INVOCATION);
        if (type == JavaEntityType.CONTINUE_STATEMENT)          return getCode(ASTNode.CONTINUE_STATEMENT);
        if (type == JavaEntityType.DO_STATEMENT)                return getCode(ASTNode.DO_STATEMENT);
        if (type == JavaEntityType.FINALLY)                     return 0;
        if (type == JavaEntityType.FOR_STATEMENT)               return getCode(ASTNode.FOR_STATEMENT);
        if (type == JavaEntityType.IF_STATEMENT)                return getCode(ASTNode.IF_STATEMENT);
        if (type == JavaEntityType.LABELED_STATEMENT)           return getCode(ASTNode.LABELED_STATEMENT);
        if (type == JavaEntityType.METHOD_INVOCATION)           return getCode(ASTNode.METHOD_INVOCATION);
        if (type == JavaEntityType.RETURN_STATEMENT)            return getCode(ASTNode.RETURN_STATEMENT);
        if (type == JavaEntityType.SWITCH_CASE)                 return getCode(ASTNode.SWITCH_CASE);
        if (type == JavaEntityType.SWITCH_STATEMENT)            return getCode(ASTNode.SWITCH_STATEMENT);
        if (type == JavaEntityType.SYNCHRONIZED_STATEMENT)      return getCode(ASTNode.SYNCHRONIZED_STATEMENT);
        if (type == JavaEntityType.THROW_STATEMENT)             return getCode(ASTNode.THROW_STATEMENT);
        if (type == JavaEntityType.TRY_STATEMENT)               return getCode(ASTNode.TRY_STATEMENT);
        if (type == JavaEntityType.VARIABLE_DECLARATION_STATEMENT) return getCode(ASTNode.VARIABLE_DECLARATION_STATEMENT);
        if (type == JavaEntityType.WHILE_STATEMENT)             return getCode(ASTNode.WHILE_STATEMENT);
        if (type == JavaEntityType.FOREACH_STATEMENT)           return getCode(ASTNode.ENHANCED_FOR_STATEMENT);
        if (type == JavaEntityType.PREFIX_EXPRESSION)           return getCode(ASTNode.PREFIX_EXPRESSION);
        if (type == JavaEntityType.POSTFIX_EXPRESSION)          return getCode(ASTNode.POSTFIX_EXPRESSION);

        assert 1 == 2;
        return 0;
    }

    public static Block getBlock(SourceCodeChange change) {
        String statement = change.getChangedEntity().getUniqueName();
        parser.setKind(ASTParser.K_STATEMENTS);
        parser.setSource(statement.toCharArray());
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
        parser.setCompilerOptions(options);
        Block block = (Block)parser.createAST(null);
        return block;
    }

    public static StatementHash getInstance(SourceCodeChange change, MethodDeclaration newDeclaration, MethodDeclaration oldDeclaration) {
        if (change instanceof Insert || change instanceof Delete || change instanceof Move) {
            EntityType type = change.getChangedEntity().getType();
            if (type == JavaEntityType.ASSERT_STATEMENT)        return new IAssignment(change);
            if (type == JavaEntityType.ASSIGNMENT)              return new IAssignment(change);
            if (type == JavaEntityType.BREAK_STATEMENT)         return new IBreak(change);
            if (type == JavaEntityType.CATCH_CLAUSE)            return new ICatch(change);
            if (type == JavaEntityType.CLASS_INSTANCE_CREATION) return new IClassInstanceCreation(change);
            if (type == JavaEntityType.CONTINUE_STATEMENT)      return new IContinue(change);
            if (type == JavaEntityType.VARIABLE_DECLARATION_STATEMENT) return new IDeclaration(change);
            if (type == JavaEntityType.DO_STATEMENT)            return new IDo(change);
            if (type == JavaEntityType.FOR_STATEMENT)           return new IFor(change);
            if (type == JavaEntityType.FOREACH_STATEMENT)       return new IForeach(change);
            if (type == JavaEntityType.IF_STATEMENT ||
                    type == JavaEntityType.ELSE_STATEMENT)      return new IIf(change);
            if (type == JavaEntityType.LABELED_STATEMENT)       return new ILabelStatement(change);
            if (type == JavaEntityType.METHOD_INVOCATION  ||
                    type == JavaEntityType.CONSTRUCTOR_INVOCATION)       return new IMethodInvocation(change);
            if (type == JavaEntityType.PARAMETER)               return new IParameter(change);
            if (type == JavaEntityType.POSTFIX_EXPRESSION)      return new IPostfixExpression(change);
            if (type == JavaEntityType.PREFIX_EXPRESSION)       return new IPrefixExpression(change);
            if (type == JavaEntityType.RETURN_STATEMENT)        return new IReturn(change, newDeclaration, oldDeclaration);
            if (type == JavaEntityType.SWITCH_CASE)             return new ISwitchCase(change);
            if (type == JavaEntityType.SWITCH_STATEMENT)        return new ISwitchStatement(change);
            if (type == JavaEntityType.SYNCHRONIZED_STATEMENT)  return new ISynchronize(change);
            if (type == JavaEntityType.THROW_STATEMENT)         return new IThrow(change);
            if (type == JavaEntityType.TRY_STATEMENT)           return new ITry(change);
            if (type == JavaEntityType.WHILE_STATEMENT)         return new IWhile(change);
            if (type == JavaEntityType.PARAMETER)               return new IParameter(change);
        } else if (change instanceof Update) {
            return new UUpdate(change);
        } else if (change instanceof Move) {
            return new MMove(change);
        }

        return new IDefault(change);

    }

    public static void main1(String[] args) {


        String json = ReaderTool.read("C:\\Users\\oliver\\Desktop\\json1.json");
        JSONObject o = new JSONObject(json);
        int count = 0;
        Set<String> files = new HashSet<>();
        Set<String> commit = new HashSet<>();
        Set<String> id = new HashSet<>();
        JSONArray clusters = o.getJSONArray("results");
        for (int i = 0; i < clusters.length(); i++) {
            JSONObject cluster = clusters.getJSONObject(i);
            JSONArray members = cluster.getJSONObject("cluster").getJSONArray("members");
            count += members.length();
            for (int j = 0; j < members.length(); j++) {
                JSONObject m = members.getJSONObject(j);
                files.add(m.getString("fileName"));
                commit.add(m.getString("commitBeforeChange"));
                id.add(m.getString("id"));
            }

        }
        System.out.println(count);
        System.out.println(files.size());
        System.out.println(commit.size());
        System.out.println(id.size());
    }

    public static void main(String[] args) {
    }
}
