package net.leolink.android.twitter4a;
import net.leolink.android.twitter4a.utils.Constants;
import net.leolink.android.twitter4a.widget.LoginDialog;
import net.leolink.android.twitter4a.widget.Spinner;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;

public abstract class Twitter4A {
	public final static String TAG = "twitter4a";

	// twitter4j's objects
	private Twitter mTwitter;
	private RequestToken mTwitterRequestToken;
	private AccessToken mTwitterAccessToken;
	private User mTwitterUser;

	// twitter4a's objects
	private String mConsumerKey;
	private String mConsumerSecret;
	private Activity mContext;
	private boolean isLoggedIn = false;
	private boolean isLoggingIn = false;

	// Constructor
	public Twitter4A(Activity context, String consumerKey,
			String consumerSecret) {
		mContext = context;
		mConsumerKey = consumerKey;
		mConsumerSecret = consumerSecret;
	}

	public void login() {
		if (!isLoggingIn) {
			// run an AsyncTask to get authentication URL, then open login dialog
			new AsyncTask<Void, String, Void>() {
				@Override
				protected Void doInBackground(Void... voids) {
					ConfigurationBuilder builder = new ConfigurationBuilder();
					builder.setOAuthConsumerKey(mConsumerKey);
					builder.setOAuthConsumerSecret(mConsumerSecret);
					twitter4j.conf.Configuration configuration = builder.build();
		
					TwitterFactory factory = new TwitterFactory(configuration);
					mTwitter = factory.getInstance();
					
					try {
						mTwitterRequestToken = mTwitter.getOAuthRequestToken(Constants.TWITTER_CALLBACK_PREFIX);
						publishProgress(mTwitterRequestToken.getAuthenticationURL());
					} catch (TwitterException e) {
						e.printStackTrace();
					}					
					
					return null;
				}
				
				@Override
				protected void onProgressUpdate(String... values) {
					// open LoginDialog to let user login to Twitter
					if (!mContext.isFinishing())
						new LoginDialog(mContext, Twitter4A.this, values[0]).show();			
				}			
			}.execute();

			// Prevent calling login() function consecutively which leads to
			// multiple LoginDialogs are opened at the same time
			isLoggingIn = true;
		}
	}
	
	// get data after login successfully
	public void handleSuccessfulLogin(String uri) {
		final Uri mUri = Uri.parse(uri);
		final String verifier = mUri.getQueryParameter(Constants.URL_TWITTER_OAUTH_VERIFIER);
		
		// Because this task need to using network which cannot be run on UI
		// thread since Android 3.0 (maybe equivalent to API 11), so I need to
		// use AsyncTask here!
		new AsyncTask<Void, Void, Void>() {
			private Spinner spinner;

			protected void onPreExecute() {
				spinner = new Spinner(mContext);
				spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
				spinner.setCancelable(true);
				spinner.show();
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				try {
					mTwitterAccessToken = mTwitter.getOAuthAccessToken(mTwitterRequestToken, verifier);
					mTwitterUser = mTwitter.showUser(mTwitterAccessToken.getUserId());
				} catch (TwitterException e) {
					// call loginFailedCallback
					loginFailedCallback();
					
					e.printStackTrace();
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				if (spinner.isShowing()) {
					// enable cookie
					CookieManager.getInstance().setAcceptCookie(true);
	
					// dismiss the spinner
					spinner.dismiss();
					
					// if everything is okay, set isLoggedIn = true
					isLoggedIn = true;
					
					// call the callback
					loginCallback();
				} else { // if spinner is explicitly cancelled by user
					// remove everything
					mTwitter = null;
					mTwitterAccessToken = null;
					mTwitterRequestToken = null;
					mTwitterUser = null;
	
					// call loginFailedCallback
					loginFailedCallback();			
				}
			}
		}.execute();
	}

	// logging out
    public void logout() {
		// remove all Twitter4J objects
		mTwitter = null;
		mTwitterAccessToken = null;
		mTwitterRequestToken = null;
		mTwitterUser = null;
		
		isLoggedIn = false;
		
		// inform programmers :)
		Log.d(TAG, "Logged out successfully!");

     	// call the logout call back
    	this.logoutCallback();   		
    }
    
    // this method is called after login successfully
	protected abstract void loginCallback();
	
	// this method is called after logout successfully
	protected abstract void logoutCallback();
	
	// this method is called when login progress couldn't succeed for some reasons
	public void loginFailedCallback() {
		Log.e(TAG, "Login failed!");
	}
	
	public void setLoggingIn(boolean isLoggingIn) {
		this.isLoggingIn = isLoggingIn;
	}
	
	/********************************** API ***********************************/
	// Get Twitter4J's objects -> For people who want to more than just a login
	/**
	 * @return Twitter object of Twitter4J library
	 */
	public Twitter getTwitter4J() {
		return mTwitter;
	}
	/**
	 * @return AccessToken object of Twitter4J library
	 */
	public AccessToken getTwitter4JAccessToken() {
		return mTwitterAccessToken;
	}
	/**
	 * @return User object of Twitter4J library
	 */
	public User getTwitter4JUser() {
		return mTwitterUser;
	}
	/**
	 * @return null if not logged in yet, otherwise return token of the current session
	 */
	public String getToken() {
		if (mTwitterAccessToken != null)
			return mTwitterAccessToken.getToken();
		else
			return null;
	}
	/**
	 * @return null if not logged in yet, otherwise return secret token of the current session
	 */
	public String getTokenSecret() {
		if (mTwitterAccessToken != null)
			return mTwitterAccessToken.getTokenSecret();
		else
			return null;
	}
	
	// Get basic user data -> For people who just want a login to get some basic data
	/**
	 * @return return current login state
	 */
	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	/**
	 * @return userID of the current logged in user 
	 */
	public long getUserID() {
		if (mTwitterUser != null)
			return mTwitterUser.getId();
		else
			return 0;
	}
	/**
	 * @return user name of the current logged in user
	 */
	public String getUsername() {
		if (mTwitterUser != null)
			return mTwitterUser.getName();
		else
			return null;
	}
	/**
	 * @return original profile picture URL of the user of the current logged in user 
	 */
	public String getProfilePicURL() {
		if (mTwitterUser != null)
			return mTwitterUser.getOriginalProfileImageURL();
		else
			return null;
	}
}
