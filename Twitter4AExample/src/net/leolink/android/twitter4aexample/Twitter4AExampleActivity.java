package net.leolink.android.twitter4aexample;

import java.io.IOException;
import java.net.URL;

import net.leolink.android.twitter4a.Twitter4A;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Twitter4AExampleActivity extends Activity {
	// grab these keys at https://dev.twitter.com/apps
    public final static String TWITTER_CONSUMER_KEY = "RLAsuMh1IkwQlTXTqTBig";
    public final static String TWITTER_CONSUMER_SECRET = "DqeRw9BuRHKDcsCyQAttqJEZrk7q0zBIjP2kXvzJI";
	
    private Twitter4A mTwitter4a;
    private Button twitterBtn;
    private ImageView profilePic;
    private TextView userID;
    private TextView username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViewById();
		setup();
	}
	
	private void findViewById() {
		twitterBtn = (Button) findViewById(R.id.twitter_button);
		profilePic = (ImageView) findViewById(R.id.profile_pic);
		userID = (TextView) findViewById(R.id.user_id);
		username = (TextView) findViewById(R.id.user_name);
	}
	
	private void setup() {
		// create an Twitter4a object
		mTwitter4a = new Twitter4A(this, TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET) {
			@Override
			protected void logoutCallback() {
				twitterBtn.setText(R.string.log_in);
				profilePic.setVisibility(View.GONE);
				userID.setVisibility(View.GONE);
				username.setVisibility(View.GONE);
			}
			
			/*
			 * In loginCallback of this example, I just use user's data directly
			 * , but you should do something like save user's data to 
			 * SharedPreference for the persistence of your app.
			 */
			@Override
			protected void loginCallback() {
				userID.setText("User ID: " + this.getUserID());
				username.setText("Username: " + this.getUsername());

				// Since Android 3.0, you cannot run the codes which require 
				// network access directly on UI thread, so we need an AsyncTask
				// to do this job.
				// @see: http://www.androiddesignpatterns.com/2012/06/app-force-close-honeycomb-ics.html
				new AsyncTask<Void, Bitmap, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						try {
							publishProgress(loadBitmap(mTwitter4a.getProfilePicURL()));
						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void onProgressUpdate(Bitmap... values) {
						profilePic.setImageBitmap(values[0]);

						twitterBtn.setText(R.string.log_out);
						userID.setVisibility(View.VISIBLE);
						username.setVisibility(View.VISIBLE);
						profilePic.setVisibility(View.VISIBLE);
					}
				}.execute();
						
			}
		};

		// then use the Twitter4A object to log in/out
		twitterBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mTwitter4a.isLoggedIn())
					mTwitter4a.logout();
				else
					mTwitter4a.login();
			}
		});
	}
	
	private Bitmap loadBitmap(String url) throws IOException {
		URL mURL = new URL(url);
		Bitmap mBitmap = BitmapFactory.decodeStream(mURL.openConnection().getInputStream());
	    return mBitmap;
	}
}
