# Developer Tips

## Testing in your IDE

If you want to run unit tests in your IDE, you will need to spin up a keycloak server first. You can do that by running:

    mvn -Pkeycloak-default process-exec:start

And when you run the test case in the IDE, add the '-Dkeycloak.http.port=8282' JVM system property to the test's execution.
