#Twitter4A - Twitter For Android
This is an open-source Android Library that is built on top of <a href="https://github.com/yusuke/twitter4j/">Twitter4J</a>.<br />
Twitter4A provides the easiest way to integrate "Sign in with Twitter" function into your apps and still gives you all what Twitter4J can do.

##Screenshots
<img src="https://raw.github.com/mrleolink/Twitter4A/master/screenshots/before_login.png" width="180" height="300" alt="before login">&nbsp;&nbsp;
<img src="https://raw.github.com/mrleolink/Twitter4A/master/screenshots/login_dialog.png" width="180" height="300" alt="login dialog">&nbsp;&nbsp;
<img src="https://raw.github.com/mrleolink/Twitter4A/master/screenshots/logging_in.png" width="180" height="300" alt="loggin in">&nbsp;&nbsp;
<img src="https://raw.github.com/mrleolink/Twitter4A/master/screenshots/logged_in.png" width="180" height="300" alt="logged in">

##Features
 - Modal login dialog
 - Callbacks are provided: `loginCallback`, `logoutCallback` and `loginFailedCallback`
 - Full-integrated with the latest stable version of Twitter4J - <a href="http://twitter4j.org/archive/twitter4j-3.0.5.zip">twitter4j-3.0.5</a>
 - Supports API 8+

**Notice:** Since this library was created with the main purpose that is provide the easiest way to integrate "Sign in with Twitter" function into your apps, so it doesn't help to manage things like cookie or session, it only provides a nice modal login dialog, some callbacks, and if the login progress is succeeded, it will give you basic user data like username, userID as well as all Twitter4J objects.

##Setup
In Eclipse, import the library as an Android library, then right click on your project -> Properties -> Android -> Add -> choose Twitter4A -> OK.<br />
That's all, you are ready to use!

##Usage
1. Go to Twitter Dev page: https://dev.twitter.com/apps, create an app then grab the `Consumer Key` and `Consumer Secret`
2. Include *uses-permission* `android.permission.INTERNET` to your `AndroidManifest.xml`.
3. Create a `Twitter4A` object with `Consumer Key` and `Consumer Secret`, then fill `loginCallback` and `logoutCallback` as your needs and override `loginFailedCallback` if you want. But notice that:
 - `loginCallback` is only if login progress are succeeded, otherwise `loginFailedCallback` will be called.
4. Call `login` or `logout` from `Twitter4A` function whenever you want.
 
####Code Example
```java
	// grab these keys at https://dev.twitter.com/apps
    public final static String TWITTER_CONSUMER_KEY = "RLAsuMh1IkwQlTXTqTBig";
    public final static String TWITTER_CONSUMER_SECRET = "DqeRw9BuRHKDcsCyQAttqJEZrk7q0zBIjP2kXvzJI";
    private Twitter4A mTwitter4a;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter4a_example);
		
		// create an Twitter4a object
		mTwitter4a = new Twitter4A(this, TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET) {
			@Override
			protected void logoutCallback() {
				// TODO write your logout callback here
			}
			@Override
			protected void loginCallback() {
				// TODO write your login callback here
			}
			@Override
			public void loginFailedCallback() {
				super.loginFailedCallback();
				// TODO You also can override loginFailedCallback if you want
			}
		};
		
		// then use the Twitter4A object to log in/out
		findViewById(R.id.twitter_login_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!mTwitter4a.isLoggedIn())
					mTwitter4a.login();
				else
					mTwitter4a.logout();
			}
		});
	}
```


##API
###Main API
**`Twitter4A` class**

 - `public void login()`
 - `public void logout()`
 - `protected abstract void loginCallback()`
 - `protected abstract void logoutCallback()`
 - `public void loginFailedCallback()`

###Other Getters that you may want
```java
	/************* Get Twitter4J's objects -> For people who want to more than just a login *************/
	/**
	 * @return Twitter object of Twitter4J library
	 */
	public Twitter getTwitter4J() {//...}
	
	/**
	 * @return AccessToken object of Twitter4J library
	 */
	public AccessToken getTwitter4JAccessToken() {//...}
	
	/**
	 * @return User object of Twitter4J library
	 */
	public User getTwitter4JUser() {//...}
	
	/**
	 * @return null if not logged in yet, otherwise return token of the current session
	 */
	public String getToken() {//...}
	
	/**
	 * @return null if not logged in yet, otherwise return secret token of the current session
	 */
	public String getTokenSecret() {//...}
	
	
	
	/************* Get basic user data -> For people who just want a login to get some basic data *************/
	/**
	 * @return return current login state
	 */
	public boolean isLoggedIn() {//...}

	/**
	 * @return userID of the current logged in user 
	 */
	public long getUserID() {//...}
	
	/**
	 * @return user name of the current logged in user
	 */
	public String getUsername() {//...}
	
	/**
	 * @return original profile picture URL of the user of the current logged in user 
	 */
	public String getProfilePicURL() {//...}
```


##Licenses

    Copyright 2013 Leo Link

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

Portions of LoginDialog.java:

    Copyright 2013 Facebook
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
