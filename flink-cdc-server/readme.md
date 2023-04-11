## FlinkCDC

Upgrading the Debezium Oracle connector from 1.6.4 to 1.9.5 in a FlinkCDC application requires several changes in the application code. Here are some steps that you can follow to refactor your code:

### 1、Upgrade the Debezium Oracle connector dependency version in your FlinkCDC application's pom.xml file to 1.9.5:

```xml
<dependency>
    <groupId>io.debezium.connector</groupId>
    <artifactId>debezium-connector-oracle</artifactId>
    <version>1.9.5.Final</version>
</dependency>
```

### 2、Replace the deprecated DebeziumSourceFunction class with the FlinkDebeziumConsumer class. The FlinkDebeziumConsumer class is a new class introduced in Debezium 1.9.x, which replaces the DebeziumSourceFunction class.

```java
// Old code
DebeziumSourceFunction<String> sourceFunction = new DebeziumSourceFunction<>(
    DebeziumSourceFunction.createDebeziumConfig(
        serverName, 
        "server-id", 
        offsetStrategy
    ), 
    formatFunction, 
    new StringDebeziumDeserializationSchema(), 
    new JSONDebeziumDeserializationSchema(
        TypeInformation.of(new TypeHint<Map<String, ?>>() {})
    )
);

// New code
FlinkDebeziumConsumer<String> debeziumConsumer = FlinkDebeziumConsumer.createJsonConsumeFunction(
    DebeziumConfigProperties.createDebeziumConfig(
        serverName,
        "server-id",
        offsetStrategy
    ),
    formatFunction,
    TypeInformation.of(new TypeHint<Map<String, ?>>() {})
);
```

### 3、Update the Debezium Oracle connector configuration properties according to the new configuration options in Debezium 1.9.x. The configuration properties for the Oracle connector have changed significantly between versions 1.6.4 and 1.9.5, so you will need to update them accordingly. The following code shows some of the configuration properties you might need to change:

```java
Properties debeziumProperties = new Properties();
debeziumProperties.setProperty("connector.class", "io.debezium.connector.oracle.OracleConnector");
debeziumProperties.setProperty("database.hostname", "localhost");
debeziumProperties.setProperty("database.port", "1521");
debeziumProperties.setProperty("database.user", "myusername");
debeziumProperties.setProperty("database.password","mypassword");
debeziumProperties.setProperty("database.server.name", "my-server");
// Additional configuration properties specific to the Oracle connector
debeziumProperties.setProperty("database.dbname", "mydatabase");
debeziumProperties.setProperty("database.out.server.name", "out-server");
```

### 4、Modify the way you create the DebeziumDeserializationSchema object in your application. The Debezium Oracle connector in version 1.9.x introduces a new deserialization schema class DebeziumJsonSerde that replaces the previous JSONDebeziumDeserializationSchema.

```java
// Old code
JSONDebeziumDeserializationSchema deserializationSchema = new JSONDebeziumDeserializationSchema(
    TypeInformation.of(new TypeHint<Map<String, ?>>() {})
);

// New code
DebeziumDeserializationSchema<RowData> deserializationSchema =
    DebeziumJsonSerde.schemaRegistrySerde(
        TypeInformation.of(RowData.class),
        new Configuration(),
        DebeziumJsonSerdeMode.after
    );
```

### 5、Modify the way you create your OracleOffsetContext object. The OracleOffsetContext object is a new class introduced in Debezium 1.9.x that replaces the JdbcOffsetContext class.

```java
// Old code
JdbcOffsetContext offsetContext = new JdbcOffsetContext(
    "{\"server\":\"my-server\",\"ts_sec\":1615501027,\"file\":\"mysql-bin.000003\",\"pos\":303517,\"row\":0,\"snapshot\":{\"status\":\"running\"}}"
);

// New code
OracleOffsetContext offsetContext = new OracleOffsetContext(
    "{\"server\":\"my-server\",\"ts_sec\":1615501027,\"scn\":123456}"
);
```

These are some of the main changes that you need to make in your FlinkCDC application's code to upgrade from Debezium Oracle connector 1.6.4 to 1.9.5. However, there may be other smaller changes required depending on your specific application's configuration and implementation.



## SpringBoot

In Spring Boot, you can use Aspect Oriented Programming (AOP) to modify the front-end parameters before they are passed to the controller. Here's an example of how you can use AOP to modify front-end parameters by making them null:

### 1、Create a new aspect class that implements the org.aspectj.lang.annotation.Aspect interface, and annotate it with @Aspect:

```java
@Aspect
@Component
public class FrontendParamModifier {

}
```

### 2、Define a pointcut that matches the controller method that you want to intercept by using the @Pointcut annotation. You can use the @RequestMapping annotation to match the controller method:

```java
@Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
public void requestMappingPointcut() {
}
```

### 3、Define an advice method that modifies the front-end parameters by making them null. You can use the @Around annotation to define an around advice, which allows you to modify the parameters before the controller method is executed:

```java
@Around("requestMappingPointcut()")
public Object modifyFrontendParams(ProceedingJoinPoint joinPoint) throws Throwable {
    Object[] args = joinPoint.getArgs();
    for (int i = 0; i < args.length; i++) {
        if (args[i] instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) args[i];
            String[] paramNames = request.getParameterNames();
            for (String paramName : paramNames) {
                request.getParameterMap().put(paramName, new String[]{null});
            }
        }
    }
    return joinPoint.proceed();
}
```

### 4、In this advice method, we first get the front-end parameters from the request object, and then loop through them to set each parameter to null. Finally, we call joinPoint.proceed() to proceed with the execution of the controller method.

Make sure that the FrontendParamModifier aspect is enabled by adding the @EnableAspectJAutoProxy annotation to your application's main class:

```java
@SpringBootApplication
@EnableAspectJAutoProxy
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```

With this setup, AOP intercepts every method that is annotated with @RequestMapping. If the method has HTTP request parameters with non-null values, the modifyFrontendParams method modifies them by setting their values to null before the controller method is executed. This way, you can modify the front-end parameters before they are processed by the controller method.2