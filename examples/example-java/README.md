## Example Java Funktion

This example shows how easy it is to develop a _funktion_ in Java.

<p align="center">
  <a href="http://fabric8.io/">
  	<img src="https://raw.githubusercontent.com/fabric8io/funktion/master/docs/images/icon.png" alt="funktion logo" width="200" height="200"/>
  </a>
</p>

It consists of:

* [funktion.yml](funktion.yml) to define the trigger URL (in this cas HTTP)
* [main() function in Java](src/main/java/io/fabric8/funktion/example/Main.java#L25-L27) to process incoming events

You can then run it via:

```
mvn compile exec:java
```

