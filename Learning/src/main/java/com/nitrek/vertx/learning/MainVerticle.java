package com.nitrek.vertx.learning;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {

    vertx.deployVerticle(new HelloVerticle());
        Router router = Router.router(vertx);

        router.get("/api/v1/hello").handler(this::helloVertx);
        router.get("/api/v1/hello/:name").handler(this::helloNamedVertx);
        int httpPort ;

        try{
            httpPort = Integer.parseInt(System.getProperty("http.port","8080"));
        } catch (NumberFormatException e){
            httpPort = 8080;
        }
        vertx.createHttpServer().requestHandler(router).listen(httpPort);
    

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
