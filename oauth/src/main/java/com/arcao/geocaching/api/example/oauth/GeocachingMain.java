package com.arcao.geocaching.api.example.oauth;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.LiveGeocachingApi;
import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.example.oauth.provider.GeocachingOAuthProvider;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class GeocachingMain {
    // Ask Groundspeak for your own keys, see: http://www.geocaching.com/live/apidevelopers/
    // These are my staging keys. Staging server is slow with old database data. Do not use it for
    // production use. This is only for testing purpose!!!
	private static final String OAUTH_CONSUMER_KEY = "9C7552E1-3C04-4D04-A395-230D8931E494";
	private static final String OAUTH_CONSUMER_SECRET = "DA7CC147-7B5B-4423-BCB4-D0C03E2BF685";

    // please use your own callback url if you want capture oauth_verifier parameter
    private static final String OAUTH_CALLBACK_URL = "http://test.arcao.com/oauth.php";

    private final static String GEOCACHE_CODE = "GCY81P";

    private GeocachingMain() {
    }

    private static OAuth10aService createOAuthService() {
        ServiceBuilder serviceBuilder = new ServiceBuilder()
                .apiKey(OAUTH_CONSUMER_KEY)
                .apiSecret(OAUTH_CONSUMER_SECRET)
                .callback(OAUTH_CALLBACK_URL)
                .debug();

        // For staging server has to be used staging OAuth service
        return serviceBuilder.build(new GeocachingOAuthProvider.Staging());
    }


    public static void main(String[] args) throws IOException, GeocachingApiException {

        OAuth10aService service = createOAuthService();

        System.out.println("Fetching request token from Geocaching...");

        // Groundspeak OAuth service use OAuth 1.0a, so you must always specify your own callback URL
        OAuth1RequestToken requestToken = service.getRequestToken();

        System.out.println("Request token: " + requestToken.getToken());
        System.out.println("Token secret: " + requestToken.getTokenSecret());

        String authUrl = service.getAuthorizationUrl(requestToken);

        System.out.println("Now visit:\n" + authUrl + "\n... and grant this app authorization");

        // after you allow the access you'll get move to URL like (callback url + authorization parameters):
        // e.g.: http://oauth.callback/callback?oauth_verifier=...&oauth_token=...
        // put oauth_verifier bellow (beware! must be URL decoded)
        System.out.println("Enter the oauth_verifier and hit ENTER when you're done:");

        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        final String oAuthVerifier = br.readLine();

        System.out.println("Fetching access token from Geocaching...");

        OAuth1AccessToken accessToken = service.getAccessToken(requestToken, oAuthVerifier);

        System.out.println("Access token: " + accessToken.getToken());
        System.out.println("Token secret: " + accessToken.getTokenSecret());

        // create Geocaching API instance
        final GeocachingApi api = LiveGeocachingApi.builder()
                // use staging configuration (mean staging service url)
                .configuration(new DefaultStagingGeocachingApiConfiguration())
                .build();

        // now for Geocaching API use access token as a session token
        // Note: Save this token for later usage, it wouldn't have expire. Token can expire only
        //       in case user forbid your application on Geocaching site.
        api.openSession(accessToken.getToken());

        System.out.println("Sending request to Geocaching...");

        // get a geocache
        final Geocache cache = api.getGeocache(GeocachingApi.ResultQuality.LITE, GEOCACHE_CODE, 0, 0);

        System.out.println("Response: " + cache);
    }
}
