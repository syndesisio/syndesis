## Filter data in integration flow

* Issue: https://github.com/syndesisio/syndesis-project/issues/2
* Sprint: 12

### Story

As a citizen user, I would like to restrict the data that flows through an integration to match a specific set of criteria.  For example, I may have an integration that is triggered every time a tweet mentions my account, but I only want to process tweets that contain the text "Syndesis".

I have two initial requirements for filtering data in an integration:
* Based on content in the body.
* Based on content in a message header.

In both cases, I expect to be able to apply basic string operations to the data to form a filter expression (contains, does not contain, starts with).  For more details on options, see the Operator Support section here:
http://camel.apache.org/simple.html

Similar to logging, the UI should help in creating filter expressions for new/inexperienced users (like through clickable examples or typed form inputs).  The interface should also allow for advanced users to enter free form expressions.

### Tasks

#### syndesis-ui

* Evaluate which editing modes should be supported
  - Plain text fields (body & header filter) for complete control
  - Form like editing / creation of filter rules
* Wonder whether it would be possible to list all available fields at that point e.g. in a drop down box ? (e.g. like for the datamapper ?)
* We could have lines of dropdowns. Would be cool if the second drop down would change choices based on the data type of the given field.

```
[ field_a, field_b, ... ]  [ contains, matches, ... (everythin supported by simple lang) ] [ input text field for value]
```

* Multiple such lines could be combined for a singled filter. These lines would be combined with a logical "AND"
* For logical "OR", multiple filter step should be concatenated in an integration.

#### syndesis-rest

* Check for a "filter" step in funktion.yml
* Add a filter step to domain model (tbv)
* Add REST endpoints for managing the filter step (tbv)
* Evaluate connection to logging (i.e. should dropped message be logged ?)
