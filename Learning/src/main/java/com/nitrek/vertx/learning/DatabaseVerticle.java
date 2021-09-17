package com.nitrek.vertx.learning;

import java.util.function.Function;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

public class DatabaseVerticle extends AbstractVerticle {

  public static final String LIST_ALL_TODOS_ADDR = "com.nitrek.demo.vertx.list_all_todos";
  public static final String ADD_TODO_ADD = "com.nitrek.demo.vertx.add_todo";
  public static final String GET_TODO_BY_ID_ADDR = "com.nitrek.demo.vertx.get_todo_by_id";
  public static final String UPDATE_TODO_ADDR = "com.nitrek.demo.vertx.update_todo";

  private static final String LIST_ALL_TODOS = "SELECT * FROM todos ORDER BY created ASC";
  private static final String GET_TODO_BY_ID = "SELECT * FROM todos WHERE id = ?";
  private static final String UPDATE_TODO = "UPDATE todos SET title = ?, description = ?, due_date = ?, complete = ? WHERE id = ? RETURNING *";
  private static final String ADD_TODO = "INSERT INTO todos (title, description, due_date, complete) VALUES (?, ?, ?, ?) RETURNING *";
  SQLClient client;

  @Override
  public void start(Promise<Void> start) throws Exception {
    doDatabaseMigrations().compose(this::configureSqlClient).setHandler(start::handle);
  }

  Future<Void> configureSqlClient(Void unused) {
    client = JDBCClient.createShared(vertx, config().getJsonObject("db"));
    return Promise.<Void>succeededPromise().future();
  }

  Future<ResultSet> queryWithParams(SQLConnection conn, String query, JsonArray params) {

    return Future.<ResultSet>future(promise -> {
      conn.queryWithParams(query, params, promise);
    });
  }

  Future<JsonObject> mapToFirstResult(ResultSet rs) {

    if (rs.getNumRows() >= 0) {
      return Future.<JsonObject>succeededFuture(rs.getRows().get(0));
    } else {
      return Future.<JsonObject>failedFuture("No rows found");
    }
  }

  Future<JsonArray> mapToJsonArray(ResultSet rs) {
    return Promise.succeededPromise(new JsonArray(rs.getRows())).future();
  }

  /**
   * Uses the {@code loadedConfig} to try to perform the required database schema
   * migrations
   * 
   * @param unused A {@link Void} object which is ignored
   * @return A {@link Future} which indicates the success/failure of this
   *         operation
   */
  Future<Void> doDatabaseMigrations() {
    JsonObject dbConfig = config().getJsonObject("db", new JsonObject());
    String url = dbConfig.getString("url", "jdbc:postgresql://127.0.0.1:5432/introduction");
    String adminUser = dbConfig.getString("admin_user", "introduction");
    String adminPass = dbConfig.getString("admin_pass", "introduction");
    Flyway flyway = Flyway.configure().dataSource(url, adminUser, adminPass).load();

    try {
      flyway.migrate();
      return Promise.<Void>succeededPromise().future();
    } catch (FlywayException fe) {
      return Promise.<Void>failedPromise(fe).future();
    }
  }
}