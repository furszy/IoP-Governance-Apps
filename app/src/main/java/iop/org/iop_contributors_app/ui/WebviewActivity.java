package iop.org.iop_contributors_app.ui;

/**
 * Created by mati on 07/11/16.
 */
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.Map;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.wallet.WalletModule;


public class WebviewActivity extends Activity {


    WebView web1;
    EditText ed1;
    Button bt1;
    String Address;
    String add;
    ProgressBar pbar;

    String currentUrl;

    WalletModule module;

    Map<String, String> additionalHttpHeaders;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_main);

        module = ApplicationController.getInstance().getWalletModule();

        Intent intent = getIntent();

        currentUrl = "http://fermat.community";

        web1 = (WebView)findViewById(R.id.webView1);
        ed1 = (EditText)findViewById(R.id.editText1);
        ed1.setText(currentUrl);
        bt1 = (Button)findViewById(R.id.button1);
        pbar = (ProgressBar)findViewById(R.id.progressBar1);
        pbar.setVisibility(View.GONE);

//        additionalHttpHeaders.put("Authorization","Token "+module.getForumToken());


        bt1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String url = ed1.getText().toString();

                Address = url.contains("http://") ? url :"http://" + url;
                WebSettings webSetting = web1.getSettings();
                webSetting.setBuiltInZoomControls(true);
                webSetting.setGeolocationEnabled(true);
                webSetting.setJavaScriptEnabled(true);
                webSetting.setAppCacheEnabled(true);
                webSetting.setAllowContentAccess(true);
                webSetting.setAllowFileAccess(true);
                webSetting.setDomStorageEnabled(true);

                web1.setWebViewClient(new WebViewClient());

                web1.loadUrl(Address,additionalHttpHeaders);

            }
        });
    }

    public class WebViewClient extends android.webkit.WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            pbar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            currentUrl = url;
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            currentUrl = request.getUrl().toString();
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
            pbar.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && web1.canGoBack()) {
            web1.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void checkIfTopicIsMine(){

    }
}
