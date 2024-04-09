package com.pizzaza.uk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.pizzaza.uk.controller.MyControl;
import com.pizzaza.uk.controller.MyMethods;
import com.pizzaza.uk.helper.MyHelper;
import com.pizzaza.uk.helper.ChromeClient;
import com.pizzaza.uk.helper.HelloWebViewClient;
import com.pizzaza.uk.helper.MyWebDownloader;
import com.pizzaza.uk.network.NetworkStateReceiver;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    WebView webView;
    SwipeRefreshLayout swipeRefreshLayout;
    LottieAnimationView progress_loading;
    LinearLayout no_Internet;
    TextView nonetTitle, nonetDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For Internet
        MyMethods.startNetworkBroadcastReceiver(this);

        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        // OneSignal Initialization
        OneSignal.initWithContext(this, getResources().getString(R.string.onesignalAppId));

        // variable initialize
        webView = findViewById(R.id.webView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progress_loading = findViewById(R.id.progress_loading);
        no_Internet = findViewById(R.id.No_Internet);
        nonetTitle = findViewById(R.id.nonetTitle);
        nonetDescription = findViewById(R.id.nonetDescription);




        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.getSettings().setUserAgentString(MyControl.USER_AGENT);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebChromeClient(new ChromeClient(MainActivity.this));
        webView.setWebViewClient(new HelloWebViewClient(MainActivity.this));
        webView.getSettings().setDomStorageEnabled(true);


        //web settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setAllowContentAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);

        webSettings.setSaveFormData(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setEnableSmoothTransition(true);

        webView.getSettings().setBlockNetworkLoads(false);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setDomStorageEnabled(true);

        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.clearCache(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);


        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);
        }

        // Enable Cookies
        CookieManager.getInstance().setAcceptCookie(true);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }




        // HelloWebViewClient
        new HelloWebViewClient(new MyHelper() {
            @Override
            public void loading() {

            }

            @Override
            public void finishLoading() {

            }

            @Override
            public void webGoBack() {
                webView.goBack();
            }

            @Override
            public void webLoadUrl(String url) {
                webView.loadUrl(url);
            }

            @Override
            public void errorLoading() {
                if(MyControl.NETWORK_AVAILABLE){
                    //We have internet but something went wrong -- Show the error page
                    no_Internet.setVisibility(View.VISIBLE);
                    nonetTitle.setText("Website Load Failed");
                    nonetDescription.setText("Error Reason:\n"+MyControl.LOAD_ERROR_REASON);
                }


            }
        });




        // Handle WebLoading
        new ChromeClient(new MyHelper() {
            @Override
            public void loading() {
                progress_loading.setVisibility(View.VISIBLE);
            }

            @Override
            public void finishLoading() {
                progress_loading.setVisibility(View.GONE);
            }

            @Override
            public void webGoBack() {

            }

            @Override
            public void webLoadUrl(String url) {

            }

            @Override
            public void errorLoading() {

            }
        });




        // Handle Download Loading
        if (MyControl.isDownloading) {
            new MyWebDownloader(new MyHelper() {
                @Override
                public void loading() {
                }

                @Override
                public void finishLoading() {
                    progress_loading.setVisibility(View.GONE);
                }

                @Override
                public void webGoBack() {
                    webView.goBack();
                }

                @Override
                public void webLoadUrl(String url) {

                }

                @Override
                public void errorLoading() {

                }
            });

            MyWebDownloader.WebDownloader(webView, MainActivity.this);
        }

        // For Refresh in WebView
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WebViewRefresh();
            }

        });


        webView.loadUrl(getString(R.string.Website_Link));


    } // OnCreate Method End Here ===================





    @Override
    public void networkAvailable() {
        MyControl.NETWORK_AVAILABLE = true;

        if (MyControl.FAILED_FOR_OTHER_REASON==false)
            no_Internet.setVisibility(View.GONE);
        else
        no_Internet.setVisibility(View.VISIBLE);
    }

    @Override
    public void networkUnavailable() {
        MyControl.NETWORK_AVAILABLE = false;
        no_Internet.setVisibility(View.VISIBLE);
        nonetTitle.setText("No Internet");
        nonetDescription.setText("Please Check Internet Connection and Try Again..");
    }

    @Override
    protected void onPause() {
        MyMethods.unregisterNetworkBroadcastReceiver(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        MyMethods.registerNetworkBroadcastReceiver(this);
        super.onResume();
    }


    //======================= invoic for print  ======================
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        // Method to handle printing
        @JavascriptInterface
        public void printInvoice() {
            runOnUiThread(() -> {
                // Add code here to handle the printing functionality
                printWebView(webView);
            });
        }
    }


    // Method to print the WebView content
    private void printWebView(WebView webView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            String jobName = getString(R.string.app_name) + " Document";

            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

            PrintAttributes attributes = new PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(new PrintAttributes.Resolution("id", "Resolution", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                    .build();

            PrintJob printJob = printManager.print(jobName, printAdapter, attributes);

            if (printJob.isCompleted()) {
                Toast.makeText(MainActivity.this, "Print job completed", Toast.LENGTH_SHORT).show();
            } else if (printJob.isFailed()) {
                Toast.makeText(MainActivity.this, "Print job failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Printing is not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }



    //=================================================================

    private void WebViewRefresh() {
        if (!MyMethods.isConnected(MainActivity.this)) {
            swipeRefreshLayout.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            no_Internet.setVisibility(View.VISIBLE);
        } else {
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            webView.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(true);
            no_Internet.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                    webView.reload();
                }
            }, 2000);
        }

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_dark), getResources().getColor(android.R.color.holo_orange_dark), getResources().getColor(android.R.color.holo_green_dark), getResources().getColor(android.R.color.holo_red_dark));

    } // WebViewRefresh End Here ============





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;

            /*-- if file request cancelled; exited camera. we need to send null value to make future attempts workable --*/
            if (resultCode == Activity.RESULT_CANCELED) {
                MyControl.file_path.onReceiveValue(null);
                return;
            }

            /*-- continue if response is positive --*/
            if (resultCode == Activity.RESULT_OK) {
                if (null == MyControl.file_path) {
                    return;
                }
                ClipData clipData;
                String stringData;

                try {
                    clipData = intent.getClipData();
                    stringData = intent.getDataString();
                } catch (Exception e) {
                    clipData = null;
                    stringData = null;
                }
                if (clipData == null && stringData == null && MyControl.cam_file_data != null) {
                    results = new Uri[]{Uri.parse(MyControl.cam_file_data)};
                } else {
                    if (clipData != null) {
                        final int numSelectedFiles = clipData.getItemCount();
                        results = new Uri[numSelectedFiles];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            results[i] = clipData.getItemAt(i).getUri();
                        }
                    } else {
                        try {
                            Bitmap cam_photo = (Bitmap) intent.getExtras().get("data");
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            cam_photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            stringData = MediaStore.Images.Media.insertImage(this.getContentResolver(), cam_photo, null, null);
                        } catch (Exception ignored) {
                        }

                        results = new Uri[]{Uri.parse(stringData)};
                    }
                }
            }

            MyControl.file_path.onReceiveValue(results);
            MyControl.file_path = null;
        } else {
            if (requestCode == MyControl.file_req_code) {
                if (null == MyControl.file_data) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                MyControl.file_data.onReceiveValue(result);
                MyControl.file_data = null;
            }
        }

    } // onActivityResult End Here =============

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }




    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired
    private long mBackPressed;

    // When user click bakpress button this method is called
    @Override
    public void onBackPressed() {

        // When landing in home screen
        if (!webView.canGoBack()) {

            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                finishAndRemoveTask();
            } else {
                Toast.makeText(getBaseContext(), "Press again to exit",
                        Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();

        } else{
            webView.goBack();
        }


    } // end of onBackpressed method

    //#############################################################################################







} // Public Class End Here ==========================