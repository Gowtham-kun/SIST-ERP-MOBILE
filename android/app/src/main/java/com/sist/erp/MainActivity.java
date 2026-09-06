package com.sist.erp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private static final String TAG = "SIST_ERP";
    private WebView webView;

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // ── System Bar Colors (Obsidian Dark #0f131c) ─────────────────────
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0xFF0F131C);
            window.setNavigationBarColor(0xFF0F131C);
        } catch (Throwable t) {
            Log.w(TAG, "Window bar styling note: " + t.getMessage());
        }

        try {
            // ── 120Hz Display Refresh Rate Configuration ──────────────────────
            configureHighRefreshRate();
        } catch (Throwable t) {
            Log.w(TAG, "Refresh rate configuration note: " + t.getMessage());
        }

        try {
            webView = new WebView(this);
            webView.setBackgroundColor(0xFF0F131C);
            setContentView(webView);

            // ── High Performance WebView Configuration ────────────────────────
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            // Hardware Acceleration
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

            // Register Native JavaScript Bridge for standalone direct ERP requests
            webView.addJavascriptInterface(new AndroidBridge(this), "AndroidBridge");

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("file:///android_asset/")) {
                        return false;
                    }
                    view.loadUrl(url);
                    return true;
                }
            });

            webView.setWebChromeClient(new WebChromeClient());

            // Load offline-first local asset
            webView.loadUrl("file:///android_asset/index.html");

        } catch (Throwable t) {
            Log.e(TAG, "Fatal WebView initialization error", t);
        }
    }

    private void configureHighRefreshRate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Display display = getDisplay();
                if (display != null) {
                    Display.Mode[] modes = display.getSupportedModes();
                    Display.Mode maxMode = null;
                    float maxFps = 60.0f;
                    if (modes != null) {
                        for (Display.Mode m : modes) {
                            if (m.getRefreshRate() > maxFps) {
                                maxFps = m.getRefreshRate();
                                maxMode = m;
                            }
                        }
                    }
                    if (maxMode != null) {
                        WindowManager.LayoutParams lp = getWindow().getAttributes();
                        lp.preferredDisplayModeId = maxMode.getModeId();
                        getWindow().setAttributes(lp);
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // ── Native JavaScript Interface ──────────────────────────────────────────
    public static class AndroidBridge {
        private final Context context;
        private static final String PREFS_NAME = "sist_erp_cache";
        private static final String KEY_DASHBOARD = "dashboard_data";

        public AndroidBridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public String erpPost(String endpoint, String token, String bodyJson) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://erp.sathyabama.ac.in/erp/api/v1.0/" + endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json, text/plain, */*");
                conn.setRequestProperty("Origin", "https://erp.sathyabama.ac.in");
                conn.setRequestProperty("Referer", "https://erp.sathyabama.ac.in/student/view");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");

                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setRequestProperty("Access-Token", token);
                    conn.setRequestProperty("Token", token);
                }

                conn.setConnectTimeout(12000);
                conn.setReadTimeout(12000);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                if (bodyJson != null && !bodyJson.isEmpty()) {
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = bodyJson.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                        os.flush();
                    }
                }

                int statusCode = conn.getResponseCode();
                InputStream is = (statusCode >= 200 && statusCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                if (is == null) return null;

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();

            } catch (Exception e) {
                Log.w("AndroidBridge", "Request to " + endpoint + " failed: " + e.getMessage());
                return null;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @JavascriptInterface
        public boolean isOnline() {
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                    return nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                          nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                          nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
                }
            } catch (Exception ignored) {}
            return true;
        }

        @JavascriptInterface
        public void saveCache(String cacheJson) {
            try {
                SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                sp.edit().putString(KEY_DASHBOARD, cacheJson).apply();
            } catch (Exception ignored) {}
        }

        @JavascriptInterface
        public String loadCache() {
            try {
                SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                return sp.getString(KEY_DASHBOARD, null);
            } catch (Exception ignored) {
                return null;
            }
        }

        @JavascriptInterface
        public void clearCache() {
            try {
                SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                sp.edit().remove(KEY_DASHBOARD).apply();
            } catch (Exception ignored) {}
        }
    }
}
