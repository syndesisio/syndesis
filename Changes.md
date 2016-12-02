## Changelog

### 1.1 (in progress) 

Minor YAML change to make things more flexible; so that the `action` property becomes a list of objects which have a `kind` of either `function` or `endpoint`.

So a YAML like this:

```yaml
rules:
  - trigger: http://0.0.0.0:8080
    action: io.fabric8.funktion.example.Main
```    

now looks like this

```yaml
rules:
  - trigger: http://0.0.0.0:8080
    actions:
    - kind: function
      name: io.fabric8.funktion.example.Main
```    

Its a little bit more verbose but makes things much more flexible; e.g. we can invoke multiple functions or endpoints in order now more cleanly.


### 1.0

First release of funktion supporting java, groovy, kotlin and nodejs functions