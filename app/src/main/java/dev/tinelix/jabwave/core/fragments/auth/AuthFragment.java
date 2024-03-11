package dev.tinelix.jabwave.core.fragments.auth;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.AuthActivity;
import dev.tinelix.jabwave.ui.list.adapters.SimpleSpinnerAdapter;
import dev.tinelix.jabwave.ui.list.items.SimpleListItem;

public class AuthFragment extends Fragment {
    private View view;
    private SharedPreferences global_prefs;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_auth, container, false);
        Button sign_in_btn = view.findViewById(R.id.sign_in_btn);
        TextInputEditText username_edit = view.findViewById(R.id.username_edit);
        TextInputEditText password_edit = view.findViewById(R.id.password_edit);
        global_prefs = PreferenceManager.getDefaultSharedPreferences(
                Objects.requireNonNull(getContext()).getApplicationContext()
        );
        sign_in_btn.setOnClickListener(view -> {
            if (getActivity() != null) {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).signIn(
                            Objects.requireNonNull(username_edit.getText()).toString(),
                            Objects.requireNonNull(password_edit.getText()).toString()
                    );
                }
            }
        });
        ((LinearLayoutCompat) view.findViewById(R.id.auth_layout)).setGravity(Gravity.CENTER);
        createNetworksAdapter();
        return view;
    }

    private void createNetworksAdapter() {
        Spinner network_spinner = view.findViewById(R.id.networks_spinner);
        ArrayList<SimpleListItem> networks_list = new ArrayList<>();
        String[] array = getResources().getStringArray(R.array.supported_networks);
        for (String network_name: array) {
            networks_list.add(new SimpleListItem(network_name));
        }
        SimpleSpinnerAdapter adapter = new SimpleSpinnerAdapter(
                getContext(),
                networks_list,
                (list, position) -> {
                    setAuthNetwork(position);
                }
        );
        network_spinner.setAdapter(adapter);
        network_spinner.setSelection(0);
        setAuthNetwork(0);
    }

    private void setAuthNetwork(int position) {
        TextInputLayout username_til = view.findViewById(R.id.username_til);
        Spinner network_spinner = view.findViewById(R.id.networks_spinner);
        TextInputLayout password_til = view.findViewById(R.id.password_til);
        try {
            Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
            method.setAccessible(true);
            method.invoke(network_spinner);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = global_prefs.edit();
        if (position == 0) {
            username_til.setHint(R.string.auth_phone_number);
            password_til.setVisibility(View.GONE);
            editor.putString("network_type", "telegram");
        } else {
            username_til.setHint(R.string.auth_jid);
            password_til.setVisibility(View.VISIBLE);
            editor.putString("network_type", "auth");
        }
        network_spinner.setSelection(position);
        editor.apply();
    }

    public void setAuthorizationData(String instance, String username, String password) {
        try {
            TextInputEditText username_edit = view.findViewById(R.id.username_edit);
            TextInputEditText password_edit = view.findViewById(R.id.password_edit);
            username_edit.setText(username);
            password_edit.setText(password);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getNetworkType() {
        Spinner network_spinner = view.findViewById(R.id.networks_spinner);
        switch (network_spinner.getSelectedItemPosition()) {
            case 0:
                return "telegram";
            default:
                return "xmpp";
        }
    }
}
