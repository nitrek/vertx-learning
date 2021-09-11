 vertx.eventBus().consumer("hello.vertx.addr", function(msg) {
      msg.reply("hello");
    });