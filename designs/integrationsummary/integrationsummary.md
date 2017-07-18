# Integration Summary Page

When a user create an integration and deploys or saves it, user will be taken to this page to review summary of integration. User can also get back to this page by clicking on the integration from the Integration list page.

![Image of choosing connection](img/integrationsummary.png)

1. **Description**: Integration visual summary shown in horizontal orientation. Description shown in full.
1. Label showing if integration is active or not. Button shown under label to change status of integration.
1. If a draft exists, it will be shown at top of table with date it was last edited. Action button will be to Edit Draft.
1. **History**: shows the history of the integration and version details.
  - With each new deployment of an integration, a new version is created.
  - Only one version is running at a given time (shown in table).
  - User can also choose to deploy previous versions (but can't edit older versions).
  - User can also Duplicate versions to use as a blueprint.
  - For the version that is currently running, only Duplicate button will be available along with label showing which version is running.
1. Delete integration button can be found at the bottom. Will delete whole integration including all versions and a draft if it exists.

![Image of choosing object](img/integrationsummary2.png)

1. User can also choose to edit integration from action bar found at top of page. If no draft exists, selecting this button will create a new draft. If a draft exists, selecting this button will open existing draft.
1. Shows how the other integration status's would appear. If user selects the "Run Integration" button when it's inactive, it will deploy the latest integration version
1. If integration is inactive, there will be no label showing which version is running. User can choose to deploy any version.
