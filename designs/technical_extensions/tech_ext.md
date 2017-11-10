# Technical Extension

This design covers the following workflows:
- Overview (Empty State, List View, and Details View)
- Import new technical extension
- Update an existing technical extension
- Delete a technical extension

## Overview

### Empty State

![Image of choosing connection](img/technical_extensions_empty_state.png)

1. Technical Extensions empty state. Technical Extensions is a tab under Customizations category. Show this page when there are no technical extensions.

2. User can add a new technical extension by clicking the call-to-action button.

### Tech Extension List

![Image of choosing connection](img/technical_extensions_list.png)

1.  Technical Extensions List page offer a list view of available technical extensions. Users can filter this list to find a specific tech extension.

2. Users can import new technical extensions by clicking the “Import Technical Extension” primary action button.  

3. Clicking on an individual row would bring users to a detailed view of the selected technical extension.

4. Information about how many integrations is using a given tech extension.

5. User has the ability to update or delete a technical extension from the list.  

### Tech Extension Details
![Image of choosing connection](img/technical_extensions_details.png)

1.  Users can view details of a technical extension. Tech extension id is included alongside the title of the tech extension.

2. Users can update technical extension by clicking on the Update button.  

3. Table showing usage of the tech extension. Users see a list of integrations that are using this tech extension.

## Import New Technical Extension

![Image of choosing connection](img/tech_ext_list_importnew.png)

User select "Import Technical Extension" to start the workflow.

### Choose File

![Image of choosing connection](img/tech_ext_import_choosefile.png)

1. Breadcrumb located at the top of the screen to help users navigate back to other areas of the application.

2. User can cancel this workflow and go back to the tech extension list page by clicking “Cancel”.

3. Short blurb explaining what this workflow is about and what kind of file is needed for importing tech ext.

4. User can upload a file by choosing file from local file folder.

![Image of choosing connection](img/tech_ext_import_choosefilewindow.png)

### Uploading File

![Image of choosing connection](img/tech_ext_import_uploading.png)

1. Show a spinner when uploading the file.

![Image of choosing connection](img/tech_ext_import_errormsg.png)

1. Validation Error: 1) If the file uploaded does not have the correct file extension; 2) If the file uploaded does not contain sufficient metatdata.

2. User can re-upload a file.

### Review Details and Confirm Import

![Image of choosing connection](img/tech_ext_import_finalcta.png)

1.  The bottom part of the page would appear if user’s file uploaded successfully. Fields would autopopulate with information extracted from the uploaded file. User would be able to see the tech extension ID.

2. User can review Name, Description and steps included in the tech extension file.

3. Select “Import” to confirm importing the tech extension.

## Update Existing Technical Extension

![Image of choosing connection](img/tech_ext_list_update.png)

Select "Update" to update a specific technical extension.

![Image of choosing connection](img/tech_ext_update_choosefile.png)

1. When updating an existing tech extension, user would see the ID associated with this tech extension.

2. Same workflow as importing a new tech extension.

![Image of choosing connection](img/tech_ext_update_validation.png)

1. Show validation message if there's an error.

![Image of choosing connection](img/tech_ext_update_finalscreen.png)

1. The call to action on this screen would be “Update” to confirm the action of updating an existing tech extension.

## Delete a Technical Extension

![Image of choosing connection](img/tech_ext_list_delete.png)

Select "Delete" to delete a specific technical extension.

![Image of choosing connection](img/tech_ext_list_delete_warning.png)

1. If the tech extension is in use when user deletes it, a modal window would appear stating that tech extension is in use and user need to confirm deletion.

2. Provide a link for users to quickly get to the tech extension details page.

3. Use destructive button styling for the “Delete” button.

![Image of choosing connection](img/technical_extensions_details_delete.png)

1.  Delete a tech extension from the details page.

![Image of choosing connection](img/technical_extensions_details_delete_warning.png)

1.  Show a warning message to confirm delete action.  

## Technical Extensions in Create Integration - Add a Step

![Image of choosing connection](img/tech_ext_addstep_filter1.png)

1. Add the ability to filter on the Add a Step page. User can user filter to narrow down the list when looking for a specific filter.

![Image of choosing connection](img/tech_ext_addstep_filter2.png)

1. When user selects Custom Steps, only custom steps are shown on this page.
