
# Jsonsubtypes Openapi Autogen

A program that uses `javassist-maven-plugin`  to add OpenAPI @Schema annotations by using bytecode manipulation (thanks to Javassist API)

This class:
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = Guitar.class, name = "guitar"),
	@JsonSubTypes.Type(value = Drums.class, name = "drums")
})
class Instrument {}
```

Will be transformed to this class at `process-classes` phase :
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = Guitar.class, name = "guitar"),
	@JsonSubTypes.Type(value = Drums.class, name = "drums")
})
@Schema(  
    discriminatorProperty = "type",  
    discriminatorMapping = {
	 @DiscriminatorMapping(value = "guitar", schema = Guitar.class),
	 @DiscriminatorMapping(value = "drums", schema = Drums.class)
 })  
public class Instrument {
```

## How To Demonstrate
In the repo:

    cd jsonsubtypes-openapi-autogen/
    mvn clean install
    
    cd ../demo-jsonsubtypes-openapi-autogen/
    mvn clean install
    
    java -jar target/demo-jsonsubtypes-openapi-autogen-1.0.0.jar

