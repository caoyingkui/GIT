package fileDiff;

/**
 * Created by kvirus on 2019/4/18 11:22
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */


import java.io.Serializable;

/**
 * Change用来记录一次commit中，某种信息的前后变化
 * @param <T>
 */
public class Change <T> implements Serializable {
    //提交前
    public T OLD;

    //提交后
    public T NEW;

    public Change() {
        this.NEW = null;
        this.OLD = null;
    }

    public Change(T newOne, T oldOne) {
        this.NEW = newOne;
        this.OLD = oldOne;
    }

    @Override
    public int hashCode() {
        return NEW.hashCode() & OLD.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof Change)) return false;

        Change o = (Change)obj;
        return o.NEW.equals(this.NEW) && o.OLD.equals(this.OLD);
    }
}
