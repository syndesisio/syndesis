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
'repo' scope. This scope info is set when Keycloak interacts with Github. See the table below for a full
list of scopes supported by Github [1] and [2].

|Name|	Description|
|----|-------------|
|(no scope)|	Grants read-only access to public information (includes public user profile info, public repository info, and gists)|
|user	|Grants read/write access to profile info only. Note that this scope includes user:email and user:follow.|
|user:email	|Grants read access to a user's email addresses.|
|user:follow	|Grants access to follow or unfollow other users.|
|public_repo	|Grants read/write access to code, commit statuses, collaborators, and deployment statuses for public |repositories and organizations. Also required for starring public repositories.|
|repo	|Grants read/write access to code, commit statuses, invitations, collaborators, adding team memberships, and deployment statuses for public and private repositories and organizations.|
|repo_deployment	|Grants access to deployment statuses for public and private repositories. This scope is only necessary to grant other users or services access to deployment statuses, without granting access to the code.|
|repo:status	|Grants read/write access to public and private repository commit statuses. This scope is only necessary to grant other users or services access to private repository commit statuses without granting access to the code.|
|delete_repo	|Grants access to delete adminable repositories.|
|notifications	|Grants read access to a user's notifications. repo also provides this access.|
|gist	|Grants write access to gists.|
|read:repo_hook	|Grants read and ping access to hooks in public or private repositories.|
|write:repo_hook	|Grants read, write, and ping access to hooks in public or private repositories.|
|admin:repo_hook	|Grants read, write, ping, and delete access to hooks in public or private repositories.|
|admin:org_hook	|Grants read, write, ping, and delete access to organization hooks. Note: OAuth tokens will only be able to perform these actions on organization hooks which were created by the OAuth App. Personal access tokens will only be able to perform these actions on organization hooks created by a user.|
|read:org	|Read-only access to organization, teams, and membership.|
|write:org	|Publicize and unpublicize organization membership.|
|admin:org	Fully manage organization, teams, and memberships.|
|read:public_key	|List and view details for public keys.|
|write:public_key	|Create, list, and view details for public keys.|
|admin:public_key	|Fully manage public keys.|
|read:gpg_key	|List and view details for GPG keys.|
|write:gpg_key	|Create, list, and view details for GPG keys.|
|admin:gpg_key	|Fully manage GPG keys.|

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
