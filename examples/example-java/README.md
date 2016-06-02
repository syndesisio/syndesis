## Example Java Funktion

This example shows how easy it is to develop a [funktion](https://github.com/fabric8io/funktion/blob/master/README.md) in Java.

<p align="center">
  <a href="http://fabric8.io/">
  	<img src="https://raw.githubusercontent.com/fabric8io/funktion/master/docs/images/icon.png" alt="funktion logo" width="200" height="200"/>
  </a>
</p>

The source code consists of:

* [funktion.yml](src/main/resources/funktion.yml) to define the trigger URL (in this case HTTP)
* [main() function in Java](src/main/java/io/fabric8/funktion/example/Main.java#L25-L27) to process incoming events

You can then run your funktion locally via:

```
mvn spring-boot:run
```

You can then invoke it via your web browser at [http://localhost:8080/?name=Funktion](http://localhost:8080/?name=Funktion)


