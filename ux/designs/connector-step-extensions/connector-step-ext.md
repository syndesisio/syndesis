# Extension

This design covers the following workflows:
- Overview (Empty State, List View, and Details View)
- Import new extension
- Update an existing extension
- Delete a extension

## Overview

### Empty State

![Image of extensions empty state](img/extensions_empty_state.png)

1. 	Extensions empty state. Extensions is a tab under Customizations category. Show this page when there are no extensions.

2. 	User can add a new extension by clicking the call-to-action button.

### Extension List

![Image of extension list view](img/extensions_list.png)

1.	Extensions List page offer a list view of available extensions. Users can filter this list to find a specific extension.      

2.	Users can import new extensions by clicking the “Import Extension” primary action button.

3.	Clicking on an individual row would bring users to a detailed view of the selected extension.

4.	Specifying extension types.

5.	Information about how many integrations is using a given extension.

6.	User has the ability to update or delete an  extension from the list.  

### Extension Details

![Image of step extension details](img/extensions_details_step.png)

1.	Users can view details of a step extension. Extension id is included alongside the title of the extension.

2.	Users can update extension by clicking on the “Update” button.  

3.	Type information listed in Overview to help users differentiate Step Extension and Connector Extension

4.	Supported steps are listed for easy reference.

5.	Table showing usage of the extension. Users see a list of integrations that are using this extension.

![Image of step extension details](img/extensions_details_connector.png)

1.	Type information listed in Overview to help users differentiate Step Extension and Connector Extension.

2.	Supported actions are listed for Connector Extensions.

## Import New Extension

![Image of import new extension](img/extensions_importnew.png)

	1.	Users select “Import Extension” to start the workflow.

### Choose File

![Image of choose file to import](img/ext_import_choosefile.png)

1. Breadcrumb located at the top of the screen to help users navigate back to other areas of the application.

2. User can cancel this workflow and go back to the extension list page by clicking “Cancel”.

3. Short blurb explaining what this workflow is about and what kind of file is needed for importing extension.

4. User can upload a file by choosing file from local file folder.

![Image of choose file window](img/ext_import_choosefilewindow.png)

### Uploading File

![Image of uploading](img/ext_import_uploading.png)

1. Show a spinner when uploading the file.

![Image of import error message](img/ext_import_errormsg.png)

1. 	Validation Error: 1) If the file uploaded does not have the correct file extension; 2) If the file uploaded does not contain sufficient metatdata.

2. 	User can re-upload a file.

### Review Details and Confirm Import

![Image of import final screen](img/ext_import_finalcta.png)

1. 	The bottom part of the page would appear if user’s file uploaded successfully. Fields would auto-populate with information extracted from the uploaded file. User would be able to see the extension ID.

2. 	User can review Name, Description, Type and steps or actions included in the extension file. If importing a Connector Extension, the type would appear as Connector Extension.

3. 	Select “Import” to confirm importing the extension.  

## Update Existing Extension

![Image of update extension from list view](img/extensions_list_update.png)

Select "Update" to update a specific extension.

![Image of choose file to update](img/ext_update_choosefile.png)

1. 	When updating an existing extension, user would see the ID associated with this extension.

2. 	Same workflow as importing a new extension.

![Image of update error message](img/ext_update_validation.png)

1. Show validation message if there's an error.

![Image of update review screen](img/ext_update_finalscreen.png)

1. The call to action on this screen would be “Update” to confirm the action of updating an existing extension.

## Delete an Extension

![Image of delete extension from list view](img/extensions_list_delete.png)

Select "Delete" to delete a specific extension.

![Image of list view delete warning](img/extensions_list_delete_warning.png)

1. 	If the extension is in use when user deletes it, a modal window would appear stating that extension is in use and user need to confirm deletion.

2. 	Provide a link for users to quickly get to the extension details page.

3. 	Use destructive button styling for the “Delete” button.

![Image of details view delete](img/extensions_details_delete.png)

1.  Delete an extension from the details page.

![Image of details view delete warning](img/extensions_details_delet_warning.png)

1.  Show a warning message to confirm delete action.  

## Extensions in Create Integration - Add a Step

![Image of choosing connection](img/tech_ext_addstep_filter1.png)

1. Add the ability to filter on the Add a Step page. User can user filter to narrow down the list when looking for a specific filter.

![Image of choosing connection](img/tech_ext_addstep_filter2.png)

1. When user selects Custom Steps, only custom steps are shown on this page.
