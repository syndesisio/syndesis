catalog-example
---------------

A little example to demonstrates the CamelCatalog and CamelCatalogConnector.

This example will show how you can add custom connectors and download them from Maven, by providing the Maven GAV
for these connectors.

Then the example shows how you can use the catalog to provide a set of options to configure the connector,
and then generate the Camel endpoint uri, that should be used at runtime in Camel routes.

To run this example type

    mvn compile exec:java
    
Check the source code how it works.
    
   
