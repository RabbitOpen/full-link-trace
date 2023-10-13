package rabbit.flt.rpc.common;

import java.nio.channels.SelectionKey;

public interface SelectorResetListener {

    /**
     * 重建selector后出发的事件
     * @param oldKey
     * @param newKey
     */
    void keyChanged(SelectionKey oldKey, SelectionKey newKey);
}
