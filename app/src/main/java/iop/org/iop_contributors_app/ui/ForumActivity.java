package iop.org.iop_contributors_app.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;


/**
 * Created by mati on 07/11/16.
 */

public class ForumActivity extends BaseActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private String address;

    private static final String FORUM_URL = "fermat.community/";

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        getLayoutInflater().inflate(R.layout.webview_with_loading,container);

        webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

        address = FORUM_URL.contains("http://") ? FORUM_URL :"http://" + FORUM_URL;
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setAllowContentAccess(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        webView.loadUrl(address);

    }

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
