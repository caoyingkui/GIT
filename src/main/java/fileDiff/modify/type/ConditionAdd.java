package fileDiff.modify.type;

import javax.validation.constraints.NotNull;

/**
 * Created by kvirus on 2019/4/24 8:43
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ConditionAdd extends Modify {

    String variable = "";



    public ConditionAdd(String variable) {
        this.variable = variable;
        build();
    }

    @Override
    public void build() {
        content.append(String.format(Template.CONDITION_ADD,  variable));
    }

    @Override
    public String getContent() {
        return content.toString();
    }

    @Override
    @NotNull
    public boolean equals(Object obj) {
        //TODO
        return false;
    }
}
