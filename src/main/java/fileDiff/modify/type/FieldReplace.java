package fileDiff.modify.type;

/**
 * Created by kvirus on 2019/4/23 22:34
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class FieldReplace extends Modify {

    private String source = "";

    private String sourceComment = "";

    private String target = "";

    private String targetComment = "";

    public FieldReplace(String source, String sourceComment, String target, String targetComment) {
        this.source = source.trim();
        this.sourceComment = sourceComment.trim();
        this.target = target.trim();
        this.targetComment = targetComment.trim();
        build();
    }

    protected void build() {
        content.append(String.format(Template.FIELD_REPLACE, source, target));
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
        if (!(obj instanceof FieldReplace)) return false;
        FieldReplace r = (FieldReplace) obj;
        if (target.equals(r.target)) return true;
        return true;
    }
}
