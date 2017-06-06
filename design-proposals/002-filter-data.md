## Filter data in integration flow

* Issue: https://github.com/syndesisio/syndesis-project/issues/2
* Sprint: 12
* Affected Repos:
  - syndesis-rest
  - syndesis-ui

### User Story

As a citizen user, I would like to restrict the data that flows through an integration to match a specific set of criteria.  For example, I may have an integration that is triggered every time a tweet mentions my account, but I only want to process tweets that contain the text "Syndesis".

I have two initial requirements for filtering data in an integration:
* Based on content in the body.
* Based on content in a message header.

In both cases, I expect to be able to apply basic string operations to the data to form a filter expression (contains, does not contain, starts with).  For more details on options, see the Operator Support section here:
http://camel.apache.org/simple.html

Similar to logging, the UI should help in creating filter expressions for new/inexperienced users (like through clickable examples or typed form inputs).  The interface should also allow for advanced users to enter free form expressions.

### Domain

#### UI defined filter (typed)

For the typed definition of a filter, filtering breaks down in several sub-concepts:

* A **Filter Step** contains one or more **Filter Rules**. Multiple **Filter Rules** are combined with logical `OR`.
* A **Filter Rule** contains one or more **Filter Statements**. The 

A _Filter Step_ is a regular `Step` with a type "filter". 
_Filter Rules_ and _Filter Statements_ are part of the configuration of this step and stored in the properties of a step (_TODO: The domain object `IntegrationConnectionStep_ seems to be supeflous).

The lowest building block is a a `FilterStatement`. It has the following properties:

* **type** : On which part of the message to apply the filter. Can be either "header", "body" or "properties".
* **key** : The key to use to extract a message value (e.g. header or property name). The key is empty when type is equals to body.
* **op** : The operator to use for the filter
* **value** : The value used by the operator to determine whether a filter applies.

"type" and "key" could also be combined to a selector (which e.g. then would have the format `type.key`)

A `FilterRule` is then a list of `FilterStatements`.
The configuration of a `FilterStep` is then a list of `FilterRules`.

For example, the following configuration

```yaml
- - type: "body"
    op: "contains"
    value: "antman"
  - type: "header"
    key: "region"
    op: "=~"
    value: "asia"
- - type: "body"
    op: "regex"
    value: "bat(wo)?man"
  - type: "header"
    key: "publisher"
    op: "=~"
    value: "DC Comics"
```

would be stored as a property "filter" on the integration step. It translates later to simple lang expression

```
${body} contains "antman" && ${in.header.region} =~ "asia" || ${body} regex "bat(wo)?man" && ${in.header.publisher} =~ "DC Comics"
```

Since `&&` has always higher precedence than `||` this works even when the simple language [does not support parentheses](http://camel.apache.org/simple.html)

#### Text based filter (freeform)

It should be possible to add a simple expression directly as text. Ideally there should be some sort of intellisense, but in the first step its a plain text input. However the expression needs to be validated before its stored in the DB. In the examples above the simple expression would be added directly. 

### Persistence

Currently the configuration of a step is a plain properties map object in JSON. Each FilterStep supports the following properties:

* **type** : Either "form" or "text" for the two modes described above.
* **simple** : The simple filter expression to apply.
* **rules** : The rules as defined above for a "form" typed integration.

In the case of a "form" filter, the "simple" expression is calculated when the step is persisted.

Example for a persistent integration step:

```json
{ 
  "id": "1",
  "stepKind": "filter",
  "configuredProperties": {
    "type": "form",
    "simple" : "${body} contains \"antman\" || ${in.header.publisher} =~ \"DC Comics\""
    "rules" : [
      [ 
         { 
           "type": "body",
           "op": "contains",
           "value": "antman"
         }
      ],
      [
         { 
           "type": "header",
           "op": "=~",
           "value": "DC Comics"
         }
      ]      
    ]
  },
  "id": "2",
  "stepKind": "filter",
  "configuredProperties": {
    "type": "text",
    "simple": "${in.header.region} =~ \"asia\" || ${body} regex \"bat(wo)?man\""
  }
}
```

The example is simplified in so far as the value to the "rules" field for step with id "1" must be enterd as a single line string with newlines replaced by "\n" since the value of a property is currently only allowed to be a string. 

If switching to JPA it is recommended to use a more typed approach which `FilterStep` being a subclass of `Step` and having the relation to "filter rules" and "filter statements" in seperate tables, which are linked together.

### UI

An initial design suggestion can be found [here](https://redhat.invisionapp.com/share/KNBZYX1W3)
and the comments on this are collected on https://github.com/syndesisio/syndesis-ui/issues/569

The domain model above reflects the first suggestion to model `&&` and `||` within the filter step. 
For the form type, the filter should be presented as a list of lists: The outer list is the overall "filter step" which contains "filter rules" as elements. Each "filter rule" is a list, too and contains "filter statements". This list is built up from the statements. 

In a first version we can assume that a "filter step" only consist of a single "filter rule" in order to avoid too complex UI interaction. This means, that no "OR" is supported then (as each "filter rules" elements are combined with AND).

The reason to use a two-level definition leading to this list of lists is, that allowing free selection of "and" and "or" might easily leads to confusion for the citizen developer. Also, it looks easier to hide the very technical boolean algebra issue and allow a more rigid structure for the overall construct which is easier to grasp mentally (everythin in a rule is combined with "AND", all rules are combined with "OR").

One big open point is from where to get the metadata to e.g. provide a list of header keys which can be selected from a dropdown in the "key" field. Is there some metadata which contains such values ?

### Misc / Open Points

* How to add the filter step to funktion.yml when deploying the integration
* Define proper REST API based on the domain model provided above
* Evaluate connection to logging (i.e. should dropped message be logged ?)
