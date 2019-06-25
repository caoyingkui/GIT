package util;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.apache.commons.text.similarity.CosineSimilarity;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by kvirus on 2019/5/2 13:59
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class StemTool {
    private static Set<String> stopWordsList;
    static {
        //region stop words
        String[] words = {
                "",
                "!!" ,
                "?!" ,
                "??" ,
                "!?" ,
                "`" ,
                "``" ,
                "''" ,
                "-lrb-" ,
                "-rrb-" ,
                "-lsb-" ,
                "-rsb-" ,
                "," ,
                "." ,
                ":" ,
                ";" ,
                "\"" ,
                "'" ,
                "?" ,
                "<" ,
                ">" ,
                "{" ,
                "}" ,
                "[" ,
                "]" ,
                "+" ,
                "-" ,
                "(" ,
                ")" ,
                "&" ,
                "%" ,
                "$" ,
                "@" ,
                "!" ,
                "^" ,
                "#" ,
                "*" ,
                ".." ,
                "..." ,
                "'ll" ,
                "'s" ,
                "'m" ,
                "a" ,
                "about" ,
                "above" ,
                "after" ,
                "again" ,
                "against" ,
                "all" ,
                "am" ,
                "an" ,
                "and" ,
                "any" ,
                "are" ,
                "aren't" ,
                "as" ,
                "at" ,
                "be" ,
                "because" ,
                "been" ,
                "before" ,
                "being" ,
                "below" ,
                "between" ,
                "both" ,
                "but" ,
                "by" ,
                "can" ,
                "can't" ,
                "cannot" ,
                "could" ,
                "couldn't" ,
                "did" ,
                "didn't" ,
                "do" ,
                "does" ,
                "doesn't" ,
                "doing" ,
                "don't" ,
                "down" ,
                "during" ,
                "each" ,
                "few" ,
                "for" ,
                "from" ,
                "further" ,
                "had" ,
                "hadn't" ,
                "has" ,
                "hasn't" ,
                "have" ,
                "haven't" ,
                "having" ,
                "he" ,
                "he'd" ,
                "he'll" ,
                "he's" ,
                "her" ,
                "here" ,
                "here's" ,
                "hers" ,
                "herself" ,
                "him" ,
                "himself" ,
                "his" ,
                "how" ,
                "how's" ,
                "i" ,
                "i'd" ,
                "i'll" ,
                "i'm" ,
                "i've" ,
                "if" ,
                "in" ,
                "into" ,
                "is" ,
                "isn't" ,
                "it" ,
                "it's" ,
                "its" ,
                "itself" ,
                "let's" ,
                "me" ,
                "more" ,
                "most" ,
                "mustn't" ,
                "my" ,
                "myself" ,
                "no" ,
                "nor" ,
                "not" ,
                "of" ,
                "off" ,
                "on" ,
                "once" ,
                "only" ,
                "or" ,
                "other" ,
                "ought" ,
                "our" ,
                "ours" ,
                "ourselves" ,
                "out" ,
                "over" ,
                "own" ,
                "same" ,
                "shan't" ,
                "she" ,
                "she'd" ,
                "she'll" ,
                "she's" ,
                "should" ,
                "shouldn't" ,
                "so" ,
                "some" ,
                "such" ,
                "than" ,
                "that" ,
                "that's" ,
                "the" ,
                "their" ,
                "theirs" ,
                "them" ,
                "themselves" ,
                "then" ,
                "there" ,
                "there's" ,
                "these" ,
                "they" ,
                "they'd" ,
                "they'll" ,
                "they're" ,
                "they've" ,
                "this" ,
                "those" ,
                "through" ,
                "to" ,
                "too" ,
                "under" ,
                "until" ,
                "up" ,
                "very" ,
                "was" ,
                "wasn't" ,
                "we" ,
                "we'd" ,
                "we'll" ,
                "we're" ,
                "we've" ,
                "were" ,
                "weren't" ,
                "what" ,
                "what's" ,
                "when" ,
                "when's" ,
                "where" ,
                "where's" ,
                "which" ,
                "while" ,
                "who" ,
                "who's" ,
                "whom" ,
                "why" ,
                "why's" ,
                "with" ,
                "won't" ,
                "would" ,
                "wouldn't" ,
                "you" ,
                "you'd" ,
                "you'll" ,
                "you're" ,
                "you've" ,
                "your" ,
                "yours" ,
                "yourself" ,
                "yourselves" ,
                "###" ,
                "return" ,
                "arent" ,
                "cant" ,
                "couldnt" ,
                "didnt" ,
                "doesnt" ,
                "dont" ,
                "hadnt" ,
                "hasnt" ,
                "havent" ,
                "hes" ,
                "heres" ,
                "hows" ,
                "im" ,
                "isnt" ,
                "its" ,
                "lets" ,
                "mustnt" ,
                "shant" ,
                "shes" ,
                "shouldnt" ,
                "thats" ,
                "theres" ,
                "theyll" ,
                "theyre" ,
                "theyve" ,
                "wasnt" ,
                "were" ,
                "werent" ,
                "whats" ,
                "whens" ,
                "wheres" ,
                "whos" ,
                "whys" ,
                "wont" ,
                "wouldnt" ,
                "youd" ,
                "youll" ,
                "youre" ,
                "youve" ,
        };
        //endregion <stop words>
        stopWordsList = new HashSet<>();
        for (String word: words)
            stopWordsList.add(word);
    }

    public static String stemSingleWord(String word){
        String result = "";
        try{
            EnglishStemmer stemmer = new EnglishStemmer();
            stemmer.setCurrent(word);
            stemmer.stem();
            result = stemmer.getCurrent();
        }catch (Exception e){
            result = "";
            e.printStackTrace();
        }

        return result;
    }

    public static List<String> stem(String sentence){
        List<String> result = new ArrayList<String>();
        try {
            result = new ArrayList<String>();
            List<String> tokens = tokenize(sentence);

            EnglishStemmer stemmer = new EnglishStemmer();

            for(String token : tokens){
                stemmer.setCurrent(token);
                stemmer.stem();
                result.add(stemmer.getCurrent());
            }


        }catch(Exception e){
            result.clear();
            e.printStackTrace();
        }
        return result;
    }

    public static List<String> stem(List<String> words){
        try{
            List<String> result = new ArrayList<String>();
            EnglishStemmer stemmer = new EnglishStemmer();
            for(String word : words){
                stemmer.setCurrent(word.toLowerCase());
                stemmer.stem();
                result.add(stemmer.getCurrent());
            }

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将句子进行切词，去停用词
     * @param sentence
     * @return
     */
    public static List<String> tokenize(String sentence){
        try{
            String camelCasePattern = "([^\\p{L}\\d]+)|(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)|(?<=[\\p{L}&&[^\\p{Lu}]])(?=\\p{Lu})|(?<=\\p{Lu})(?=\\p{Lu}[\\p{L}&&[^\\p{Lu}]])";
            sentence = String.join(" " , sentence.split(camelCasePattern));
            List<String> result = new ArrayList<String>();

            Analyzer analyzer = new StopAnalyzer();

            TokenStream tokenStream = analyzer.tokenStream("" , new StringReader(sentence));
            CharTermAttribute cta = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while(tokenStream.incrementToken()){
                String term = cta.toString();
                result.add(term);
            }

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public static double compare(String str1 , String str2){
        String[] tokens1 = stem(tokenize(str1)).toArray(new String[0]);
        String[] tokens2 = stem(tokenize(str2)).toArray(new String[0]);

        CosineSimilarity similarity = new CosineSimilarity();

        Map<CharSequence , Integer> left = Arrays.stream(tokens1).collect(Collectors.toMap(c -> c , c -> 1 , Integer::sum));

        Map<CharSequence , Integer> right = Arrays.stream(tokens2).collect(Collectors.toMap(c -> c , c -> 1 , Integer::sum));

        return similarity.cosineSimilarity(left , right);
    }

    public static String filterHtmlTags(String text){
        StringBuilder result = new StringBuilder("");

        Pattern linkPattern = Pattern.compile("(<a[^>]*>([^<]*)</a>)|([a-zA-z]+://[^\\s]*)");
        Matcher matcher = linkPattern.matcher(text);

        int start = -1;
        int length = 0;
        while(matcher.find()){
            length = matcher.start() - start - 1;
            if(length > 0){
                result.append(text.substring(start + 1 , matcher.start()));
            }

            result.append(" " + matcher.group(2) + " ");
            start = matcher.end() - 1;
        }

        if(text.length() - start - 1 > 0){
            result.append(text.substring(start + 1 , text.length()));
        }

        return result.toString();

    }

    public static List<String> string2sentence(String string){
        List<String> result = new ArrayList<>();
        Reader reader = new StringReader(string);
        DocumentPreprocessor dp = new DocumentPreprocessor(reader);
        List<String> sentenceList = new ArrayList<String>();

        for (List<HasWord> sentence : dp) {
            // SentenceUtils not Sentence
            String sentenceString = SentenceUtils.listToString(sentence);
            result.add(sentenceString);
        }

        return result;
    }

    public static List<String> camelCase(String string){
        string = string.trim();
        String camelCasePattern = "([^\\p{L}\\d]+)|(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)|(?<=[\\p{L}&&[^\\p{Lu}]])(?=\\p{Lu})|(?<=\\p{Lu})(?=\\p{Lu}[\\p{L}&&[^\\p{Lu}]])";
        String[] subs = string.split(camelCasePattern);

        List<String> result = new ArrayList<>();
        for(String sub : subs){
            result.add(sub.toLowerCase());
        }
        return  result;
    }

    public static boolean equal(String word , String... wordSet){
        String s1 = StemTool.stemSingleWord(word).toLowerCase();
        for(String word2 : wordSet){
            String s2 = StemTool.stemSingleWord(word2).toLowerCase();
            if(s1.compareTo(s2) == 0)
                return true;
        }

        return false;
    }

    public static boolean isStopWord(String word) {
        return stopWordsList.contains(word);
    }

    public static List<String> java2Tokens(String sourceCode) {
        List<String> result = new ArrayList<>();

        InputStream in = new ByteArrayInputStream(sourceCode.getBytes());
        StreamTokenizer st = new StreamTokenizer(in);

        st.parseNumbers();
        st.wordChars('_', '_');
        st.eolIsSignificant(true);
        st.ordinaryChars(0, ' ');
        st.slashSlashComments(true);
        st.slashStarComments(true);

        int token;
        try {
            while ((token = st.nextToken()) != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_NUMBER:
                        double num = st.nval;
                        result.add(num + "");
                        break;
                    case StreamTokenizer.TT_WORD:
                        String word = st.sval;
                        result.add(word);
                        break;
                    case '"':
                        String dquoteVal = st.sval;
                        result.add(dquoteVal);
                        break;
                    case '\'':
                        String squoteVal = st.sval;
                        result.add(squoteVal);
                        break;
                    case StreamTokenizer.TT_EOL:
                        break;
                    case StreamTokenizer.TT_EOF:
                        break;
                    default:
                        String s = Character.toString((char)st.ttype).trim();
                        if (s.length() > 0) result.add(s);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        java2Tokens(" (Integer) gaugeValue);");
    }
}
