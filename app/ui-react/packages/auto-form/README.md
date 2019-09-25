# AutoForm

AutoForm is client-rendered forms driven by JSON

## AutoForm interface
AutoForm uses an IFormDefinition object, which is simply a map of IFormDefinitionProperty objects.  These interfaces are defined [in this file](./src/models.ts).

In Syndesis [this code](../utils/src/autoformHelpers.ts) is used to map the Syndesis map of ConfigurationProperty objects to an IFormDefinitionObject.

## Writing JSON for the Syndesis map of ConfigurationProperty interface

The `propertyDefinitionSteps` field in a `descriptor` can contain one or more elements of objects that contain a `properties` attribute. 

The `properties` attribute is a map that describes each property of the configuration object that the user's extension would require. 

The key for a given element in the `properties` map corresponds to a key in the resulting `configuredProperties` object that will be stored on the step when the user configures the extension in the editor.

Each value of the properties object must at the very least contain a `type` field, which tells the UI what kind of form control to use. Other fields are optional. The fields that can be set on a given value are:

* __type__ - Controls the kind of form control that should be displayed in the UI. The corresponding table shows how the field is mapped:

| Type Value | HTML Form Control |
| ---------- | ----------------- |
| boolean, checkbox | input of type "checkbox" |
| int, integer, long, number |	input of type "number" |
| hidden     |	input of type "hidden" | 
| select     |	input of type "select" |
| duration   |	a custom duration control is used |
| textarea   |	a textarea element is used | 
| text, string, any unknown value |	an input of type "text" is used. |
| array | A special array component is used |

* __required__ - _boolean_: controls whether or not the `required` attribute is set on the control, if true then the user will need to supply a value for the form to successfully validate.
* __secret__ - _boolean_: If specified the form control will be overridden to be an input of type `password`.
* __displayName__ - _string_: sets the label text for the form control, if not set then the raw property name is shown
* __labelHint__ or __labelTooltip__ - _string_: if set, a `?` icon will be displayed next to the label and the value will be shown in a popover when the user clicks on the`?` icon
* __controlHint__ or __controlTooltip__ - _string_: If set the value will be used to set the "title" property of the form control, resulting in a tooltip that will appear when the user hovers the mouse over the control.
* __description__ - _string_: If set, the value will be shown underneath the control, generally used to display a short useful message about the field to the user.
* __placeholder__ - _string_: if set, the value will be used in the form control's `placeholder` property.
* __enum__ - _array_: if set, the "type" will be overridden to be a "select" control. The array should contain a list of objects that contain a "label" and "value" attributes. The "label" attribute is used for each select item's label, and the "value" will be set for each select items' value.
* __dataList__ - _array_: Used for "text" fields, will be used to add a "datalist" element to the form control that provides typeahead support for the user. The array should contain just strings values.
* __multiple__ - _boolean_: If set to "true" for a "select" field (or a field with "enum" set) a multi-select control will be used instead of a select drop-down.
* __max__ - _number_: If set for a "number" field, controls the highest value accepted by the form control.
* __min__ - _number_: if set for a "number" field, controls the lowest value accepted by the form control.
* __rows__ - _number_: if set for a "textarea" field, controls the number of rows initially displayed for the textarea control.
* __cols__ - _number_: if set for a "textarea" field, controls the number of columns initially displayed for the textarea control.
* __order__ - _number_: influences order of controls in the resulting form. The arrangement of controls will be lower values first.

