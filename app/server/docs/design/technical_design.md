# Integration Design

This is going to be a technical description of the concept of **Integration** and how its logical model is implemented.  
It's not a comprehensive definition of how Syndesis work. We are not going to describe system pods and their respective responsibilities.

## Data Model

Be aware that we currently don't have the logical model defined in any document or UML diagram. There is actually an ER Diagram [here](https://github.com/syndesisio/syndesis-rest/blob/master/docs/design/ui-domain-object-map.adoc), but it's not reflecting entirely the current status of the information architecture.

The entry point for our model is the `Integration` [entity](https://github.com/syndesisio/syndesis/blob/master/rest/model/src/main/java/io/syndesis/model/integration/Integration.java), that represents an interaction between systems.

This object encapsulates 2 aspects:
1. information about the **definition** of an `Integration`, i.e. *what it has to do*
2. **runtime information** about a specific Integration instance, for example `getStatus()` tells you if the Integration is *currently deployed and running*

The real definition of what an `Integration` does, is delegated to the following composing entities:

- `Connector`
- `Connection`
- `Action`
- `Step`

![Syndesis Model core concepts](img/syndesisModel-coreConcepts.svg)

##### Connector
Is used to define a set of common properties the user can define on `Connection` entities related this `Connector`.  
Think of `Connector` as a **configurable schema**, used to define the shape of a corresponding `Connection` entity.  
Another responsibility of the `Connector` is to aggregate a list of `Actions`, that are "operations" available to the user.

An example of `Connector` could be a **Twitter** `Connector`, where you define some of the properties that are **required by all** the operations it will provide.  
Properties could be for example `user`, `password`, `token` and so on. Just understand that **you are not setting the values for these keys** here. You are just declaring what are the fields that a specific instance of this item will be asked to fill!  
`Connector` is also associated with set of Maven GAV coordinates, to be referred to.

##### Connection
It can be seen as a concrete instance of a `Connector`.  
On the `Connector` entity you were just declaring the fields that the specific `Connector` supported, here you are passing the corresponding values for those fields.
For example it's here that you specify that the value for the field `user` is `scott` and the value for the `password` field is `tiger`.

##### Action
`Action` represents a specific operation within the scope of a `Connector`.  
In the Twitter example, an operation might be `postMessage()`.  
An `Action` has its own set of of properties.  
In the Twitter example, a property for an action might be `salute`, that a user might fill in with a value like `hello` or `good bye`.  
Role of an `Action` is also to define the link with physical java code library that will provide connectivity with a specific system/technology:  
`CamelConnectorGAV`. In this field, you specify the coordinates of the `Camel Connector` component that this `Action` delegates its logical operations to.


##### Step
`Step` is a **generic entity**, that **represents a pipeline operation** in your `Integration`. It might be a transformation, a logging call or anything else.
Due to all this flexibility `Step` is actually a semantically poor entity. Everything but its type is optional: it can have references to an `Action` or a `Connection` but it doesn't have to. That fact that it needs them or not depends on its `type`, that is its most important information.
An important type of `Step` is `Endpoint`. Unluckily, an enum that lists these kinds does not exist; So the relationship is weak. You have to take care to use a type that the framework is able to understand, because there is no preemptive way to validate your choice.
Steps are declared within an `Integration`.


The overall idea is that your `Integration` define 2 kind of information:
1. a catalog of technologies and allowed operations (`Connectors`,  `Actions`)
2. a pipeline of customized components(`Connection`) and operations you need to perform(`Step`), using the building blocks defined at 1).

To reinforce the idea you can see it as if:

You define some (technology)`Connector`, where you declare all the properties you allow the developer to give a value to, in a corresponding set of `Connection`s. You also link this `Connector` with a series of operations that are tied to this technology, represented by `Action`. In each `Action` you declare also eventual additional properties.  
At last, you detail how the data will flow across `Connections`, and with which eventual transformation, specifying a list of `Steps`.

Notice that despite you define an ordered list of `Steps` to define the business behavior of your `Integration` there is no type checking of any sort between those steps. **It's up to you** being sure that the output of a `Step` is coherent with the input of the following one.


## Integration creation workflow

The entities described and linked above are indeed what the code implementation will use to implement the required behavior. But the idea is that those elements are protected behind some layer of abstraction that prevents you from manipulating them directly.

From a UX point of view, the user is exposed to a graphical UI, dynamically built base on the above models.
Interacting with a set of forms and UI elements, a user completes the definition of a logical integration.

When the user has finished, the UI application produces a `.json` files that corresponds to the user activity and that will be used as the input to generate code on his behalf.

The component responsible of consuming this `.json` is the [`ActivateHandler`](https://github.com/syndesisio/syndesis/blob/master/rest/controllers/src/main/java/io/syndesis/controllers/integration/online/ActivateHandler.java), that at runtime, lives in the `syndesis-rest` pod.  

This component is responsible of 3 main activities:  
1. produce **deplyoment data**
1. **build** the project related to our `Integration`
1. **deploy** an instance of our `Integration`



##### Build
**Building** is actually delegated to an implementation of [`ProjectGenerator`](https://github.com/syndesisio/syndesis/blob/master/rest/project-generator/src/main/java/io/syndesis/project/converter/DefaultProjectGenerator.java) that is responsible of **build-time code generation**.  
With some introspection and templating, a full Java based Apache Maven project is generated. The idea is that this artifact could be built succesfully even outside Syndesis runtime platform, being a fully consistent project.  
The `ActivateHandler`, uses the physical bytes generated by the `ProjectGenerator` to trigger a **build** invocation at **OpenShift** level.  
This means that we leverage **OpenShift** "source to image (s2i)" capability, that accepts a reference to a source code project location, to build the project at source code level and produce also a corresponding **Docker** image including everything needed to spawn a runtime instance of our `Integration`.

##### Deploy
**Deploying** is another responsibility of `ActivateHandler`. After a source code project corresponding to an `Integration` has been generated, built and shared to the interal repository, it still needs to be deployed to run and be available.
This phase does just that: instructs **OpenShift** platform to deploy a live incarnation of our logical `Integration`


##### Deployment Data
This phase actually occur before the `build` one, but it's better understood after what happens in the building phase has been made clearer.  
We have seen that the building phase is all about code generation, to produce an output that will then pass further to a step of code compilation.  
Before the code generation phase, actually some further manipulation happens.  
The idea is to extract references to sensible information, like passwords and externalized properties, from the code itself.  
Secrets and properties will be turned into **OpenShift** level handled resources, allowing them to follow a different lifecycle and letting a platform administrator manage them.  
These resources will be seen by the application as plain files; but in reality they are a logical resource, that **OpenShift** re-exposes as a physical file for easier access.


### Generated project details
The project generated in the `build` phase is an **Apache Camel SpringBoot** based project, packed following an Apache Maven structure.  
The Camel DSL used to define Camel Routes is a custom one, based initially on the [`Funktion`](https://github.com/funktionio/funktion) project.  
It uses a `.yaml` syntax.  
A benefit from using this custom `.yaml` syntax, compared to the already present XML DSL is that it's easier to extend, from a development point of view( since you don't have to modify both and XSD and `CamelNamespaceHandler` classes). One thing that makes easy is for example adding a new `Step` kind.

An important concept here is that the `.yaml` generated from the `Integration` might show a tree shape, **similar but not necessarily identical**, than the `.json` file that had been produced from the **Integration** conceptual model.  
This is because some functionality, like for example **Technical Extensions** might need to create multiple **Steps** from a single definition, thus leading to a structure with a slightly different topology.  
So the `.json` file can be consider a "design time" artifact, while the `.yaml` file can be considered a runtime artifact. At runtime there is only the `.yaml` file.

If you are familiar with Camel framework you can find a correspondence between the following entities:

`Flow` --> `Camel Route`
`Step` --> `Camel Processor` (endpoint, filter, map, etc)

The `.yaml` file is read, at Camel start-time by [`SyndesisRouteBuilder`](https://github.com/syndesisio/syndesis/blob/master/runtime/runtime/src/main/java/io/syndesis/integration/runtime/SyndesisRouteBuilder.java)


## Technical Extension

**Technical Extension** is the feature, in Syndesis, that allows invoking custom code from within your `Integration`.  
The idea is to enable a technical user to augment his `Integration` in a pluggable way, letting the system invoke arbitrary precompiled code that is published to the environment.

From a UX point of view, this happens letting a user uploading a `.jar` file containing classes and dependencies required to run.

The `.jar` that is uploaded has to be built following a specific structure. To aid with the operation of creating this structure, a Maven Plugin has been created: `syndesis-maven-plugin`.  

This plugin has 2 main roles:

- to identify the "kind" of extension and **create a metadata descriptor** according to its findings
- to **package** the extension dependencies in the correct location while **filtering out provided libs**

You can see those 2 logical steps, being explicitly invoked in this sample configuration of `syndesis-maven-plugin`

```xml
<plugin>
    <groupId>io.syndesis</groupId>
    <artifactId>syndesis-maven-plugin</artifactId>
    <version>${syndesis.version}</version>
    <configuration>
        <listAllArtifacts>false</listAllArtifacts>
    </configuration>
    <executions>
        <execution>
        <id>generate</id>
        <goals>
            <goal>generate-metadata</goal>
        </goals>
        </execution>
        <execution>
        <id>repackage</id>
        <goals>
            <goal>repackage-extension</goal>
        </goals>
        </execution>
    </executions>
</plugin>
```

### Supported extensions

An extension can be implemented following different approaches: from the simpler annotated POJO to the usage of Camel aware resources.

Here a brief description of the options a developer has:

- **Annotated POJO**
- **Implementing SyndesisStepExtension Interface**
- **Camel Spring XML DSL**
- **Camel Java DSL**

##### Annotated POJO
This is the simplest form of integration. You are allowed to write your code as you prefer and to be plugged as a Syndesis Extension, you just need to annotate the method you want to be invoked, with ` @SyndesisExtensionAction( id = "myId", name = "myExtension",  description = "bla" )`.  
When you are building your extension project, using `syndesis-maven-plugin`, the plugin will look for annotated methods, and when it will find one, it will generated metadata, that it will put inside a `META-INF/syndesis/syndesis-extension-definition.json` file, defining all the important information, for Syndesis Runtime to recognize your code as a Syndesis Extension and allow you to reference it in the UI.  
From a runtime point of view, methods annotated this way, behaves entirely as Camel `bean` component, that allows you to use both static and non-static methods in your Camel routes.

##### Implementing `SyndesisStepExtension` Interface
You can extend Syndesis implementing `SyndesisStepExtension` functional interface.  
You need to provide the behavior for a single method:
```java
ProcessorDefinition configure(CamelContext context, ProcessorDefinition definition, Map<String, Object> parameters);
```
Two important aspects are present here:
- you have direct access to a `CamelContext` (so you can customize/break Camel engine)
- you have a direct access to `ProcessorDefinition`

`ProcessorDefinition`, might not tell you much, but it's actually your entry point to the full power of Camel Java DSL. With a reference to a `ProcessorDefinition` you have access to all the method you find in the fluent Camel Java DSL, from `split()` to `script()` to `toD()` giving you literally total control to the full Camel expressiveness.  

Your implementation doesn't require di be annotated as a Spring Boot component. The mechanism the runtime will use to load it, is based on a `Class.forName()` invocation.

##### Camel Spring XML DSL
Another option you have is to express your extension as if you were writing a Camel application using Spring XML DSL.  
You can define either a `<camelContext>` node or just a `<routes>` node.  
Just like you can break things in the runtime your extension will run into when implementing `SyndesisStepExtension`, you can end up doing the same here. So the suggested approach is actually to write only `<routes>` definition, as per Camel [documentation](http://camel.apache.org/loading-routes-from-xml-files.html).


##### Camel Java DSL
The last option we describe is Camel Java DSL.  
You can write your `RouteBuilder` or `RouteDefinition`, just like you do when you write Camel Routes directly in Java DSL.  
The only caveat here is that since your extension is going to be loaded as part of a Spring Boot application, you have to annotate your classes in such a way that Spring is able to inject them in its own Spring context.  
These means that you have to annotate your methods with `@Bean` annotation.  
Additionally, since a Spring Boot application auto discovery, only looks for classes within the same package of the entry point class annotated with `@SpringBootApplication`, which in Syndesis case is `io.syndesis.example`, you to do the extra work to "present" your code to the Spring Boot container.  
To do so you have to perform steps:
- define a `spring.factories` class in your `.jar` `META-INF` folder, where you add one or more entries for the key `org.springframework.boot.autoconfigure.EnableAutoConfiguration=` with a fully qualified class name
- provide the implementation of the classes you have specified in the previous step that have also to be annotated with `@Configuration` annotation.

Will this configuration in place, your extension will be picked up correctly by the Spring Boot application you Syndesis Integration lives into.


### What happens in Syndesis when an Extension is uploaded to the platform
A record in a database is created, linking the logical name of our extension to its `.jar`.  
From now on, the UI, when you add a new `Step` will also list all the `Extension` that are present in the database.

### Some consideration
There's currently no explicit metadata about the input and output of an Extension. So it's up to you to expect something that can be handled by Camel implicit converters.  
You have a lot of freedom while writing your extension. And with a lot of freedom you have a lot of risk of breaking things.  
You can probably guess already that if you start spawning new threads here and there you are probably going to corrupt the main engine.  
What it's less intuitive is that you can also break things with what might look as an innocuous Camel Route:
Imagine that you define a route that starts with a `from("twitter:xxxx")`.  On the consuming side there are no problems. As long as you managed to inline all the relevant field in the endpoint, Camel will be able to start a consumer hitting Twitter services.  
The logical error though, arises due to the way Extensions have been implemented.  
The `Step` just before your Extension step, will use the same Camel URI to try to hit pipe it's output to your route.  
This would work without issues with a simpler kind of Camel component, like "direct:" or "seda:" but won't work in the Twitter example, since the URI represents a remote service, not a local one.  
What would happen in this case, is that the previous `Step` will try to write to Twitter!

So, keep this in mind when you design your Techinical Extensions.
