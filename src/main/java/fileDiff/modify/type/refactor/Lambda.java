package fileDiff.modify.type.refactor;

import fileDiff.Diff;
import fileDiff.file.FileDiff;
import fileDiff.method.MethodDiff;
import fileDiff.modify.type.Modify;
import fileDiff.modify.type.Order;
import git.GitAnalyzer;

/**
 * Created by kvirus on 2019/5/20 8:59
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.REFACTOR)
public class Lambda extends Modify {
    public static int count = 0;

    public static Lambda match(MethodDiff method) {
        // TODO

        return null;
    }

    @Override
    protected void build() {

    }

    @Override
    public String getContent() {
        return super.getContent();
    }

    @Override
    public void extend(String str) {
        super.extend(str);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    public static void test() {
        String[] ids = {
                "4410ef941acf02e752a599b5403091f86e66a9a2"
        };

        String[] fileNames = {
                "solr/solrj/src/java/org/apache/solr/client/solrj/request/json/JsonQueryRequest.java"
        };

        String[] methodNames = {
                "withFilter"
        };
        GitAnalyzer git = new GitAnalyzer();
        for (int i = 0; i < 1; i++) {
            Diff d = new Diff(git, git.getId(ids[i]));
            FileDiff c = null;
            String targetName = fileNames[i];
            targetName = targetName.substring(targetName.lastIndexOf("/") + 1, targetName.length() - 5);
            String methodName = methodNames[i];
            for (FileDiff file: d.getClasses()) {
                if (file.getName().equals(targetName)) {
                    c = file;
                    break;
                }
            }

            for (MethodDiff method: c.getMethods()) {
                if (method.getName().equals(methodName)) {
                    Lambda result = Lambda.match(method);
                }
            }
        }
    }

    public static void main(String[] args) {

    }
}
