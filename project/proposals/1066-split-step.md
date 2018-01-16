## Split data in integration flow

* Issue: https://github.com/syndesisio/syndesis-project/issues/384
* Sprint: 

### User Story

### Domain

#### UI defined split (typed)

For the typed definition of a filter, a **filter step** contains one or more **filter rules** which are all combined either _and_ or _or_

A `FilterStep` is a regular `Step` of type "filter". It has the following structure:

```java
// A step used as split
class SplitStep extends Step { 
  String expression;
}
```

### UI


### Misc / Open Points


### Remarks
