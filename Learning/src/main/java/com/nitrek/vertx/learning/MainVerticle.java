package com.nitrek.vertx.learning;



import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

        vertx.deployVerticle(new HelloVerticle());
        //vertx.deployVerticle("Hello.groovy");
        //vertx.deployVerticle("Hello.js");

        SessionStore sessionStore = LocalSessionStore.create(vertx);
        Router router = Router.router(vertx);
        router.route().handler(LoggerHandler.create());

        router.route().handler(SessionHandler.create(sessionStore));
        router.route().handler(CorsHandler.create("localhost"));
        router.route().handler(CSRFHandler.create("rejbwjhebfejgffhkvfkhv"));

    router
        .route()
        .handler(
            ctx -> {
              String authToken = ctx.request().getHeader("AUTH_TOKEN");
              if(authToken != null && authToken.contentEquals("authToken")) {
                  ctx.next();
              } else {
                  ctx.response().setStatusCode(401).setStatusMessage("UNAUTHORISED").end();
              }
            });
        router.get("/api/v1/hello").handler(this::helloVertx);
        router.get("/api/v1/hello/:name").handler(this::helloNamedVertx);

        router.route().handler(StaticHandler.create("web"));
        startApp(startPromise,router);


}

void startApp(Promise<Void> startPromise, Router router){
    ConfigStoreOptions options = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path", "config.json"));
    ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
            .addStore(options);
    ConfigRetriever configRetriever = ConfigRetriever.create(vertx,configRetrieverOptions);

    configRetriever.getConfig( asyncResults -> {
                if (asyncResults.succeeded())
                {
                    JsonObject config = asyncResults.result();
                    JsonObject http = config.getJsonObject("http");
                    int httpPort = http.getInteger("port");
                    System.out.println("starting now got port"+httpPort);
                    vertx.createHttpServer().requestHandler(router).listen(httpPort);
                    startPromise.complete();
                }
                else{
                    startPromise.fail("Unable to load configuration");
                }
            }

    );
}
void helloVertx(RoutingContext ctx){
    vertx.eventBus().request("hello.vertx.addr","",reply->{
        ctx.response().end(reply.result().body().toString());                
    });
}

void helloNamedVertx(RoutingContext ctx){
    String name = ctx.request().getParam("name");
    vertx.eventBus().request("hello.named.addr",name,reply->{
        ctx.response().end(reply.result().body().toString());
    });
}

}
