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

import java.io.BufferedOutputStream;
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
    private static final String TAG = "UserFragment";
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
            // 目标目录：app 专属外部下载目录
            File downloadsDir = getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (downloadsDir == null) downloadsDir = getContext().getFilesDir();

            if (!downloadsDir.exists()) downloadsDir.mkdirs();

            // 使用临时文件名 -> 完成后原子重命名
            String finalName = "latest.apk"; // 或者使用 unique 名称 e.g. "latest-" + System.currentTimeMillis() + ".apk"
            File tmpFile = new File(downloadsDir, finalName + ".tmp");
            File finalFile = new File(downloadsDir, finalName);

            // 删除残留 tmp
            if (tmpFile.exists()) tmpFile.delete();

            try (FileOutputStream fos = new FileOutputStream(tmpFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos);
                 InputStream is = response.byteStream()) {

                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                bos.flush();
                fos.getFD().sync(); // 强制写入底层设备（尽可能保证数据已落盘）
            } catch (IOException e) {
                Log.e(TAG, "写入 tmp apk 失败: " + e.getMessage());
                if (tmpFile.exists()) tmpFile.delete();
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "下载失败: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                return;
            }

            // 重命名 tmp -> final（若 final 存在先删除或备份）
            if (finalFile.exists()) {
                boolean del = finalFile.delete();
                Log.i(TAG, "删除旧 finalFile: " + del);
            }
            boolean renamed = tmpFile.renameTo(finalFile);
            Log.i(TAG, "tmp -> final 重命名: " + renamed + " finalPath=" + finalFile.getAbsolutePath());

//            // 计算校验信息用于 debug（可选）
//            String sha = sha256File(finalFile);
//            Log.i(TAG, "APK 大小: " + finalFile.length() + " bytes, lastMod: " + finalFile.lastModified() + ", sha256: " + sha);

            // 回到主线程触发安装
            getActivity().runOnUiThread(() -> {
                try {
                    // authority 必须和 manifest 中 provider 的 authority 一致
                    String authority = requireContext().getPackageName() + ".provider"; // e.g. xyz.shurlin.demo2.fileprovider
                    Uri contentUri = FileProvider.getUriForFile(getContext(), authority, finalFile);

                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                    installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(installIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "安装失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "安装异常", e);
                }
            });
        });
    }
}