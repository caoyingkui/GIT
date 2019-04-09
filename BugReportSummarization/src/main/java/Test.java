import cn.edu.pku.sei.structureAlignment.util.DoubleValue;
import cn.edu.pku.sei.structureAlignment.util.Matrix;
import cn.edu.pku.sei.summarization.Summarization;
import git.ClassParser;
import git.GitAnalyzer;
import git.Method;
import javafx.util.Pair;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Test {
    GitAnalyzer git = new GitAnalyzer("C:\\Users\\oliver\\Desktop\\eclipse.jdt.debug");

    public void getTestItems() {
        Map<String, Item> bugs = getBugReport();
        getAnnotation(bugs);
        getRelatedCode(bugs.keySet().stream().map(bug -> bugs.get(bug)).collect(Collectors.toList()));
        bugs.keySet().stream().filter(bugId -> bugs.get(bugId).codeSnippets.size() > 0).forEach(bugId -> {
            System.out.println("***");
            Item item = bugs.get(bugId);
            List<String> sentences = new ArrayList<>();
            List<Double> sims = new ArrayList<>();
            Map<Integer, String> index_id = new HashMap<>();
            int count = 0;
            for (String id: item.sentences.keySet()) {
                sentences.add(item.sentences.get(id));
                index_id.put(count ++, id);
                sims.add(0.0);
            }
            Summarization summarization = new Summarization();
            summarization.setText(sentences);

            int cn = 0;
            for (String codeSnippet: item.codeSnippets) {
                System.out.println("in");
                summarization.setCode(codeSnippet);
                if (summarization.codeTrees.size() < 5 || summarization.codeTrees.size() != 14) continue;
                cn ++;
                Matrix<DoubleValue> matrix = summarization.getMatrix();
                for (int n = 0; n < matrix.getN(); n++) {
                    for (int m = 0; m < matrix.getM(); m++) {
                        if (sims.get(n) < matrix.getValue(m, n)) sims.set(n, matrix.getValue(m, n));
                    }
                }
                if (cn > 0)
                    break;
            }

            /*
            item.codeSnippets.forEach(codeSnippet -> {
                System.out.println("in");
                if (a == 10) return ;
                summarization.setCode(codeSnippet);
                if (summarization.codeTrees.size() < 5) return;
                Matrix<DoubleValue> matrix= summarization.getMatrix();
                for (int n = 0; n < matrix.getN(); n ++) {
                    for (int m = 0; m < matrix.getM(); m ++) {
                        if (sims.get(n) < matrix.getValue(m, n)) sims.set(n, matrix.getValue(m, n));
                    }
                }
            });*/

            Set<String> targets = new HashSet<>();
            targets.addAll(item.sentences.keySet());
            targets.removeIf(target -> {
                int c = 0;
                if (item.targets.get(1).contains(target)) c++;
                if (item.targets.get(1).contains(target)) c++;
                if (item.targets.get(1).contains(target)) c++;

                return c < 2;
            });

            List<Integer> ranking = sort(sims);
            double correct = 0;
            for (int i = 0; i < ranking.size() / 3 ; i ++) {
                if (targets.contains(index_id.get(ranking.get(i))))
                    correct ++;
            }
            System.out.println(correct / (ranking.size() / 3));
        });
    }


    public Map<String, String> extractSentences (Element turn) {
        Map<String, String> result = new HashMap<>();
        Iterator sentences = turn.elementIterator();
        while (sentences.hasNext()) {
            Element sentence = (Element)sentences.next();
            result.put(sentence.attribute("ID").getText(),
                    sentence.getText());
        }
        return result;
    }

    public Map<String, Item> getBugReport() {
        Map<String, Item> result = new HashMap<>();
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(new File("BugReportSummarization/data/bugreports.xml"));
            Element root = document.getRootElement();
            Iterator it = root.elementIterator();
            while (it.hasNext()) {
                Item item = new Item();
                Element bugReport = (Element) it.next();
                String bugId = bugReport.attribute("ID").getText();
                item.setTitle(bugReport.element("Title").getText());

                if (!item.title.contains("eclipse"))
                    continue;
                System.out.println(item.title);
                Iterator subIt = bugReport.elementIterator();
                while (subIt.hasNext()) {
                    Element element = (Element) subIt.next();
                    if (element.getName().equals("Turn")) {
                        Element text = element.element("Text");
                        Map<String, String> sentences = extractSentences(text);
                        item.sentences.putAll(sentences);
                    }
                }
                result.put(bugId, item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void getAnnotation(Map<String, Item> bugReports) {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File("BugReportSummarization/data/annotation.xml"));
            Iterator it = document.getRootElement().elementIterator();
            while(it.hasNext()) {
                Element annotation = (Element)it.next();
                String bugReportId = annotation.attribute("ID").getText();
                if (!bugReports.containsKey(bugReportId)) continue;

                Item item = bugReports.get(bugReportId);
                Iterator subIt = annotation.elementIterator();
                while (subIt.hasNext()) {
                    Element element = (Element) subIt.next();
                    if (element.getName().equals("Annotation")) {
                        Element summary = element.element("ExtractiveSummary");
                        List<String> sentences = new ArrayList<>();
                        Iterator senIt = summary.elementIterator();
                        while (senIt.hasNext()) {
                            Element sentence = (Element) senIt.next();
                            sentences.add(sentence.attribute("ID").getText());
                        }
                        item.targets.add(sentences);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getRelatedCode(List<Item> bugReports) {
        bugReports.stream().forEach(item -> {
            if (item.issueId == null) return;
            String issue = item.issueId;
            if (item.title.contains("182965")) {
                git = new GitAnalyzer("C:\\Users\\oliver\\Desktop\\org.eclipse.mylyn.tasks");
            } else if (item.title.contains("199846") ||
                        item.title.contains("207003") ||
                    item.title.contains("219570") ||
                    item.title.contains("224588") ) {
                git = new GitAnalyzer("C:\\Users\\oliver\\Desktop\\eclipse.platform.team");
            } else if (item.title.contains("241557") || item.title.contains("279721")) {
                git = new GitAnalyzer("C:\\Users\\oliver\\Desktop\\org.eclipse.datatools");
            } else if (item.title.contains("306736")) {
                git = new GitAnalyzer("C:\\Users\\oliver\\Desktop\\eclipse.platform.ui");
            } else if (item.title.contains("346116")) {
                git = new GitAnalyzer("C:\\Users\\oliver\\Desktop\\eclipse.jdt.debug");
            } else if (item.title.contains("405506")) {
                git = new GitAnalyzer("C:\\Users\\oliver\\Desktop\\rt.equinox.bundles");
            }

            git.getCommits().stream().

                filter(revCommit -> {
                    String msg = revCommit.getShortMessage();
                    return msg.contains(issue);
                }).map(revCommit -> {
                    List<Pair<String, String>> fileContents = new ArrayList<>();
                    String commitId = revCommit.getName();
                    List<String> files = git.getAllFilesModifiedByCommit(commitId);
                    for (String file: files) {
                        Pair<String, String> content = new Pair<>(
                                git.getFileFromCommit(git.getId(commitId + "^"), file),
                                git.getFileFromCommit(git.getId(commitId), file)
                        );
                        fileContents.add(content);
                    }
                    return fileContents;
                }).flatMap(list -> {
                    return list.stream();
                }).flatMap(pair -> {
                    String oldContent = pair.getKey();
                    String newContent = pair.getValue();
                    ClassParser newParser = new ClassParser().setSourceCode(newContent);
                    Map<String, Method> methodMap = newParser.getChangedMethod(git.getEditList(oldContent, newContent), true);
                    return methodMap.keySet().stream().map(methodName -> methodMap.get(methodName));
                }).forEach(method -> {
                    String methodContent = method.methodContent;
                    ASTParser parser = ASTParser.newParser(8);
                    parser.setSource(methodContent.toCharArray());
                    parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
                    TypeDeclaration declaration = (TypeDeclaration)parser.createAST(null);
                    MethodDeclaration methodDeclaration =  (MethodDeclaration) declaration.bodyDeclarations().get(0);
                    StringBuilder methodBody = new StringBuilder();
                    methodDeclaration.getBody().statements().forEach(statement -> methodBody.append(statement.toString()));
                    item.codeSnippets.add(methodBody.toString());
                });
        });
    }

    public List<Integer> sort(List<Double> sims) {
        List<Pair<Integer, Double>> pairs = new ArrayList<>();
        for (int i = 0; i < sims.size(); i++) {
            pairs.add(new Pair(i, sims.get(i)));
        }

        Collections.sort(pairs, (pair1, pair2) -> {
            //System.out.println(pair1.getValue() + " " + pair2.getValue());
            return -1 * pair1.getValue().compareTo(pair2.getValue());
        });

        return pairs.stream().map(pair -> pair.getKey()).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.getTestItems();
    }

}
