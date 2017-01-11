package iop.org.iop_contributors_app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import iop.org.iop_contributors_app.R;


/**
 * Created by mati on 07/11/16.
 */

public class ForumActivity extends ContributorBaseActivity {

    private static final String TAG = "ForumActivity";

    public static final String INTENT_URL = "currentUrl";
    public static final String INTENT_FORUM_ID = "forumID";

    private WebView webView;
    private ProgressBar progressBar;
    private String address;
    private FloatingActionButton fab_edit;

    //public static String FORUM_URL;//"http://test.fermat.community/";//104.199.78.250/";//"fermat.community/";

    @Override
    protected boolean hasDrawer() {
        return true;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        Intent intent = getIntent();

        String forumUrl = module.getForumUrl();

        if(intent.hasExtra(INTENT_URL)){
            address = getIntent().getStringExtra(INTENT_URL);
        } else
        if (intent.hasExtra(INTENT_FORUM_ID)){
            // posts http://fermat.community/t/propuesta-numero-4/19
            String forumId = intent.getStringExtra(INTENT_FORUM_ID);
            forumId = forumId.replace(" ","-");
            address = forumUrl+"/t/"+forumId;
        }
        else {
            address = forumUrl;
        }

        Log.d(TAG,"Url a cargar: "+address);

        // ui
        getLayoutInflater().inflate(R.layout.webview_with_loading,container);

        webView = (WebView) findViewById(R.id.webView);
        fab_edit = (FloatingActionButton) findViewById(R.id.fab_edit);
        fab_edit.setVisibility(View.GONE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] addressSplitted = address.split("/");
                String forumTitle = addressSplitted[4];
                String forumId = addressSplitted[5];
                Intent intent1 = new Intent(ForumActivity.this,ProposalSummaryActivity.class);
                intent1.setAction(ProposalSummaryActivity.ACTION_SUMMARY_PROPOSAL);
                intent1.putExtra(CreateProposalActivity.INTENT_DATA_FORUM_ID,Integer.valueOf(forumId));
                intent1.putExtra(CreateProposalActivity.INTENT_DATA_FORUM_TITLE,forumTitle);
                startActivity(intent1);
                finish();
            }
        });




//        address = FORUM_URL.contains("http://") ? FORUM_URL :"http://" + FORUM_URL;
        WebSettings webSetting = webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setAllowContentAccess(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        // put js callback
        webView.addJavascriptInterface(new MyJSI(), "myjsi");
        webView.loadUrl(address);



    }

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected boolean onContributorsBroadcastReceive(Bundle data) {
        return false;
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
            address = url;
            // check url
            checkUrl(url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            address = request.getUrl().toString();
            // check url
            checkUrl(address);
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub
            progressBar.setVisibility(View.GONE);
            view.loadUrl("javascript:window.onhashchange = function() { myjsi.doStuff(); };");
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.d(TAG,"onLoadResource");
            try {
                if (webView!=null) {
                    if (!webView.getUrl().equals(address)) {
                        checkUrl(webView.getUrl());
                        address = webView.getUrl();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }



        private void checkUrl(String url) {
            try {
                // todo: ver esto..
                //http://fermat.community/t/propuesta-a-enviar-numero-10/31
                String[] urlStr = url.split("/");
                if (urlStr.length < 4) return;
                String forumId = urlStr[5];
                if (module.isProposalMine(Integer.parseInt(forumId))) {
                    fab_edit.setVisibility(View.VISIBLE);
                } else
                    fab_edit.setVisibility(View.GONE);
            }catch (Exception e){
                e.printStackTrace();
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView = null;
    }

    /**
     * js class
     */
    private class MyJSI {

        @JavascriptInterface
        public void doStuff()
        {

            Log.d(TAG,"HOLASSSSS");
        }
    }
}
