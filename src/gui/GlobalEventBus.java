package gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

public class GlobalEventBus {
    public interface DataUpdateListener {
        void onDataUpdated();
    }
    
    private static final List<DataUpdateListener> listeners = new CopyOnWriteArrayList<>();
    
    public static void addListener(DataUpdateListener listener) {
        listeners.add(listener);
    }
    
    public static void removeListener(DataUpdateListener listener) {
        listeners.remove(listener);
    }
    
    public static void fireUpdateEvent() {
        SwingUtilities.invokeLater(() -> {
            for (DataUpdateListener l : listeners) {
                try {
                    l.onDataUpdated();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
