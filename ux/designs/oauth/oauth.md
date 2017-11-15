# OAuth Flow

Updates to the create a connection flow to account of OAuth feature for Twitter and Salesforce.

![Image of choosing connection](img/chooseconnector.png)

In this case, user chooses Twitter Connector.

![Image of choosing object](img/connecttwitter.png)

User clicks on "Connect Twitter" button to start OAuth flow.

![Image of choosing object](img/oauth.png)

1. Clicking on button directs user to service provider in same tab. At end of OAuth flow user is directed back to iPaaS.
1. OAuth will be different for each service but be sure to state what iPaaS will be doing.
1. Buttons to either Authorize app or Cancel. Both buttons will close tab and bring user back to iPaaS
1. iPaaS logo and app information when applicable

![Image of choosing object](img/connectsuccess.png)

If user connects app then validation message should be displayed when user is taken back to ipaas tab.

![Image of choosing object](img/connectionfail.png)

If user cancelled or there was an error in authorization, user will see error message. Connect Twitter button will be enabled in this state.
