package dev.tinelix.jabwave.core.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.core.activities.AuthActivity;

public class AuthCloudPasswordFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_password, container, false);
        TextView password_edit = view.findViewById(R.id.password_edit);
        view.findViewById(R.id.password_confirm_btn).setOnClickListener(
                view12 -> {
                    if (getActivity() != null) {
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).sendCloudPassword(password_edit.getText().toString());
                        }
                    }
                });
        view.findViewById(R.id.password_cancel_btn).setOnClickListener(
                view1 -> {
                    if(getActivity() != null) {
                        getActivity();//((AuthActivity) getActivity()).changeFragment("auth_form");
                    }
                });
        return view;
    }
}
