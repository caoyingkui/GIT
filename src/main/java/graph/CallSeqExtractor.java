package graph;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.IOException;

public class CallSeqExtractor {

  static class CallSeqVisitor implements DirExplorer.FileHandler {
    @Override
    public void handle(int level, String path, File file) {

      try {
        new VoidVisitorAdapter<Object>() {
          @Override
          public void visit(ImportDeclaration n, Object arg) {
            super.visit(n, arg);
          }
          public void visit(MethodDeclaration n, Object arg) {
            super.visit(n, arg);
          }
          public void visit(MethodCallExpr n, Object arg) {
            super.visit(n, arg);
          }
        }.visit(JavaParser.parse(file), null);
        // System.out.println();
      } catch (ParseProblemException | IOException e) {
        System.out.println("Exception found in parsing " + path);
        new RuntimeException(e);
      }

    }
  }

  public static void extract(File projectDir) {
    new DirExplorer(((level, path, file) -> path.endsWith("java")), new CallSeqVisitor())
            .explore(projectDir);
    }

  public static void main(String[] args) {
    File projectDir = new File("data/test");
     extract(projectDir);
  }
}
