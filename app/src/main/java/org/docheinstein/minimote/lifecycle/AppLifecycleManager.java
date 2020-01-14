package org.docheinstein.minimote.lifecycle;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class AppLifecycleManager implements LifecycleObserver {

    public interface AppLifecycleListener {
        void onAppStart();
        void onAppStop();
    }

    private static final AppLifecycleManager INSTANCE = new AppLifecycleManager();

    private static final String TAG = "AppLifecycleManager";
    private final Set<AppLifecycleListener> mListeners = new CopyOnWriteArraySet<>();

    public static AppLifecycleManager getInstance() {
        return INSTANCE;
    }

    public void addListener(AppLifecycleListener obs) {
        mListeners.add(obs);
    }

    public void removeListener(AppLifecycleListener obs) {
        mListeners.remove(obs);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onLifecycleStart() {
        Log.v(TAG, "onAppStart");
        for (AppLifecycleListener l : mListeners)
            l.onAppStart();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onLifecycleStop() {
        Log.v(TAG, "onAppStop");
        for (AppLifecycleListener l : mListeners)
            l.onAppStop();
    }
}
