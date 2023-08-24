package app.simple.inure.loaders;

import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import app.simple.inure.BuildConfig;
import app.simple.inure.models.GitHubRelease;
import app.simple.inure.models.GitHubReleaseAsset;
import app.simple.inure.util.AppUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GitHubReleaseChecker {
    
    private static final String REPO_OWNER = "Hamza417";
    private static final String REPO_NAME = "Inure";
    
    public void checkAndDownloadNewRelease(Context context, OnDownloadCompleteListener listener) {
        try {
            TrafficStats.setThreadStatsTag((int) Thread.currentThread().getId());
            URL apiUrl = new URL("https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases");
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                
                Gson gson = new Gson();
                GitHubRelease[] releases = gson.fromJson(reader, GitHubRelease[].class);
                
                // Assuming releases[0] is the latest release
                GitHubRelease latestRelease = releases[0];
                
                for (GitHubRelease release : releases) {
                    Log.d("TAG", "checkAndDownloadNewRelease: " + release.getTagName());
                }
                
                // Compare with current app version
                // If new version available, proceed to download and install
                if (isNewVersionAvailable(latestRelease.getTagName())) {
                    Log.d("TAG", "checkAndDownloadNewRelease: New version available");
                    downloadReleaseAsset(latestRelease.getAssets(), latestRelease.getTagName(), context, listener);
                } else {
                    Log.d("TAG", "checkAndDownloadNewRelease: No new version available");
                }
                
                reader.close();
                inputStream.close();
            }
            
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private boolean isNewVersionAvailable(String latestVersion) {
        // Compare latestVersion with current app version
        // Return true if new version available
        return BuildConfig.VERSION_CODE
                < Integer.parseInt(latestVersion.substring(5));
    }
    
    @WorkerThread
    private void downloadReleaseAsset(List <GitHubReleaseAsset> asset, String tagName, Context context, OnDownloadCompleteListener listener) {
        // Use downloadUrl from the asset to download the file
        // Install the downloaded APK using an Intent
        if (AppUtils.INSTANCE.isGithubFlavor()) {
            for (GitHubReleaseAsset releaseAsset : asset) {
                if (releaseAsset.getName().contains("github")) {
                    // Download the release asset
                    // Install the downloaded APK using an Intent
                    OkHttpClient client = new OkHttpClient();
                    
                    Request request = new Request.Builder()
                            .url(releaseAsset.getDownloadUrl())
                            .build();
                    
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }
                        
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                File outputFile = new File(context.getExternalFilesDir(null), tagName + ".apk");
                                
                                if (outputFile.exists()) {
                                    Log.d("TAG", "onResponse: File already exists: " + outputFile.getName());
                                    listener.onDownloadComplete(outputFile);
                                    client.dispatcher().cancelAll();
                                    response.close();
                                    return;
                                } else {
                                    Log.d("TAG", "Downloading file: " + outputFile.getName());
                                }
                                
                                InputStream inputStream = response.body().byteStream();
                                
                                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, bytesRead);
                                        if (Thread.currentThread().isInterrupted()) {
                                            throw new IOException("Thread interrupted");
                                        }
                                    }
                                    
                                    listener.onDownloadComplete(outputFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    if (outputFile != null) {
                                        if (outputFile.exists()) {
                                            if (outputFile.delete()) {
                                                Log.d("TAG", "onResponse: deleted file: " + outputFile.getName());
                                            }
                                        }
                                    }
                                }
                                
                                inputStream.close();
                            }
                            
                            response.close();
                        }
                    });
                }
            }
        }
    }
    
    public interface OnDownloadCompleteListener {
        void onDownloadComplete(File outputFile);
    }
}
