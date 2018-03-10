# About the Syndesis CSS Framework

Syndesis UI is a JavaScript application built on top of the Angular framework. As such, its CSS implementation encompasses two different layers:

* A generic CSS foundation which is injected as a linked dependency into the main `index.html` file upon loading the web application for the first time.
* Specific CSS rules which are injected into the main application shell on runtime. These particular rules are stylesheets whose hi-specificity CSS is scoped to a single component only, which are injected into the appshell when said component is rendered and get dynamically removed once the component is destroyed.

## General principles and conventions

In order to keep consistency in our code, we should conform to the following conventions:

* Apply a custom prefix to our CSS selectors such as `.syn-`. This ensures we will prevent name collisions with CSS classnames brought into the project by other 3rd party libraries in use.
* Use the [BEM pattern](http://getbem.com/) for naming class names and properly nesting them to ensure stylesheets provide an informative picture of the DOM elements hierarchy in our documents. Use SASS compound selectors to enhance your code by leveraging nesting.
* Apply styles from global to particular: always begin styling elements in a generic fashion (or just delegate styling on libraries such as Patternfly) and then apply styles on a per component basis only when the general CSS ruleset needs to be overriden.
* Ensure zero-specificity by avoiding the `!important` declaration and stay away from Shadow Piercing combinators. If a child component needs to be restyled, create specific classnames within the child component element itself that can be triggered from the host element. In the case of 3rd party components, resource to `::ng-deep`.

## Folders and partials filesystem
The CSS implementation is based on SASS under the SCSS syntax, hence the name for the parent folder containing all files. These files observe a very strict policy:

### The `/base` folder

The `/base` folder contains several files categorized by function or purpose which ONLY encompass pure SASS variables, functions, mixins or extensions. As such, no actual CSS class name or selector (except for those wrapped by the former) should exist here.

Bear in mind that the files contained in that folder will be exposed globally through the `syndesis-sass` import, so all components' stylesheets can also leverage its mixins, functions and variables. Therefore, if we ever introduce actual final CSS classnames within those files, such CSS code will be reinjected every time a component that imports it gets rendered on screen, no matter if said code is required or not.

#### Tips:

- Make a generous use of variables for ensuring consistency in the UI, abstracting commonly used values for margins, paddings, etc.
- Wrap commonly repeated blocks of CSS code in mixins.
- Expose mixins in classname extensions (`%{className}`) you can later inherit from in your own CSS class names.
- When relying on third party variables (eg. Patternfly colors or variables), abstract such variables by linking its own partials from the `_vendor.scss` partial. This will prevent you from tweaking paths file by file should such dependency change its path or filename in the future.

### The `/utils` folder

The `/utils` folder contains files that set global CSS rules for the entire application. This is the place for overriding global or 3rd party styles or define presentation rules that are generic for the entire UI.

The utils folder will see its contents grow upon time, but the `_helpers.scss` partial is worth a more detailed view. This is where you will find the helpers that you can use for your own HTML elements. Sometimes, such helpers are just wrappers of a particular mixin, so we can leverage that mixin style rules within our HTML views without having to create a custom component stylesheet or CSS block, just by applying the CSS helper class directly into the HTML template.

> *Please note:* Helpers now feature some naming discrepancies that should be addressed with the collaboration of the entire team.

### The `/components` folder

Last but not least, we have the components folder. Here we will find files categorized by name which address particular styles for _generic_ component families: forms, toolbars, menus, etc.

The idea is not to include styles for a particular component, but styles that affect each and every instance of a commonly used set of components or its inner HTML elements.

## How to proceed when creating new CSS rules

When overseeing the need for styling up something, ask yourself wether that piece of presentation logic will be reused elsewhere and how, and then observe the next decision flow:

1. Could it be used to enhance or extend some already existing classnames? Create a classname you can inherit through `%` and then reuse it with `@extend %myClassname;` within your already existing classnames.
2. Is this something that goes beyond a particular class or might be required by another code block? Use a mixin instead. Please think if you will require to add a custom payload and then create a signature with default parameter variables.
3. Is this something you know for a fact that is needed from the very moment the application is bootstrapped? Create a generic style and store it at `/components` in a file that represents its components or UI items category.
4. If the above might be needed for not only one but many elements in the DOM, regardless they converge in the DOM at some point or not, use a helper instead.
5. For all the above, in the moment you see there is a value that express a repeating parameter or criteria, abstract it in a variable. Avoid hardcoding values as much as possible.
6. Is it something that affects only one component or depicts a particular style override? Use the components associated stylesheet instead and import `syndesis-sass` from within that stylesheet to gain access to the application mixins, functions, etc.

When creating a new folder which will contain several files, abstract the internal structure of the folder by exposing all the SASS partials through a single façade file named `_module.scss`. This convention will also help other developers to easy locate where is each item within the filesystem.

### CSS reusability within components in the same context tree

You can reuse all the Syndesis variables, mixins and functions or inherited classnames in your components' stylesheets by appending `@import 'syndesis-sass';` at the top of your component's particular stylesheet.

Sometimes you might find that several components in the same domain or application context need to share the same rules, which are already defined in an already existing component's stylesheet. In such case, **do not copy+paste that CSS snippet into your component's stylesheet**. That will only add technical debt to the project. You can take three different approaches here:

- Strip it out and wrap that reusable piece of functionality in a helper and expose it globally. You can also advocate for a mixin or even a helper class.
- If you feel the above is a bit overkill because that CSS definition is not generic enough and there are not many chances that it will be reused outside the scope of this domain, then wrap that piece of CSS in a standalone SASS file and import it from the `styleUrls` array in the `@Component` decorator definition.
- Same as the above, you can skip tweaking the component's decorator definition and import the standalone SCSS file straight from each component's own stylesheet, by means of an `@import` statement.

All in all, you can combine all the approaches above, as needed depending on each circumstance.

## CSS development with the Angular CLI and 3rd party libraries

Please note that we process and build our CSS code by means of the Angular CLI compiler, which takes care of the following:

- Transpiling the code.
- Adding vendor prefixes.
- Appending vendor CSS libraries to the main CSS bundle.
- Compressing the code output.
- Bundling all generic files into a single named CSS bundle.

### Appending 3rd party libraries to the project

If you install a 3rd party dependency which also features CSS stylesheets, those can be added to the main CSS bundle by appending them to the `styles` array property in `.angular-cli.json`.

Do not forget also to append any file containing vendor SASS variables (such as colors, widths or heights) to our own `base/_vendor.scss` file.

Please refer to Angular CLI's online documentation for details.

## Essential readings:

* https://sass-lang.com/
* http://thesassway.com/
* http://getbem.com/
* https://angular.io/guide/component-styles
