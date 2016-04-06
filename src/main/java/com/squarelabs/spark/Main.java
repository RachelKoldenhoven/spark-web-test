package com.squarelabs.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.TableRecordImpl;
import org.jooq.util.maven.example.tables.People;
import spark.Spark;

import java.sql.Connection;
import java.sql.DriverManager;

import static spark.Spark.get;

public class Main {
  public static void main(String[] args) throws Exception {
    Class.forName("org.postgresql.Driver");
    String userName = System.getProperty("username");
    String password = System.getProperty("password");
    String url = System.getProperty("url");

    ObjectMapper mapper = new ObjectMapper();
    mapper.setAnnotationIntrospector(new IgnoreInheritedIntrospector());

    Spark.staticFileLocation("/public");

    get("/people", (req, res) -> {
      try (Connection conn = DriverManager.getConnection(url, userName, password)) {
        DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
        Result<Record> result = create.select().from(People.PEOPLE).fetch();
        return result;
      }
    }, mapper::writeValueAsString);

    get("/hello", (req, res) -> "Hello World");
  }

  // http://stackoverflow.com/questions/26945580/jackson-serialization-how-to-ignore-superclass-properties
  private static class IgnoreInheritedIntrospector extends JacksonAnnotationIntrospector {
    @Override
    public boolean hasIgnoreMarker(final AnnotatedMember m) {
      return m.getDeclaringClass() == TableRecordImpl.class || super.hasIgnoreMarker(m);
    }
  }
}