package fileDiff.modify.type;

/**
 * Created by kvirus on 2019/4/30 13:14
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class MethodReplace extends Modify {
    private String source = "";

    private String sourceComment = "";

    private String target = "";

    private String targetComment = "";


    public MethodReplace(String source, String sourceComment, String target, String targetComment) {
        this.source = source.trim();
        this.sourceComment = sourceComment;
        this.target = target;
        this.targetComment = targetComment;
        build();
    }

    @Override
    protected void build() {
        if (sourceComment.length() != 0 || targetComment.length() != 0) {
            content.append("where");
            if (sourceComment.length() > 0)
                content.append("\n    ").append(source).append(": ").append(sourceComment);
            if (targetComment.length() > 0)
                content.append("\n    ").append(target).append(": ").append(targetComment);
        }
    }

    @Override
    public String getContent() {
        return content.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodReplace)) return false;
        MethodReplace r = (MethodReplace) obj;
        if (source.equals(r.source) || target.equals(r.target)) return true;
        return true;
    }
}
