package auth.quickstart.firebase.google.com.facebook_google_signin_template;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class SecondPage extends AppCompatActivity {

    public static boolean isNetworkStatusAvialable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null)
                if (netInfos.isConnected())
                    return true;
        }
        return false;
    }

    private WebView mWebView;
//    private String testing = getIntent().getExtras().getString("keyName");
//    private String testingBreakPoint;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        String user = intent.getStringExtra(MainActivity.EXTRA_USER);
        String email = intent.getStringExtra(MainActivity.EXTRA_EMAIL);
        String fcm_token = intent.getStringExtra(MainActivity.EXTRA_FCM_TOKEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (isNetworkStatusAvialable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_SHORT).show();
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("google.com")
                    .appendPath("hidden_page")
                    .appendQueryParameter("email", email)
                    .appendQueryParameter("user", user)
                    .appendQueryParameter("fcm_token", fcm_token);
            String myUrl = builder.build().toString();
            Log.e("string is", myUrl);
            mWebView = new WebView(this);
            mWebView.loadUrl(myUrl);
            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("mailto:")) {
                        // We use `ACTION_SENDTO` instead of `ACTION_SEND` so that only email programs are launched.
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

                        // Parse the url and set it as the data for the `Intent`.
                        emailIntent.setData(Uri.parse(url));

                        // `FLAG_ACTIVITY_NEW_TASK` opens the email program in a new task instead as part of this application.
                        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        // Make it so.
                        startActivity(emailIntent);
                        return true;
                    } else {  // Load the URL in `webView`.
                        view.loadUrl(url);
                        return true;
                    }
                }
            });
//                    findViewById(R.id.splash).setVisibility(View.GONE);
//                    findViewById(R.id.webview).setVisibility(View.VISIBLE);


            this.setContentView(mWebView);
        } else {
            Toast.makeText(getApplicationContext(), "Internet is currenly unavialable.  Please check your connection.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SecondPage.this, NoService.class));
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

