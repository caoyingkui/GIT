package fileDiff.modify.type;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by kvirus on 2019/5/26 15:20
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
    public enum OrderValue {
        SIGNATURE(0),
        SYNCHRONIZED(6),
        REFACTOR(2),
        IF(3),
        TRY(4),
        STATEMENT(1),
        JAVADOC(7),
        NULL(8);
        public int order;
        OrderValue(int order){
            this.order = order;
        }
    }

    public OrderValue order() default OrderValue.NULL;
}
