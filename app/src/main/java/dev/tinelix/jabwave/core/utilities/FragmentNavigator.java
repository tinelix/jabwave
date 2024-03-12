package dev.tinelix.jabwave.core.utilities;

import android.util.Log;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.core.fragments.app.ChatsFragment;
import dev.tinelix.jabwave.core.fragments.app.NetworkServicesFragment;
import dev.tinelix.jabwave.core.fragments.settings.MainSettingsFragment;

public class FragmentNavigator {
    public static final int FRAGMENT_CHATS                      = 100;
    public static final int FRAGMENT_SERVICES                   = 101;
    public static final int FRAGMENT_SETTINGS                   = 102;

    public static Fragment switchToAnotherFragment(FragmentManager fm, @IdRes int frame_resid, int fragment_id) {
        Fragment fragment = null;
        FragmentTransaction ft = fm.beginTransaction();
        switch (fragment_id) {
            case FRAGMENT_CHATS -> fragment = new ChatsFragment();
            case FRAGMENT_SERVICES -> fragment = new NetworkServicesFragment();
            case FRAGMENT_SETTINGS -> fragment = new MainSettingsFragment();
        }
        if(fragment != null) {
            Log.d(JabwaveApp.APP_TAG,
                    String.format("Switching fragment to %s", fragment.getClass().getSimpleName())
            );
            ft.replace(frame_resid, fragment);
            ft.commit();
        } else {
            Log.e(JabwaveApp.APP_TAG, "Invalid Fragment ID for switching");
        }
        return fragment;
    }
}
