## UI: Refactoring and bug fixing work

* **Patternfly** toolbar component in use on integration, connection and global settings list pages, type in a search phrase and hit enter and the component behaves per the patternfly standard.
* Adopted patternfly toast notification component for notifications
  - **Notification** occurs when the user’s token expires, prompting to refresh the page
* Create **Connection wizard** improvements:
  - Modal dialog prompt if the user clicks ‘cancel’ to avoid losing work
* **Integration editor** state improvements:
  - creating a step disables the trash icons and links in the left-hand navigation which led to data consistency issues
  - Breadcrumbs now have ‘Home’ and ‘Integrations’ so you can get to either the dashboard or the integration list
  - Both ‘Save as Draft’ and ‘Publish’ are available on the save or add step page
  - Integration name above the flow view is editable
  - No more empty tooltips (hopefully)
  - When revisiting a step, the button reads ‘Done’ instead of ‘Next’
* **Data Mapper** host improvements
  - Passes along all available input types to the data mapper component
  - Passes the correct output type to the data mapper component
  - Data mapper no longer shows duplicate input/output types
* **Global settings page** added
  - Currently only has OAuth client tab, can support other tabs
* **Basic filter page**
  - Should load and save it’s data from the integration
  - Should allow you to add/remove additional rules
  - Should have typeahead data for the field
  - Fetches the operators from the server
* Main navigation on the side hides itself properly again
* Edit action added to kebab menu in connection list that links the user directly to editing the connection
* Can delete ‘in progress’ integrations
* Forms updated to better match patternfly standard, tooltip help on fields used instead of placeholder text.
