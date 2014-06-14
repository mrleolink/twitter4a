package net.leolink.android.twitter4a;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.Window;

import net.leolink.android.twitter4a.utils.Const;
import net.leolink.android.twitter4a.utils.Util;
import net.leolink.android.twitter4a.widget.LoginDialog;
import net.leolink.android.twitter4a.widget.Spinner;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public abstract class Twitter4A {
	// twitter4j's objects
	private Twitter mTwitter;
	private RequestToken mTwitterRequestToken;
	private AccessToken mTwitterAccessToken;

	// twitter4a's objects
    private LoginDialog mLoginDialog;
	private String mConsumer;
	private String mConsumerSecret;
	private Context mContext;
    private boolean isStopped;

	// Constructor
	public Twitter4A(Context context, String consumerKey, String consumerKeySecret) {
		mContext = context;
		mConsumer = consumerKey;
		mConsumerSecret = consumerKeySecret;
	}

    /**
     * Run an AsyncTask to get Twitter authentication URL, then open login dialog
     */
	public void login() {
        new AsyncTask<Void, String, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(mConsumer);
                builder.setOAuthConsumerSecret(mConsumerSecret);
                Configuration configuration = builder.build();

                TwitterFactory factory = new TwitterFactory(configuration);
                mTwitter = factory.getInstance();

                try {
                    mTwitterRequestToken = mTwitter.getOAuthRequestToken(
                            Const.TWITTER_CALLBACK_PREFIX);
                    return mTwitterRequestToken.getAuthenticationURL();
                } catch (TwitterException e) {
                    if (BuildConfig.DEBUG) e.printStackTrace();
                    // call onFailed
                    if (!isStopped) onFailed();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    // open LoginDialog to let user login to Twitter
                    if (!isStopped) {
                        mLoginDialog = new LoginDialog(mContext, Twitter4A.this, s);
                        mLoginDialog.show();
                    }
                } else {
                    onFailed();
                }
            }
        }.execute();
	}

	// get data after login successfully
	public void handleSuccessfulLogin(String uri) {
		Uri mUri = Uri.parse(uri);
		String verifier = mUri.getQueryParameter(Const.URL_TWITTER_OAUTH_VERIFIER);
		
		// Because this task need to using network which cannot be run on UI
		// thread since Android 3.0 (maybe equivalent to API 11), so I need to
		// use AsyncTask here!
		new AsyncTask<String, Void, Void>() {
			private Spinner spinner;

			protected void onPreExecute() {
				spinner = new Spinner(mContext);
				spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
				spinner.setCancelable(true);
                spinner.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        // if spinner is explicitly cancelled by user
                        // remove everything
                        mTwitter = null;
                        mTwitterAccessToken = null;
                        mTwitterRequestToken = null;

                        // call onFailed
                        Twitter4A.this.onCancelled();
                    }
                });
				if (!isStopped) spinner.show();
			}
			
			@Override
			protected Void doInBackground(String... params) {
				try {
					mTwitterAccessToken = mTwitter
                            .getOAuthAccessToken(mTwitterRequestToken, params[0]);
                    // save tokens for later uses
                    // save consumer keys
                    Util.spPutString(mContext, Const.SP_TWITTER4A_CONSUMER, mConsumer);
                    Util.spPutString(mContext, Const.SP_TWITTER4A_CONSUMER_SECRET, mConsumerSecret);
                    Util.spPutString(mContext, Const.SP_TWITTER4A_TOKEN,
                            mTwitterAccessToken.getToken());
                    Util.spPutString(mContext, Const.SP_TWITTER4A_TOKEN_SECRET,
                            mTwitterAccessToken.getTokenSecret());
				} catch (TwitterException e) {
					if (BuildConfig.DEBUG) e.printStackTrace();
					// call onFailed
					if (!isStopped) onFailed();
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				if (spinner.isShowing()) {
					// dismiss the spinner
					spinner.dismiss();
					
					// call the callback
					onSuccess(mTwitter);
				}
			}
		}.execute(verifier);
	}

    public boolean isStopped() {
        return isStopped;
    }

    /**
     * This method must be called in {@link #onStop()} of the Activity or Fragment where
     * {@link #login()} was called.
     */
    public void onStop() {
        isStopped = true;
        if (mLoginDialog != null) {
            mLoginDialog.dismiss();
        }
    }

    /**
     * Clear all data of current session
     */
    public void logout() {
        // remove saved data
        Util.spRemove(mContext, Const.SP_TWITTER4A_CONSUMER);
        Util.spRemove(mContext, Const.SP_TWITTER4A_CONSUMER_SECRET);
        Util.spRemove(mContext, Const.SP_TWITTER4A_TOKEN);
        Util.spRemove(mContext, Const.SP_TWITTER4A_TOKEN_SECRET);

		// remove all Twitter4J objects
		mTwitter = null;
		mTwitterAccessToken = null;
		mTwitterRequestToken = null;

        // inform
        Util.logd("Logged out successfully!");
    }

    /**
     * Callback is called after logged in successfully
     * @param twitter Twitter4J object after logged in successfully, you can use this object to post
     *                tweets, media, etc... About Twitter4J, see more here:
     *                http://twitter4j.org/en/code-examples.html
     */
	protected abstract void onSuccess(Twitter twitter);

    /**
     * Called when login progress couldn't succeed for some reasons
     */
	public abstract void onFailed();

    /**
     * Called when login progress is explicitly cancelled by user
     */
	public abstract void onCancelled();

	/********************************** STATIC METHODS ***********************************/
	/**
	 * @return Returns logged in Twitter4J object for current active session,
     * or null if there is none.
	 */
	public static Twitter getCurrentSession(Context context) {
        String consumer = Util.spGetString(context, Const.SP_TWITTER4A_CONSUMER, null);
        String consumerSecret = Util.spGetString(context, Const.SP_TWITTER4A_CONSUMER_SECRET, null);
        String token = Util.spGetString(context, Const.SP_TWITTER4A_TOKEN, null);
        String tokenSecret = Util.spGetString(context, Const.SP_TWITTER4A_TOKEN_SECRET, null);
        if (token != null && tokenSecret != null && consumer != null && consumerSecret != null) {
            Twitter twitter4j = TwitterFactory.getSingleton();
            try {
                twitter4j.setOAuthConsumer(consumer, consumerSecret);
                twitter4j.setOAuthAccessToken(new AccessToken(token, tokenSecret));
            } catch (IllegalStateException e) {
                if (BuildConfig.DEBUG) e.printStackTrace();
            }
            return twitter4j;
        } else {
            return null;
        }
	}

	/**
	 * @return true if there is an active session, false if there is none.
	 */
	public static boolean isLoggedIn(Context context) {
        String consumer = Util.spGetString(context, Const.SP_TWITTER4A_CONSUMER, null);
        String consumerSecret = Util.spGetString(context, Const.SP_TWITTER4A_CONSUMER_SECRET, null);
        String token = Util.spGetString(context, Const.SP_TWITTER4A_TOKEN, null);
        String tokenSecret = Util.spGetString(context, Const.SP_TWITTER4A_TOKEN_SECRET, null);
        return token != null && tokenSecret != null && consumer != null && consumerSecret != null;
	}
}
