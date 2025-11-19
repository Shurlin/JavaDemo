package xyz.shurlin.demo2.ui.main;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.ui.login.LoginActivity;
import xyz.shurlin.demo2.utils.Constants;

public class UserFragment extends Fragment {

    private ActivityResultLauncher<Intent> launcher;

    private TextView tvDisplayName, tvEmail;
    private Button btnLogout;
    private boolean login = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == RESULT_OK) {
                updateDisplay();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDisplayName = view.findViewById(R.id.tvDisplayName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        TextView versionTextView = view.findViewById(R.id.tvVersion);
        versionTextView.setText("@Shurlin "+ Constants.version);

        updateDisplay();

        tvDisplayName.setOnClickListener((e) -> {
            if (!login) {
                Intent it = new Intent(requireContext(), LoginActivity.class);
                launcher.launch(it);
            }
        });

        btnLogout.setOnClickListener(e -> {
            SharedPreferences sp = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE);
            sp.edit().clear().apply();

            tvDisplayName.setText("点我登录");
            tvEmail.setText("");
            login = false;
            Toast.makeText(getContext(), "已登出", Toast.LENGTH_SHORT).show();
        });
    }

    void updateDisplay() {
        SharedPreferences sp = requireContext().getSharedPreferences("profile", Context.MODE_PRIVATE);
        String displayName = sp.getString("displayName", null);
        String email = sp.getString("email", null);

        if (displayName != null) {
            tvDisplayName.setText(displayName);
            tvEmail.setText(email);
            login = true;
        } else {
            tvDisplayName.setText("点我登录");
            tvEmail.setText("");
        }
    }
}