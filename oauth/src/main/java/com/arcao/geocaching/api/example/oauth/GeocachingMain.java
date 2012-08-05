package com.arcao.geocaching.api.example.oauth;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.SimpleGeocache;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;

public class GeocachingMain {
	// Please DO NOT USE this keys in production. It's only for testing!!!
	// Ask Groudspeak for your own keys, see: http://www.geocaching.com/live/apidevelopers/
	// put you Geocaching api consumer key here
	public static final String OAUTH_CONSUMER_KEY = "90C7F340-7998-477D-B4D3-AC48A9A0F560";
	// put you Geocaching api consumer secret here
	public static final String OAUTH_CONSUMER_SECRET = "55BBC3C1-24CF-4D1B-B7EC-7A8E75DAB7D1";

	// There are URLs for production mode, in testing mode can be different (see your
	// e-mail from Groundspeak) 
	public static final String OAUTH_REQUEST_URL = "http://www.geocaching.com/oauth/mobileoauth.ashx";
	public static final String OAUTH_ACCESS_URL = "http://www.geocaching.com/oauth/mobileoauth.ashx";
	public static final String OAUTH_AUTHORIZE_URL = "http://www.geocaching.com/oauth/mobileoauth.ashx";

	// please use your own callback url (change protocol for example) if you want
	// capture oauth_verifier
	public static final String OAUTH_CALLBACK_URL = "x-geocaching-api://oauth.callback/callback";

	public final static String CACHE_CODE = "GCY81P";

	public static void main(String[] args) throws Exception {

		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(OAUTH_CONSUMER_KEY, OAUTH_CONSUMER_SECRET);

		OAuthProvider provider = new CommonsHttpOAuthProvider(OAUTH_REQUEST_URL, OAUTH_ACCESS_URL, OAUTH_AUTHORIZE_URL);

		System.out.println("Fetching request token from Geocaching...");

		// Groundspeak OAuth doesn't support OAuth.OUT_OF_BAND callback like other
		// services, so you must always specify your own callback URL
		String authUrl = provider.retrieveRequestToken(consumer, OAUTH_CALLBACK_URL);

		System.out.println("Request token: " + consumer.getToken());
		System.out.println("Token secret: " + consumer.getTokenSecret());

		System.out.println("Now visit:\n" + authUrl + "\n... and grant this app authorization");

		// after you allow the access you'll get move to URL like (callback url + authorization parameters):
		// x-geocaching-api://oauth.callback/callback?oauth_verifier=...&oauth_token=...
		// put oauth_verifier bellow (beware! must be URL decoded)
		System.out.println("Enter the oauth_verifier and hit ENTER when you're done:");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String oAuthVerifier = br.readLine();

		System.out.println("Fetching access token from Geocaching...");

		provider.retrieveAccessToken(consumer, oAuthVerifier);

		System.out.println("Access token: " + consumer.getToken());
		System.out.println("Token secret: " + consumer.getTokenSecret());

		// create Geocaching API instance
		GeocachingApi api = new LiveGeocachingApi();

		// now for Geocaching API use access token as a session token
		// Note: Save this token for later usage, it wouldn't have expire. Token can expire only 
		//       in case user forbid your application on Geocaching site.
		api.openSession(consumer.getToken());

		System.out.println("Sending request to Geocaching...");

		// get a cache
		SimpleGeocache cache = api.getCacheSimple(CACHE_CODE);

		System.out.println("GetCacheSimple response: " + cache.toString());
	}
}
