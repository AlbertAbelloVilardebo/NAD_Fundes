package com.shad.nad.viewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public final class MainActivity extends Activity {
    private static final String START_URL =
            "http://217.154.181.249/ws/pautestallers/visor2.php";

    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorPanel;
    private TextView errorText;
    private Uri lockedPage;
    private boolean mainFrameFailed;
    private boolean sslWarningShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setLongClickable(false);
        webView.setOnLongClickListener(v -> true);
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        root.addView(progressBar, new FrameLayout.LayoutParams(dp(52), dp(52), Gravity.CENTER));

        errorPanel = buildErrorPanel();
        errorPanel.setVisibility(View.GONE);
        root.addView(errorPanel, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(root);
        configureWebView();
        loadStartPage();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new LockedWebViewClient());
        webView.setDownloadListener(new BlockedDownloadListener());
    }

    private LinearLayout buildErrorPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(28), dp(28), dp(28), dp(28));
        panel.setBackgroundColor(Color.WHITE);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.nad_logo);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(130));
        logoParams.bottomMargin = dp(24);
        panel.addView(logo, logoParams);

        TextView title = new TextView(this);
        title.setText("No s'ha pogut obrir el visor");
        title.setTextColor(Color.BLACK);
        title.setTextSize(21);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.bottomMargin = dp(12);
        panel.addView(title, titleParams);

        errorText = new TextView(this);
        errorText.setTextColor(0xff444444);
        errorText.setTextSize(15);
        errorText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.bottomMargin = dp(22);
        panel.addView(errorText, textParams);

        Button retry = new Button(this);
        retry.setText("Tornar-ho a provar");
        retry.setAllCaps(false);
        retry.setOnClickListener(v -> loadStartPage());
        panel.addView(retry, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return panel;
    }

    private void loadStartPage() {
        lockedPage = null;
        mainFrameFailed = false;
        errorPanel.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        webView.stopLoading();
        webView.clearHistory();
        webView.loadUrl(START_URL);
    }

    private void showError(String message) {
        mainFrameFailed = true;
        progressBar.setVisibility(View.GONE);
        webView.setVisibility(View.INVISIBLE);
        errorText.setText(message);
        errorPanel.setVisibility(View.VISIBLE);
    }

    private boolean shouldBlockMainFrame(Uri target) {
        if (target == null) return true;
        String scheme = lower(target.getScheme());
        if (!"http".equals(scheme) && !"https".equals(scheme)) return true;
        return lockedPage != null && !sameDocument(lockedPage, target);
    }

    private static boolean sameDocument(Uri first, Uri second) {
        return first != null
                && second != null
                && lower(first.getScheme()).equals(lower(second.getScheme()))
                && lower(first.getHost()).equals(lower(second.getHost()))
                && effectivePort(first) == effectivePort(second)
                && safePath(first).equals(safePath(second));
    }

    private static int effectivePort(Uri uri) {
        if (uri.getPort() != -1) return uri.getPort();
        return "https".equals(lower(uri.getScheme())) ? 443 : 80;
    }

    private static String safePath(Uri uri) {
        String path = uri.getPath();
        return path == null || path.isEmpty() ? "/" : path;
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        WebView view = webView;
        webView = null;
        if (view != null) {
            view.stopLoading();
            view.setWebChromeClient(null);
            view.setWebViewClient(null);
            view.loadUrl("about:blank");
            view.clearHistory();
            view.removeAllViews();
            view.destroy();
        }
        super.onDestroy();
    }

    private final class LockedWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request == null || request.getUrl() == null) return true;
            if (!request.isForMainFrame()) return false;
            boolean blocked = shouldBlockMainFrame(request.getUrl());
            if (blocked) showNavigationBlocked();
            return blocked;
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean blocked = shouldBlockMainFrame(url == null ? null : Uri.parse(url));
            if (blocked) showNavigationBlocked();
            return blocked;
        }

        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            mainFrameFailed = false;
            errorPanel.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!mainFrameFailed && url != null && !"about:blank".equals(url)) {
                Uri finalPage = Uri.parse(url);
                String scheme = lower(finalPage.getScheme());
                if (("http".equals(scheme) || "https".equals(scheme)) && lockedPage == null) {
                    lockedPage = finalPage;
                }
                progressBar.setVisibility(View.GONE);
                errorPanel.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                view.clearHistory();
            }
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request,
                                    WebResourceError error) {
            if (request != null && request.isForMainFrame()) {
                String detail = "Comprova la connexió i torna-ho a provar.";
                if (error != null && error.getDescription() != null) {
                    detail += "\n\n" + error.getDescription();
                }
                showError(detail);
            }
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                        WebResourceResponse response) {
            if (request != null && request.isForMainFrame() && response != null) {
                showError("El servidor ha respost amb l'error HTTP "
                        + response.getStatusCode()
                        + ".\nTorna-ho a provar d'aquí a uns instants.");
            }
            super.onReceivedHttpError(view, request, response);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // Excepció provisional sol·licitada: ignora qualsevol error TLS,
            // independentment de la IP o del host que carregui el WebView.
            handler.proceed();
            if (!sslWarningShown) {
                sslWarningShown = true;
                Toast.makeText(MainActivity.this,
                        "Mode provisional: certificat del servidor no verificat.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showNavigationBlocked() {
        Toast.makeText(this,
                "Navegació bloquejada: l'app queda limitada al visor NAD.",
                Toast.LENGTH_SHORT).show();
    }

    private final class BlockedDownloadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent,
                                    String contentDisposition, String mimeType,
                                    long contentLength) {
            Toast.makeText(MainActivity.this,
                    "Les descàrregues estan bloquejades en aquesta app.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
