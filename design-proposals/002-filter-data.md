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

In both cases, I expect to be able to apply basic string operations to the data to form a filter expression (e.g. contains, does not contain, starts with ...).  For the full list of supported operators, see the Operator Support section here:
http://camel.apache.org/simple.html

Similar to logging, the UI should help in creating filter expressions for new/inexperienced users (like through clickable examples or typed form inputs).  The interface should also allow for advanced users to enter free form expressions.

### Domain

#### UI defined filter (typed)

For the typed definition of a filter, a **filter step** contains one or more **filter rules** which are combined with _and_ or _or_

A _filter step_ is a regular `Step` with a type "filter". 
_Filter rules_ are part of the configuration of this step and stored in the properties of a step.
The lowest building block is a a `FilterRule`. It has the following properties:

* **type** : On which part of the message to apply the filter. Can be either "header", "body" or "properties".
* **key** : The key to use to extract a message value (e.g. header or property name). The key is empty when type is equals to body.
* **op** : The operator to use for the filter
* **value** : The value used by the operator to determine whether a filter applies.
* **combinator** : How this rule is combined with the previous rule. The value can be either `&&` or `||`. The first filter rule's combinator is ignored when building up the expression.
 
"type" and "key" could also be combined to a selector (which e.g. then would have the format `type.key`)

The configuration of a `FilterStep` is then a list of `FilterRules`.

For example, the following configuration

```yaml
- type: "body"
  op: "contains"
  value: "antman"
- type: "header"    
  key: "region"
  op: "=~"
  value: "asia"
  combinator: "&&"
- type: "body"
  op: "regex"
  value: "bat(wo)?man"
  combinator: "||"
- type: "header"
  key: "publisher"
  op: "=~"
  value: "DC Comics"
  combinator: "&&"
```

would be stored as a property "filter" on the integration step. It translates later to simple lang expression

```
${body} contains "antman" && ${in.header.region} =~ "asia" || ${body} regex "bat(wo)?man" && ${in.header.publisher} =~ "DC Comics"
```

The simple expression language [does not support parentheses](http://camel.apache.org/simple.html) nor precedence of operators so the expression is always evaluated from left to right. It should be considered to ommit logical operations like this for now. It can be added easily to the UI later, too.

##### Fixed background data

For the `keys` as well as for the possible `operators` the UI needs a list of values which can be chosen. The list of operators should be fixed, whereas it should be possible to add a freeform key (but with suggestion of a set of given keys). 

This background data can be obtained by a dedicated API call to an endpoint `../integrations/filter/options` which takes an existing integration ID (if the intergration has already been created, otherwise the ids of all connections before this filter step need to be send to the API server) as parameter.

It returns all data required to build the form:

```json
{
  "header" : [ "headerKey1", "headerKey2", .... ],
  "body" : [ "bodyKey1", "bodyKey2", .... ],
  "op" : [
    { "label": "contains (ignore case)",  "operator": "=~"},
    { "label": "contains", "operator": "contains"},
    { "label": "matches", "operatos": "regex"},
    ......
  ]
}
```

It's up to the server how to determine this data set. Ideally the connector themselves provide this meta information (which could be done also by e.g. reflection on the body type).

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
[{ 
  "id": "1",
  "stepKind": "filter",
  "configuredProperties": {
    "type": "form",
    "simple" : "${body} contains \"antman\" || ${in.header.publisher} =~ \"DC Comics\"",
    "rules" : [
       { 
         "type": "body",
         "op": "contains",
         "value": "antman"
       },
       { 
         "type": "header",
         "op": "=~",
         "value": "DC Comics",
         "combinator": "&&"
       }
    ]
  }
},
{
  "id": "2",
  "stepKind": "filter",
  "configuredProperties": {
    "type": "text",
    "simple": "${in.header.region} contains \"asia\" || ${body} regex \"bat(wo)?man\""
  }
}]
```

The example is simplified in so far as the value to the "rules" field for step with id "1" must be entered as a single line string with newlines replaced by "\n" since the value of a property is currently only allowed to be a string. 

If switching to JPA it is recommended to use a more typed approach which `FilterStep` being a subclass of `Step` and having the relation to "filter rules" and "filter statements" in seperate tables, which are linked together.


### UI

An initial design suggestion can be found [here](https://redhat.invisionapp.com/share/KNBZYX1W3)
and the comments on this are collected in https://github.com/syndesisio/syndesis-ui/issues/569

The meta data for the list of header, body, property keys (e.g. for the possible value for 'key' for all possible 'types' should be obtainable via a rest call). The 'keys' themselves are freely typable because it is assumed that not all possible keys can be predicted during design time. It is recommended to use textfield with autosuggestions while typing. How to obtain this meta data is described above in "Fixed background data".


### Misc / Open Points

* How to add the filter step to funktion.yml when deploying the integration
* Define proper REST API based on the domain model provided above
* Evaluate connection to logging (i.e. should dropped message be logged ?)
* How to obtain the meta data to present in the dropdown boxes (or intellisense completion) for the header / property keys ?

### Remarks

* The current domain object `IntegrationConnectionStep` seems to be superfluous and should be deleted then.
