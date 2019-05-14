package analyzer.histories;

import fileDiff.method.MethodDiff;
import analyzer.histories.variation.Mutant;
import analyzer.histories.variation.MutantType;
import analyzer.histories.variation.Variation;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Matcher {
    public static List<Pair<Variation, Comment>> match(List<Variation> variations, List<Comment> comments) {
        List<Pair<Variation, Comment>> result = new ArrayList<>();
        comments.removeIf(item -> isNoise(item));

        for (Variation variation: variations) {
            double sim = 0, max = 0;
            int maxIndex = -1;

            for (int i = 0; i < comments.size(); i ++) {
                Comment comment = comments.get(i);
                sim = getSimilarity(variation, comment);
                if (sim > max) {
                    max = sim;
                    maxIndex = i;
                }
            }

            if (max > 0) {
                result.add(new Pair<>(variation, comments.get(maxIndex)));
            }
        }

        return result;
    }

    public static List<Pair<MethodDiff, Comment>> matchMethodDiff(List<MethodDiff> methodDiffs, List<Comment> comments) {
        List<Pair<MethodDiff, Comment>> result = new ArrayList<>();

        if (methodDiffs == null || comments == null || methodDiffs.size() == 0 || comments.size() == 0)
            return result;
        methodDiffs.stream().forEach(methodDiff -> {

            result.addAll(matchMethodDiff(methodDiff, comments) );
        });
        return result;
    }

    public static List<Pair<MethodDiff, Comment>> matchMethodDiff(MethodDiff methodDiff, List<Comment> comments) {

        int keyWordMax = 0, tokenMax = 0, size = comments.size();
        Set<Integer> resultList = new HashSet<>();
        for (int i = 0; i < size; i ++) {
            int keyWordCount = methodDiff.commonKeyWords(comments.get(i));
            int tokenCount = methodDiff.commentWords(comments.get(i));

            if (keyWordCount > keyWordMax ||
                    (keyWordCount == keyWordMax && tokenCount > tokenMax)) {
                keyWordMax = keyWordCount;
                tokenMax = tokenCount;
                resultList.clear();
                resultList.add(i);
            } else if (keyWordCount == keyWordMax && tokenCount == tokenMax) {
                resultList.add(i);
            }
        }

        List<Pair<MethodDiff, Comment>> ans = new ArrayList<>();
        for (int result: resultList) {
            ans.add(new Pair<MethodDiff, Comment>(methodDiff, comments.get(result)));
        }
        return ans;
    }

    public static List<Pair<Variation, Comment>> match(List<Variation> variations, Issue issue) {
        List<Pair<Variation, Comment>> result = new ArrayList<>();
        return match(variations, issue.comments);
    }

    private double compare(Variation variation, Comment comment) {
        double sim = 0.0;
        if (isNoise(comment)) return 0.0;
        if (!timeIsValid(variation, comment)) return 0.0;

        return getSimilarity(variation, comment);
    }

    private static double  getSimilarity(Variation variation, Comment comment) {
        double result = 0;
        double count = 0;
        String commentContent = comment.content;
        for (Mutant mutant: variation.mutants) {
            if (mutant.type == MutantType.UPDATE) {
                int temp = 0;
                if (maxSubstring(mutant.before, commentContent).length() > 3) temp ++;
                if (maxSubstring(mutant.after, commentContent).length() > 3) temp ++;
                if (temp == 2) return 1.0; //
                else if (temp == 1) count ++;
            } else if (mutant.type == MutantType.INSERT) {
                if (commentContent.contains(mutant.after)) count ++;
            } else if (mutant.type == MutantType.DELETE) {
                if (commentContent.contains(mutant.before)) count ++ ;
            } else if (mutant.type == MutantType.METHODNAME || mutant.type == MutantType.CLASSNAME) {
                if (commentContent.contains(mutant.after)) count ++ ;
            }
        }
        return count / variation.mutants.size();
    }

    private static boolean isNoise(Comment comment) {
        Pattern pattern = Pattern.compile("Commit [0-9a-fA-F]{30,}");
        if (comment.content == null) {
            int a = 1;
        }
        java.util.regex.Matcher matcher = pattern.matcher(comment.content);
        if (matcher.find() && matcher.start() == 0) {
            return true;
        }

        return false;
    }

    /**
     * 判断variation与comment的时间是否有可能作为潜在的匹配对象
     * 如果comment的时间，在variation对应的commit之后，则肯定不存在匹配关系
     * @param variation commit中某个函数的变动
     * @param comment issue中的某条评论
     * @return 如果variation的提交时间再comment的时间之后，则返回true
     *          否则，返回false
     */
    private static boolean timeIsValid(Variation variation, Comment comment) {
        if ( EventDate.compare(variation.date, comment.date) >= 0 ) return true;
        else return false;
    }

    private static String maxSubstring(String strOne, String strTwo){
        // 参数检查
        if(strOne==null || strTwo == null){
            return "";
        }
        if(strOne.equals("") || strTwo.equals("")){
            return "";
        }
        // 二者中较长的字符串
        String max = "";
        // 二者中较短的字符串
        String min = "";
        if(strOne.length() < strTwo.length()){
            max = strTwo;
            min = strOne;
        } else{
            max = strTwo;
            min = strOne;
        }
        String current = "";
        // 遍历较短的字符串，并依次减少短字符串的字符数量，判断长字符是否包含该子串
        for(int i=0; i<min.length(); i++){
            for(int begin=0, end=min.length()-i; end<=min.length(); begin++, end++){
                current = min.substring(begin, end);
                if(max.contains(current)){
                    return current;
                }
            }
        }
        return "";
    }
}
