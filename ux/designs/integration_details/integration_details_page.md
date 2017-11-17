# Integration Details Page

The designs covers the following user interactions:
- Integration Details Page basic layout
  - Expandable row for integration history (versions)
  - Kebab menu
  - Integration visualization (normal vs. many steps/connections)
- Creating/Replacing integration draft and publish a version from integration history
  - Status indicators at row level
  - Having notifications in place for publish action
  - Modal for confirming user actions

## Integration Details Page

### No Draft  

![no draft section](img/integration_details_page_rework_11.16_no_draft.png)

1. If there’s no draft for an integration. The draft section would not be shown on this page.

2. Users would be able to “Create Draft” from the current version.

### Basic layout

![basic layout](img/integration_details_page_rework_11.16_genericlayout.png)

1. Moved status label to the top section right next to Integration Name. Using “check mark” icon to indicate this integration is active.

2. Moving Draft out of the integration history. Users would be able to “Publish” and “Edit” the draft.

3. Added row indicator (green bar) and checkmark icon to indicate active and running version.

![many steps and connections](img/integration_details_page_rework_11.16_many_stepsconnections.png)

1. Showing an integration that has many connections and steps.

### Expanding a row

![expandable row and scrolling](img/integration_details_page_rework_11.16_expand_scroll.png)

1. When expanding a row item, show more details about that version.

  - Start Date/Time
  - Las Run
  - Last Modified: user name and timestamp
  - Modification Notes: works like commit message or note to self

2. Users are able to scroll on this page. When scrolling, the whole page scrolls.


## Interacting with the page

### Kebab menu

![kebab menu](img/integration_details_page_rework_11.16_kebab.png)

1. Breadcrumb & toolbar level kebab, users are able to:

  - Stop Integration
  - Replace Draft
  - View Logs
  - Delete Integration (all versions)

  When selecting View Logs, users would be directed to the Logs page.  

2. For the active row, kebab menu includes:

  - Replace Draft
  - Stop Integration


3. Row level kebab menu includes:

  - Replace Draft
  - Publish


### Modal for confirming actions

![replace draft modal](img/integration_details_page_rework_11.16_replaceadraft.png)

Once users click on “Replace Draft”, display a modal to notify users and to confirm the replace draft action.


![publish version modal](img/integration_details_page_rework_11.16_publishaversion.png)

If users click on “Publish”, display a modal to notify users what would happen and ask them to confirm their action.


### Status indicators

![spinning up](img/integration_details_page_rework_11.16_spinning_up.png)

1. In between state - showing the current publish version is still active, and a new version is spinning up.

2. The version spinning up would have a spinner indicator.

![being published](img/integration_details_page_rework_11.16_being_published.png)

In this case, the published version is being “decommissioned” and the new version is being published.

1. Show spinner for overall integration status.

2. Show spinners for both the “decommissioning” version and the being published version.


### Notifications (alerts)

![warning message](img/integration_details_page_rework_11.16_warning.png)

1. Display a warning message when a published integration is outdated.

2. Warning message includes a link to give users “more informatin”.

3. Users can “republish integration” from the warning message.

![error message](img/integration_details_page_rework_11.16_error.png)

1. When an integration is not running, display an error message and change indicators accordingly.
