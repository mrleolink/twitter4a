package net.leolink.android.twitter4aexample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.leolink.android.twitter4a.Twitter4A;

import java.io.IOException;
import java.net.URL;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class Twitter4AExampleActivity extends Activity {
	// grab these keys at https://dev.twitter.com/apps
    public final static String TWITTER_CONSUMER_KEY = "RLAsuMh1IkwQlTXTqTBig";
    public final static String TWITTER_CONSUMER_SECRET =
                                                        "DqeRw9BuRHKDcsCyQAttqJEZrk7q0zBIjP2kXvzJI";
	
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
        Log.e("linhln", "oncreate");
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
			/*
			 * In onSuccess of this example, I just use user's data directly
			 * , but you should do something like save user's data to 
			 * SharedPreference for the persistence of your app.
			 */
			@Override
			protected void onSuccess(Twitter twitter4j) {
                loadUserData(twitter4j);
			}

            @Override
            public void onFailed() {
                Toast.makeText(getApplicationContext(), "Oops, something went wrong!",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled() {
                Toast.makeText(getApplicationContext(), "Cancelled!", Toast.LENGTH_LONG).show();
            }
        };

		// then use the Twitter4A object to log in/out
		twitterBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (Twitter4A.isLoggedIn(getApplicationContext())) {
                    mTwitter4a.logout();
                    // UI
                    twitterBtn.setText(R.string.log_in);
                    profilePic.setVisibility(View.GONE);
                    userID.setVisibility(View.GONE);
                    username.setVisibility(View.GONE);
                } else {
                    mTwitter4a.login();
                }
            }
        });

        if (Twitter4A.isLoggedIn(this)) {
            loadUserData(Twitter4A.getCurrentSession(this));
        }
	}

    private void loadUserData(final Twitter twitter4j) {
        // Since Android 3.0, you cannot run the codes which require
        // network access directly on UI thread, so we need an AsyncTask
        // to do this job.
        // @see: http://www.androiddesignpatterns.com/2012/06/app-force-close-honeycomb-ics.html
        new AsyncTask<Void, Void, Bitmap>() {
            private User user;

            @Override
            protected void onPreExecute() {
                twitterBtn.setText("Loading...");
                twitterBtn.setEnabled(false);
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    user = twitter4j.showUser(twitter4j.getId());
                    return loadBitmap(user.getOriginalProfileImageURL());
                } catch (TwitterException te) {
                    te.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                userID.setText("User ID: " + user.getId());
                username.setText("Username: " + user.getName());
                profilePic.setImageBitmap(bitmap);
                userID.setVisibility(View.VISIBLE);
                username.setVisibility(View.VISIBLE);
                profilePic.setVisibility(View.VISIBLE);
                twitterBtn.setText(R.string.log_out);
                twitterBtn.setEnabled(true);
            }
        }.execute();
    }

    private Bitmap loadBitmap(String url) throws IOException {
        URL mURL = new URL(url);
        Bitmap mBitmap = BitmapFactory.decodeStream(mURL.openConnection().getInputStream());
        return mBitmap;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // IMPORTANT: you must call Twitter4A's onStop() in onStop() of the Activity or Fragment
        // where Twitter4A's login() was called.
        mTwitter4a.onStop();
    }
}
