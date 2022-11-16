package dev.tinelix.jabwave.user_interface.fragments.auth;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.user_interface.activities.AuthActivity;

public class AuthFragment extends Fragment {
    private View view;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.auth_fragment, container, false);
        Button sign_in_btn = view.findViewById(R.id.sign_in_btn);
        TextInputEditText username_edit = view.findViewById(R.id.username_edit);
        TextInputEditText password_edit = view.findViewById(R.id.password_edit);
        sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    if (getActivity().getClass().getSimpleName().equals("AuthActivity")) {
                        ((AuthActivity) getActivity()).signIn(username_edit.getText().toString(),
                                password_edit.getText().toString());
                    }
                }
            }
        });

        ((LinearLayoutCompat) view.findViewById(R.id.auth_layout)).setGravity(Gravity.CENTER);
        return view;
    }

    public void setAuthorizationData(String instance, String username, String password) {
        TextInputEditText username_edit = view.findViewById(R.id.username_edit);
        TextInputEditText password_edit = view.findViewById(R.id.password_edit);
        username_edit.setText(username);
        password_edit.setText(password);
    }
}
