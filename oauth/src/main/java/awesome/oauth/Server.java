package awesome.oauth;
import org.tinylog.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;


public class Server extends AbstractVerticle {

	private final static Vertx vertx = Vertx.vertx(new VertxOptions().setWarningExceptionTime(1000).setWorkerPoolSize(40).setEventLoopPoolSize(4));

	private static final String CLIENT_ID = "MY CLIENT ID";
	private static final String CLIENT_SECRET = "MY SECRET";
	private static final OAuth2ClientOptions credentials = new OAuth2ClientOptions()
	    .setFlow(OAuth2FlowType.AUTH_CODE)
	    .setClientID(CLIENT_ID)
		.setClientSecret(CLIENT_SECRET)
		.setSite("https://idcs-MYSITE.identity.oraclecloud.com")
		.setUseBasicAuthorizationHeader(true)
		.setScopeSeparator("+")
		.setTokenPath("/oauth2/v1/token")
		.setAuthorizationPath("/oauth2/v1/authorize")
		.setUserInfoPath("/oauth2/v1/userinfo");

	public void start() throws Exception {
		final Router router = Router.router(vertx);
		OAuth2Auth oauth2 = OAuth2Auth.create(vertx, credentials);
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)).setAuthProvider(oauth2));
		OAuth2AuthHandler authHandler = OAuth2AuthHandler.create(oauth2);
		authHandler.extraParams(
			new JsonObject()
				.put("redirect_uri", "http://localhost:9012/callback")
				.put("scope", "openid")
				.put("state", "1234")
		);
		authHandler.setupCallback(router.route("/callback"));

	    router.route("/private/*").handler(authHandler);

	    router.route("/private/secret").handler(ctx -> {
	    	User user = ctx.user();
            System.out.println("user man: " + user.toString());
            ctx.response().end("Welcome to the protected resource!");

	     });

        router.get("/").handler(ctx -> {
            ctx.response().putHeader("content-type", "text/html").end("Hello<br><a href=\"/private/secret\">Protected by Oracle IDCS</a>");
        });

	    vertx.createHttpServer().requestHandler(router).listen(9012);
	    Logger.info("server started");
	}
	
}