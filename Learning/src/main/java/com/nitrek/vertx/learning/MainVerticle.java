package com.nitrek.vertx.learning;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
    DeploymentOptions options = new DeploymentOptions()
    .setWorker(true)
    .setInstances(2);
    vertx.deployVerticle("com.nitrek.vertx.learning.HelloVerticle",options);
        Router router = Router.router(vertx);

        router.get("/api/v1/hello").handler(this::helloVertx);
        router.get("/api/v1/hello/:name").handler(this::helloNamedVertx);

        vertx.createHttpServer().requestHandler(router).listen(8080);
    

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
