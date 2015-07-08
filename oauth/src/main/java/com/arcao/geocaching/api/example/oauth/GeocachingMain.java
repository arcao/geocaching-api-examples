package com.arcao.geocaching.api.example.oauth;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.example.oauth.provider.GeocachingOAuthProvider;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GeocachingMain {
	// Ask Groundspeak for your own keys, see: http://www.geocaching.com/live/apidevelopers/
	// These are my staging keys. Staging server is slow with old database data. Do not use it for
	// production use. This is only for testing purpose!!!
	public static final String OAUTH_CONSUMER_KEY = "9C7552E1-3C04-4D04-A395-230D8931E494";
	public static final String OAUTH_CONSUMER_SECRET = "DA7CC147-7B5B-4423-BCB4-D0C03E2BF685";

	// please use your own callback url if you want capture oauth_verifier parameter
	public static final String OAUTH_CALLBACK_URL = "http://oauth.callback/callback";

	public final static String GEOCACHE_CODE = "GCY81P";

	private static OAuthService createOAuthService() {
		ServiceBuilder serviceBuilder = new ServiceBuilder()
						.apiKey(OAUTH_CONSUMER_KEY)
						.apiSecret(OAUTH_CONSUMER_SECRET)
						.callback(OAUTH_CALLBACK_URL)
						.debug();

		// For staging server has to be used staging OAuth service
		serviceBuilder.provider(GeocachingOAuthProvider.Staging.class);

		return serviceBuilder.build();
	}


	public static void main(String[] args) throws Exception {

		OAuthService oAuthService = createOAuthService();

		System.out.println("Fetching request token from Geocaching...");

		// Groundspeak OAuth service use OAuth 1.0a, so you must always specify your own callback URL
		final Token requestToken = oAuthService.getRequestToken();

		System.out.println("Request token: " + requestToken.getToken());
		System.out.println("Token secret: " + requestToken.getSecret());

		String authUrl = oAuthService.getAuthorizationUrl(requestToken);

		System.out.println("Now visit:\n" + authUrl + "\n... and grant this app authorization");

		// after you allow the access you'll get move to URL like (callback url + authorization parameters):
		// e.g.: http://oauth.callback/callback?oauth_verifier=...&oauth_token=...
		// put oauth_verifier bellow (beware! must be URL decoded)
		System.out.println("Enter the oauth_verifier and hit ENTER when you're done:");

		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		final String oAuthVerifier = br.readLine();

		System.out.println("Fetching access token from Geocaching...");

		Token accessToken = oAuthService.getAccessToken(requestToken, new Verifier(oAuthVerifier));

		System.out.println("Access token: " + accessToken.getToken());
		System.out.println("Token secret: " + accessToken.getSecret());

		// create Geocaching API instance
		final GeocachingApi api = LiveGeocachingApi.Builder.liveGeocachingApi()
                // use staging configuration (mean staging service url)
						.withConfiguration(new DefaultStagingGeocachingApiConfiguration())
                .build();

		// now for Geocaching API use access token as a session token
		// Note: Save this token for later usage, it wouldn't have expire. Token can expire only
		//       in case user forbid your application on Geocaching site.
		api.openSession(accessToken.getToken());

		System.out.println("Sending request to Geocaching...");

		// get a geocache
		final Geocache cache = api.getCache(GeocachingApi.ResultQuality.LITE, GEOCACHE_CODE, 0, 0);

		System.out.println("GetCacheSimple response: " + cache.toString());
	}
}
