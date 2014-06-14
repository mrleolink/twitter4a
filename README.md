#Twitter4A - Twitter For Android
Twitter4A is an open-source Android Library that is built on top of <a href="https://github.com/yusuke/twitter4j/">Twitter4J</a>, and provides the easiest way to integrate Twitter API into your Android apps.

##Screenshots
<img src="https://raw.github.com/mrleolink/Twitter4A/master/screenshots/before_login.png" width="180" height="300" alt="before login">&nbsp;&nbsp;
<img src="https://raw.github.com/mrleolink/Twitter4A/master/screenshots/login_dialog.png" width="180" height="300" alt="login dialog">&nbsp;&nbsp;
<img src="https://raw.github.com/mrleolink/Twitter4A/master/screenshots/logging_in.png" width="180" height="300" alt="loggin in">&nbsp;&nbsp;
<img src="https://raw.github.com/mrleolink/Twitter4A/master/screenshots/logged_in.png" width="180" height="300" alt="logged in">

#Changelog
##v1.1
- Integrated gradle (Eclipse is no longer supported, but you always can use Twitter4A with Eclipse by changing folder project structure manually)
- Added session management
- Improved callback mechanism

##Features
- Modal login dialog
- Support session management
- Integrated with Twitter4J via Gradle
- Supports API 8+

**Notice:**
<del>Since this library was created with the main purpose that is provide the easiest way to integrate "Sign in with Twitter" function into your apps, so it doesn't help to manage things like cookie or session, it only provides a nice modal login dialog, some callbacks, and if the login progress is succeeded, it will give you basic user data like username, userID as well as all Twitter4J objects.</del>

Twitter4A now supports session management by static functions:

- Twitter4A.isLoggedIn(Context);
- Twitter4A.getCurrentSession(Context);

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
			protected void onSuccess(Twitter twitter4j) {
				// TODO get user's data or post a twitter on behalf of the logged in user or do whatever you want here
			}
			@Override
			public void onFailed() {
                // TODO This callback is called when login progress couldn't succeed for some reasons
			}

			@Override
			public void onCancelled() {
                // TODO This callback is called when login progress is explicitly cancelled by user
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
