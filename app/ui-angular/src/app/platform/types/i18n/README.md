# About I18N in Syndesis

Managing and iterating on the text parts of a web application can become a burden once the application scales up beyond certain size, or when the copywriters curating the web app's voice are not meant to deal with Angular or TypeScript entities and structs.

In order to help content editors to focus curating content while still being able to update text contents any time, the Syndesis i18n implementation provides a convenient set of tools for handling text bindings, edit text contents while keeping datetime, currency and number formatting settings in sync with the desired locale.

I18n functionality in Syndesis is based on a solid reactive functional programming implementation on top of the Redux architecture leveraging the [NgRX library](https://github.com/ngrx/platform).

## Insights into the underlying data logic

The logic behind the i18n implementation is a pretty simple one: The Redux implementation will load a JSON dictionary file upon bootstrapping the application and will persist it in-memory as a slice of the NgRX store, honoring the same schema as in the original JSON file.

As we will see later, we can create several dictionary files and swap the active one as needed.

### Setting up our dictionary files

Developers will want to create as many dictionary files as needed at `/app/ui/src/assets/dictionary/` (click here to [open location](https://github.com/syndesisio/syndesis/tree/master/app/ui/src/assets/dictionary)). Each dictionary filename, which is CASE-SENSITIVE, must observe the `{LOCALE_ID}.json` pattern, where `LOCALE_ID` honors the RFC 5646 standard. Eg: `en-GB.json`, `fr-CA.json`, `es-ES.json`, etc.

Seed files in British English (`en-GB.json`) and Spanish (`es-ES.json`) have been made available already for your convenience at the aforementioned location.

> **A word about setup:** Ideally the dictionary files should not be handled locally, but made available remotely and edited through a proper CMS system. If we ever want to update the PATH or URL where these dicitnary files can be fetched, please refer to the _How to edit the dictionary filepath and default bindings_ section below.

## How to manage text content in Syndesis

Each dictionary file should enforce the following schema, which will expand upon time, except for the two root properties `locale` and `dictionary`:

```json
{
  "locale": "en-GB",
  "dictionary": {
    "integrations": {
      "integration": "Integration",
      "integrations": "Integrations"
    },
    "dashboard": {
      "header": "Dashboard",
      "metrics": {
        "sysmetrics": "System Metrics",
        "uptime": "Uptime"
      }
    },
    "shared": {
      "sync": "Loading...",
      "project": "Syndesis"
    }
  }
}
```

The top root `locale` and `dictionary` properties are mandatory. The first one reflects the locale selected, matching the file name, and the second one is a hash object with nested key/value sets that depict keys with string values and other nested objects. This allows to improve the granularity and categorization of key/value pairs, grouping those by context, UI area or domain.

The purpose of the file syntax above is to configure text bindings by following the same hierarchy our JSON file has, so the `dashboard.metrics.sysmetrics` binding would render `"System Metrics"`, for instance.

> **Please note:** The dictionary keys are case sensitive. For this reason it is strongly recommended to leave all dictionary keys in lowercase to prevent issues with unmatched keys and stuff.

### The "shared" node

We can create n-level nestings in our dictionary, but chances are that certain keys will be re-used throughout the application. Therefore, it doesn't make much sense to attach those to a domain-specific group. That is why the `dictionary/shared` property exists. Whatever we create there will be made available as is, with no need to qualify the entire path (unless we create a nested object therein, which is discouraged) as `shared.xxxx`.

Therefore, and according to the example provided:

- `dashboard.header` will render "_Dashboard_";
- `dashboard.metrics.sysmetrics` will render "_System Metrics_";
- `sync`, on the contrary, will render "_Loading..._". Please note the lack of prefixing namespace.

## How to bind text strings in component views

Now that we know how to create text content entries in our JSON dictionary and how to refer to them by traversing its path, we can start injecting text in our components. In order to do so, we will use a custom Angular pipe. So, with the examples provided, we can render the text above in our HTML by composing the following:

```html
<h1>{{ 'dashboard.metrics.sysmetrics' | synI18n }}</h1>
<p class="isLoading">{{ 'sync' | synI18n }}</p>
```
Which will render the following:
```html
<h1>System Metrics</h1>
<p class="isLoading">Loading...</p>
```

> **Please note:** The `I18NPipe` is part of the `SyndesisCommonModule`. As such, said module needs to be imported in the NgModule whose components need to harness the power of I18N bindings.

### The dictionary file syntax
Our custom i18n dictionary parser provides support for advanced text composition within the dictionary entries themselves, by means of our custom syntax, which provides support for _indexed placeholders_ and _reference keys_. Let's see their main differences and use cases.

#### Indexed placeholder syntax

Let's take a look into the following excerpt from our JSON dictionary:

```json
{
  "locale": "en-GB",
  "dictionary": {
    "connections": {
      "header": "Your {{0}} Connection"
    }
  }
}
```

As you can see, the `connections.header` entry has been populated as "`Your {{0}} Connection`". That `{{0}}` **indexed placeholder** can be replaced by passing a parameter to the `synI18n` pipe, so the following snippet:

```html
<h1>{{ 'connections.header' | synI18n: 'Twitter' }}</h1>
```

will render the following:

```html
<h1>Your Twitter Connection</h1>
```

Why zero-indexed placeholders you might ask? It turns out that the `synI18n` pipe supports both strings and string arrays as parameters, so we can pass not just one but several parameters if needed. As as example, for a given dictionary entry named `status` populated as `Your {{0}} Connection was {{1}} back in {{2}}`, the following implementation:

```html
<h1>
  {{ 'connections.header' | synI18n:['AMQ', 'disabled', (new Date()).toDateString()] }}
</h1>
```
will render the following:

```html
<h1>Your AMQ Connection was disabled back in Wed Apr 04 2018</h1>
```

Also, please bear in mind that **parameters are recursive**! So you can pass dictionary entries piped with `synI18n` as parameters and they will get translated as well. Taking the previous JSON excerpt as an example, and extending it with a new named key like this:
```json
{
  "locale": "en-GB",
  "dictionary": {
    "connections": {
      "header": "Your {{0}} Connection",
      "disabled": "Currently Disabled"
    }
  }
}
```

would allow to do the following...

```html
<h1>
  {{ 'connections.header' | synI18n: ('connections.disabled' | synI18n) }}
</h1>
```

which will render the following:

```html
<h1>Your Currently Disabled Connection</h1>
```

It's recommended to wrap localized parameters like these between parenthesis, to ensure a better readibility.

#### Reference key syntax

Our dictionary entries can allocate not only indexed placeholders, but _links_ or placeholders referring to other already existing dictionary keys. This is pretty convenient to avoid key/entries duplication and ensures that references to commonly used nouns found in more than one entry remain consistent.

For a given dictionary excerpt like this:

```json
{
  "locale": "en-GB",
  "dictionary": {
    "integrations": {
      "name": "My integrations",
      "disabled": "currently disabled"
    },
    "dashboard": {
      "header": "{{integrations.name}} are {{integrations.disabled}}"
    }
  }
}
```

we can do the following:

```html
<h1>
  {{ 'dashboard.header' | synI18n }}
</h1>
```

will render the following:

```html
<h1>My Integrations are currently disabled</h1>
```

Please notice how the _reference key_ placeholders honors the same hierarchy existing in the dictionary file. This is valid for keys within the `shared` node as well.

The use of _reference key_ placeholders allows for leaner dictionary files, given the high level of reusability they allow for when creating dictionary entries, contributing to keep common nouns consistent across entries.

## Managing and binding rich HTML content
We can embed rich HTML in our dictionary entries as well! This comes in handy when the overall entry wraps basic HTML formatting or CMS-driven content that would make splitting the different segments into separate entries harder to maintain. Please review the following dictionary excerpt:


```json
{
  "locale": "en-GB",
  "shared": {
    "refresh": "Click on the <strong>Refresh</strong> button to <em>reload</em>"
  }
}
```

However, we need to take into consideration that, for safety reasons, the I18N pipe returns the localized text as plain text only. If you want to render the dictionary entry contents as HTML, you will want to use the DOM's `innerHTML` attribute directive. Eg:

```html
<p [innerHTML]="'refresh' | synI18n"></p>
```

will render the following:

```html
<p>
  Click on the <strong>Refresh</strong> button to <em>reload</em>
</p>
```

### Tip: Handling DOM events (click, mouseover) within HTML-rich entries
Sometimes you will want to allocate hyperlinks in your rich HTML dictionary entries. This is fine, since those will be rendered as such but... What if you want to inject Angular specific directives such as `[routerLink]` or `(click)="doSomething($event)"` event handlers?

Unfortunately the Angular template engine does not work this way and does not support event handling for runtime-generated content as the one managed by our I18N implementation. In order to overcome this limitation, it is strongly recommended to rely on [Event Delegation](https://davidwalsh.name/event-delegate) instead.

Basically, whatever event is triggered from within the injected HTML will propagate up in the DOM tree. If so, we can bind the desired event handler in the PARENT element that wraps the injected HTML, instead of the HTML itself.

As a working example, the following dictionary excerpt:

```json
{
  "locale": "en-GB",
  "shared": {
    "refresh": "Click here to <button>reload</button>"
  }
}
```

Can be rendered like this - please note the `(click)` handler:

```html
<p [innerHTML]="'refresh' | synI18n" (click)="reload($event)"></p>
```

The click events spawned by the injected HTML will bubble up until they're caught by the wrapping `<p>` tag. Obviously, we only want to trigger actions when the `<button>` element is clicked, and not when the other elements (as plain text next to the button) are clicked as well. A smart use of the `Event` information passed through the propagated event to the event handler function assigned to the click event will help:

```typescript
reload(event): void {
  if (event.target
    && event.target.tagName
    && event.target.tagName.toLowerCase() === 'button') {
      // Insert your reload logic here...
    }
}
```

Sometimes we might need to add more than one item triggering events in our HTML snippets, or even perhaps a link to another page. For the former, we can assign `id` or `data-` attributes and discriminate whatever logic we want to apply depending on the value returned by `event.target.getAttribute('id')` (or whatever attribute we use). In regards of the `routerLink` directive or even plain regular links pointing to pages in our application, a combination of the techniques explained above will do the trick, as you can see in the following example.

```json
{
  "locale": "en-GB",
  "connections": {
    "listlink": "<p>Click to list <a href=\"{{0}}\">connections</a></p>"
  }
}
```

The `listlink` entry can be injected in our templates like this:

```html
<div [innerHTML]="'refresh' | synI18n: '/connections'"
     (click)="handleLinks($event)">
</div>
```

And in our component controller we can handle a link like this:

```typescript
handleLinks(event): void {
  if (event.target
    && event.target.tagName
    && event.target.tagName.toLowerCase() === 'a') {
      const linkHref = event.target.getAttribute('href');
      this.router.navigateByUrl(linkHref);
    }
}
```

By doing the above we can handle as many links as we wish from inside a single chunk of HTML injected on runtime.

> **Please note:** In our example above the link is passed as an injectable parameter through the I18N pipe, rather than hardcoding it as part of a given dictionary entry. It is usually a bad idea to hardcode link URLs within dictionary entries, since those are meant to change anytime and content editing should remain agnostic from the application logic.

## Using i18n values programmatically
Up to now we've seen how we handle I18N features from within the templates and how we can edit the dictionary files. However, in our daily practice we might need to access a particular dictionary entry from within our code. This is more of an edge case, though.

If you ever need to access specific keys from the I18N store, you can leverage the `I18NService.getValue(dictionaryKey: string, args?: any[]): Observable<string>` method available at the `I18NService` injectable class. Such method will return an observable string which will issue values every time the active locale is changed.

### Switching to another I18N locale programmatically
You can swap the active dictionary by triggering the `I18NFetch` action, properly populated with the locale you want to enable. Eg:

```typescript
switchToItalian(): void {
  this.store.dispatch(new I18NFetch('it-IT'));
}
```

_Please refer to the [Platform documentation](https://github.com/syndesisio/syndesis/blob/master/app/ui/src/app/platform/README.md) about further details on how to dispatch Redux actions_.

The example above will trigger a Redux action that will update the store to set is on `sync` mode, which is pretty convenient if you want to show loading spinners ot any other visual cue that there's an underlying process going on. A side effect will intercept that action and will inspect its payload (`it-IT`), fetching the corresponding `it-IT.json` file asynchronously. Once successfully loaded, the dictionary store will be refreshed with the new data coming from the store and the pipes rendering the content will automatically refresh to render the new values.

It is worth highlighting that every time we switch to another locale, its ID is persisted in the browser's local storage layer, so the application will fetch that specific locale next time the application boots.

### How to edit the dictionary filepath and default bindings
There are some global variables that affect the behavior of the I18N implementation that developers need to be aware of. These are configured as part of the Angular execution [environment setup](https://github.com/syndesisio/syndesis/tree/master/app/ui/src/environments), for both dev and production environments. If you open such file you will find a `i18n` node containing the following parameters:

```javascript
i18n: {
  fallbackValue: '?',
  localStorageKey: 'syndesis-i18n-locale',
  dictionaryFolderPath: '/assets/dictionary'
}
...
```

Let's go through these parameters in more detail:

- `fallbackValue`: This is the text string that will be rendered when the I18N engine does not find a matching key, hence no value is returned. This parameter aims to give more visibility to these hidden values and at least provide a fallback value.
- `localStorageKey`: The name of the key where the chosed LOCALE ID will be persisted in the user's browser local storage layer.
- `dictionaryFolderPath`: The full path to the dictionaries folder, stating also its own name, as starting from `ui/src`.

## Number, currency, timestamp and 3rd party localization
It is worth remarking that Syndesis UI depends on some third party vendors, such as Angular or MomentJS, that handle i18n its own way. An effort has been made to connect our I18N implementation with theirs as well. Therefore, whenever the LOCALE information is refreshed, the following processes are handled by the i18n mechanism:

- Angular will fetch and register new locale data to provide a localized experience for its own pipes depending on locale data: time, date, currency or number, just ot name a few.
- MomentJS will be refreshed to use the locale configuration corresponding with the LOCALE Id provided.

Interop functionality with third party vendors is still experimental and subject to change.

