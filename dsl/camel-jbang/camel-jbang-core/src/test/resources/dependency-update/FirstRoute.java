import org.apache.camel.builder.RouteBuilder;

class FirstRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("sql:SELECT * FROM FOO")
                .log("${body");
    }
}