vertx.eventbus().consumer("hello.vertx.addr").handler({
    msg.reply("Hello ${msg.body()} groovy")
})