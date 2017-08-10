# Github Authentication and Authorization

## Deployment

Syndesis the project files in git. By default we use Github which at the moment is really the only
git provider we support. At deployment time of Syndesis the Administrator creates a "Client App" in
the Github account that will be used by Syndesis, see also http://syndesis.io/quickstart/#github-registered-application
for more info. At that time the GITHUB_CLIENT_ID AND GITHUB_CLIENT_SECRET
settings are made available to the Syndesis installation. For more info on that see 
http://syndesis.io/quickstart/#deployment-instructions

## Keycloak

With the information above a Keycloak provider is created which at login time issues an OAuthToken that
will be set on a header in each request. For this token to be able to be used by git, it needs to have
'repo' scope. This scope info is set when Keycloak handles the oAuth flow with GitHub. See [GitHub documentation]()https://developer.github.com/apps/building-integrations/setting-up-and-registering-oauth-apps/about-scopes-for-oauth-apps/)
for a full explanation of GitHub oAuth scopes.

## Development

At development time you will also need an oauthToken to run to be able to run the GithubServiceITCase test. You can generate a token
using the following curl command (please replace USER_NAME, GITHUB_CLIENT_ID and GITHUB_CLIENT_SECRET) and provide your password when prompted.

...
curl https://api.github.com/authorizations --user "<USER_NAME>" --data '{"scopes":["repo", "delete_repo"],"note":"Syndesis-GitHubServiceITCase", "client_id":"<GITHUB_CLIENT_ID>", "client_secret":"<GITHUB_CLIENT_SECRET>" }'
...

Note that for testing it also needs "delete_repo" scope as set in the command above.

The response is a JSON document from which you can obtain the token. You will need to set this token as a system parameter in your
IDE for this test and when running from maven please add it to your settings.xml by adding:

...
<profiles>
    <profile>
      <id>syndesis</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <github.oauth.token>YOUR_TOKEN_HERE</github.oauth.token>
      </properties>
    </profile>
  </profiles>
...

## CI builds 

For our continuous integration builds we have a dedicated account on Github (syndesis-test@googlegroups.com), ask me (Kurt) for the
password if you need it. You can also ask to be added to the google group if you are a trusted developer. Integration test commits can
be monitored at https://github.com/syndesis-test. I've used the command above to generate an authToken and I have added this to the
settings.xml on jenkins: https://console.rh-idev.openshift.com/console/project/syndesis-ci/browse/secrets/m2-settings so that
the CI build can run the integration tests.

## Expiring tokens

There *could* be an issue with tokens that should be addressed in https://github.com/syndesisio/syndesis-rest/issues/248

## References

[1] OAuth on Github https://developer.github.com/apps/building-integrations/setting-up-and-registering-oauth-apps/

[2] Scopes: https://developer.github.com/apps/building-integrations/setting-up-and-registering-oauth-apps/about-scopes-for-oauth-apps
