package dev.tinelix.jabwave.core.utilities;

import android.util.Log;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import dev.tinelix.jabwave.JabwaveApp;
import dev.tinelix.jabwave.core.fragments.app.ChatsFragment;
import dev.tinelix.jabwave.core.fragments.app.NetworkServicesFragment;
import dev.tinelix.jabwave.core.fragments.settings.AccountSettingsFragment;
import dev.tinelix.jabwave.core.fragments.settings.AppearanceSettingsFragment;
import dev.tinelix.jabwave.core.fragments.settings.MainSettingsFragment;

public class FragmentNavigator {
    // Application Fragments
    public static final int FRAGMENT_CHATS                              = 10000;
    public static final int FRAGMENT_SERVICES                           = 10001;
    public static final int FRAGMENT_MAIN_SETTINGS                      = 10002;

    // Settings Fragments
    public static final int FRAGMENT_ACCOUNT_SETTINGS                   = 20000;
    public static final int FRAGMENT_ACCOUNT_INFO_SETTINGS              = 20001;
    public static final int FRAGMENT_APPEARANCE_SETTINGS                = 20100;
    public static final int FRAGMENT_SECURITY_SETTINGS                  = 20200;
    public static final int FRAGMENT_STORAGE_SETTINGS                   = 20300;
    public static final int FRAGMENT_NOTIFICATIONS_SETTINGS             = 20400;
    public static final int FRAGMENT_TRAFFIC_SAVINGS_SETTINGS           = 20500;
    public static final int FRAGMENT_PROTOCOL_SETTINGS                  = 20600;
    public static final int FRAGMENT_PROXY_SETTINGS                     = 20700;

    public static Fragment switchToAnotherFragment(FragmentManager fm, @IdRes int frame_resid, int fragment_id) {
        Fragment fragment = null;
        FragmentTransaction ft = fm.beginTransaction();
        switch (fragment_id) {
            case FRAGMENT_CHATS -> fragment = new ChatsFragment();
            case FRAGMENT_SERVICES -> fragment = new NetworkServicesFragment();
            case FRAGMENT_MAIN_SETTINGS -> fragment = new MainSettingsFragment();
            case FRAGMENT_ACCOUNT_SETTINGS -> fragment = new AccountSettingsFragment();
            case FRAGMENT_APPEARANCE_SETTINGS -> fragment = new AppearanceSettingsFragment();
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
