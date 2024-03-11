package dev.tinelix.jabwave.core.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import androidx.fragment.app.Fragment;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.AuthActivity;

public class AuthTwoFactorFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_2fa, container, false);
        TextView twofactor_edit = view.findViewById(R.id.twofactor_edit);
        ((MaterialButton) view.findViewById(R.id.twofactor_confirm_btn)).setOnClickListener(
                view12 -> {
                    if (getActivity() != null) {
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).signIn(twofactor_edit.getText().toString());
                        }
                    }
                });
        ((MaterialButton) view.findViewById(R.id.twofactor_cancel_btn)).setOnClickListener(
                view1 -> {
                    if(getActivity() != null) {
                        getActivity();//((AuthActivity) getActivity()).changeFragment("auth_form");
                    }
                });
        return view;
    }
}
