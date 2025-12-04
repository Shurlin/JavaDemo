package xyz.shurlin.demo2.ui.main;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.shurlin.demo2.R;
import xyz.shurlin.demo2.network.ApiClient;
import xyz.shurlin.demo2.network.ApiService;
import xyz.shurlin.demo2.ui.login.LoginActivity;
import xyz.shurlin.demo2.utils.Constants;

public class UserFragment extends Fragment {

    private ActivityResultLauncher<Intent> launcher;
    private ApiService apiService;

    private TextView tvDisplayName, tvEmail;
    private Button btnLogout;
    private Button btnCheckUpdate;
    private boolean login = false;
    private String version;
    private String latestVersion = "0.0";


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
        btnCheckUpdate = view.findViewById(R.id.btnCheckUpdate);
        TextView versionTextView = view.findViewById(R.id.tvVersion);

        try {
            version = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName;
            versionTextView.setText("@Shurlin v" + version);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

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

        apiService = ApiClient.getApiService();
        btnCheckUpdate.setOnClickListener(e -> {
            Call<String> call = apiService.getLatestVersion();

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        latestVersion = response.body();

                        //判断
                        if (version.equals(latestVersion)) {
                            Toast.makeText(getContext(), "已是最新版本", Toast.LENGTH_SHORT).show();
                        } else if (latestVersion.equals("0.0")) {
                            Toast.makeText(getContext(), "检查更新失败", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "有新版本可用: v" + latestVersion + ", 已为你下载", Toast.LENGTH_SHORT).show();

                            Call<ResponseBody> downloadCall = apiService.downloadApk();
                            downloadCall.enqueue(new retrofit2.Callback<ResponseBody>() {
                                @Override
                                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        getApkAndInstall(response.body()).start();
                                    } else {
                                        Log.e("UserFragment", "APK下载失败: " + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                    Log.e("UserFragment", "APK下载失败: " + t.getMessage());
                                }
                            });
                        }
                    } else {
                        Log.e("UserFragment", "获取最新版本失败: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Log.e("UserFragment", "获取最新版本失败: " + t.getMessage());
                }
            });
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

    Thread getApkAndInstall(ResponseBody response) {
        return new Thread(() -> {
            try {
                File appExtDir = getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (appExtDir == null) {
                    // 兜底到内部缓存目录
                    appExtDir = getContext().getFilesDir();
                }

                // 确保目录存在
                if (!appExtDir.exists()) {
                    boolean ok = appExtDir.mkdirs();
                    Log.i(String.valueOf(this), "创建目录 " + appExtDir.getAbsolutePath() + " -> " + ok);
                }

                File latestApk = new File(appExtDir, "latest.apk"); // e.g. "latest.apk"
                // 如果需要每次覆盖，先删除旧文件
                if (latestApk.exists()) {
                    latestApk.delete();
                }
                // 创建空文件（可选，因为 FileOutputStream 会创建），但我们可以返回 File 供后续写入
                boolean created = latestApk.createNewFile();
                Log.i(String.valueOf(this), "apk 文件: " + latestApk.getAbsolutePath() + " 已创建? " + created);

                InputStream is = response.byteStream();
                FileOutputStream fos = new FileOutputStream(latestApk);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    byte[] apkData = is.readAllBytes();
                    fos.write(apkData);
                } else {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                }
                fos.flush();
                fos.close();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "下载完成，准备安装", Toast.LENGTH_SHORT).show();

                    //安装apk
                    if (!latestApk.exists()) {
                        Toast.makeText(getContext(), "安装包不存在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String authority = requireContext().getPackageName() + ".provider";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(FileProvider.getUriForFile(requireContext(), authority, latestApk),
                            "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}