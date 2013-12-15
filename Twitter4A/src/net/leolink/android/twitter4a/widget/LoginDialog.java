package net.leolink.android.twitter4a.widget;

import net.leolink.android.twitter4a.R;
import net.leolink.android.twitter4a.Twitter4A;
import net.leolink.android.twitter4a.utils.Constants;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

/*
 * Some functions of this class were completely or partially taken from:
 * https://github.com/facebook/facebook-android-sdk/blob/master/facebook/src/com/facebook/widget/WebDialog.java
 * 
 * For the copyright of those parts:
 * 
 * *****************************************************************************
 * 
 *  Copyright 2013 Facebook
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 */
public class LoginDialog extends Dialog{
	// width below which there are no extra margins
    private static final int NO_BUFFER_SCREEN_WIDTH = 512;
    // width beyond which we're always using the MIN_SCALE_FACTOR
    private static final int MAX_BUFFER_SCREEN_WIDTH = 1024;
    // the minimum scaling factor for the web dialog (60% of screen size)
    private static final double MIN_SCALE_FACTOR = 0.6;	
    // translucent border around the webview
    private static final int BACKGROUND_GRAY = 0xCC000000;	
    // default theme
    public static final int DEFAULT_THEME = android.R.style.Theme_Translucent_NoTitleBar;

    private Context mContext;
    private Twitter4A mTwitter4A;
	private String url;
	private FrameLayout contentFrameLayout;
	private Spinner spinner;
	private ImageView crossImageView;
	private WebView webView;
	
	public LoginDialog(Context context, Twitter4A t4a, String url) {
		// Since it took me like 2 or 3 hours to figured out that I have to set
		// theme for this dialog to work, I REALLY need to note it here!!!
		// Using this super's constructor is a MUST!!!
		super(context, DEFAULT_THEME); 
		this.mContext = context;
		this.url = url;
		this.mTwitter4A = t4a;
	}

    public LoginDialog(Context context, int theme, Twitter4A t4a, 
    		String url) {
		super(context, theme);
		this.mContext = context;
		this.url = url;
		this.mTwitter4A = t4a;
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// when this dialog is closed
		setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mTwitter4A.setLoggingIn(false);
			}
		});

		// initialize spinner
		spinner = new Spinner(mContext);
		spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		spinner.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				LoginDialog.this.dismiss();

				// call loginFailedCallback
				mTwitter4A.loginFailedCallback();
			}
		});

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		contentFrameLayout = new FrameLayout(getContext());
		contentFrameLayout.setBackgroundColor(BACKGROUND_GRAY);
		
        // First calculate the margins around the frame layout
        Pair<Integer, Integer> margins = getMargins();
        contentFrameLayout.setPadding(margins.first, margins.second, margins.first, margins.second);
        
        /* Create the 'x' image, but don't add to the contentFrameLayout layout yet
         * at this point, we only need to know its drawable width and height
         * to place the webview
         */
        createCrossImage();
        
        /* Now we know 'x' drawable width and height,
         * layout the webview and add it the contentFrameLayout layout
         */
        int crossWidth = crossImageView.getDrawable().getIntrinsicWidth();

        setUpWebView(crossWidth / 2 + 1);
        
        /* Finally add the 'x' image to the contentFrameLayout layout and
        * add contentFrameLayout to the Dialog view
        */
        contentFrameLayout.addView(crossImageView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(contentFrameLayout,
        		new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}
	
    private Pair<Integer, Integer> getMargins() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        double scaleFactor;
        int scaledWidth = (int) ((float) width / metrics.density);
        if (scaledWidth <= NO_BUFFER_SCREEN_WIDTH) {
            scaleFactor = 1.0;
        } else if (scaledWidth >= MAX_BUFFER_SCREEN_WIDTH) {
            scaleFactor = MIN_SCALE_FACTOR;
        } else {
            // between the NO_BUFFER and MAX_BUFFER widths, we take a linear reduction to go from 100%
            // of screen size down to MIN_SCALE_FACTOR
            scaleFactor = MIN_SCALE_FACTOR +
                    ((double) (MAX_BUFFER_SCREEN_WIDTH - scaledWidth))
                            / ((double) (MAX_BUFFER_SCREEN_WIDTH - NO_BUFFER_SCREEN_WIDTH))
                            * (1.0 - MIN_SCALE_FACTOR);
        }

        int leftRightMargin = (int) (width * (1.0 - scaleFactor) / 2);
        int topBottomMargin = (int) (height * (1.0 - scaleFactor) / 2);

        return new Pair<Integer, Integer>(leftRightMargin, topBottomMargin);
    }	

    private void createCrossImage() {
        crossImageView = new ImageView(getContext());
        // Dismiss the dialog when user click on the 'x'
        crossImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendCancelToListener();
                LoginDialog.this.dismiss();
            }
        });
        Drawable crossDrawable = getContext().getResources().getDrawable(R.drawable.close);
        crossImageView.setImageDrawable(crossDrawable);
        /* 'x' should not be visible while webview is loading
         * make it visible only after webview has fully loaded
        */
        crossImageView.setVisibility(View.INVISIBLE);
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView(int margin) {
    	// prevent WebView from saving cookie automatically
    	CookieSyncManager.createInstance(mContext);
    	CookieManager cookieManager = CookieManager.getInstance();
    	cookieManager.setAcceptCookie(false);
    	
    	// setup WebView 
        LinearLayout webViewContainer = new LinearLayout(getContext());
        webView = new WebView(getContext());
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new DialogWebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // prevent WebView from saving form data and password
        webSettings.setSaveFormData(false);
        if (Build.VERSION.SDK_INT <= 18) { // Disable the dialog which asks to save password
        	webSettings.setSavePassword(false);
        } else {
            // No need to do anything because as mentioned here:
            // http://developer.android.com/reference/android/webkit/WebSettings.html#setSavePassword(boolean)
            // since API 19, WebView doesn't ask to save password
        }
        webView.loadUrl(url);
        webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        webView.setVisibility(View.INVISIBLE);
	

        webViewContainer.setPadding(margin, margin, margin, margin);
        webViewContainer.addView(webView);
        webViewContainer.setBackgroundColor(BACKGROUND_GRAY);
        contentFrameLayout.addView(webViewContainer);
    }    

    // Extends WebViewClient to make call back
    private class DialogWebViewClient extends WebViewClient {
    	@Override
    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
    		// login successfully
    		if (url.startsWith(Constants.TWITTER_CALLBACK_PREFIX)) {
    			// dismiss this dialog
    			LoginDialog.this.dismiss();
    			
    			// save user's data
    			mTwitter4A.handleSuccessfulLogin(url);

    			return true;
    		}
    		return false;
    	}
    	
    	@Override
    	public void onReceivedSslError(WebView view, SslErrorHandler handler,
    			SslError error) {
    		handler.proceed(); // Ignore SSL error
    	}
    	
    	@Override
    	public void onPageStarted(WebView view, String url, Bitmap favicon) {
    		super.onPageStarted(view, url, favicon);
    		spinner.show();
    	}
    	
    	@Override
    	public void onPageFinished(WebView view, String url) {
    		super.onPageFinished(view, url);
    		
            /*
             * Once web view is fully loaded, set the contentFrameLayout background to be transparent
             * and make visible the 'x' image.
             */
    		spinner.dismiss();
            contentFrameLayout.setBackgroundColor(Color.TRANSPARENT);
            webView.setVisibility(View.VISIBLE);
            crossImageView.setVisibility(View.VISIBLE);	
    	}
    }    
}
