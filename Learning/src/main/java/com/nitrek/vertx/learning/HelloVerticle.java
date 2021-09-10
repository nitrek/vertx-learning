package com.nitrek.vertx.learning;

import java.util.UUID;

import io.vertx.core.AbstractVerticle;

public class HelloVerticle extends AbstractVerticle {

  String verticleId = UUID.randomUUID().toString();
  @Override
  public void start(){

    vertx.eventBus().consumer("hello.vertx.addr", msg-> {

      msg.reply("hello");
    });

    vertx.eventBus().consumer("hello.named.addr", msg-> {
      String name = (String)msg.body();
      msg.reply(String.format("hello %s from %s",name,verticleId));
    });
  }


  
}
