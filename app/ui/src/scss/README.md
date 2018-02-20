# About the Syndesis CSS Framework

Syndesis UI is a JavaScript application built on top of the Angular framework. As such, its CSS implementation encompasses two different layers:

* A generic CSS foundation which is injected as a linked dependency into the main `index.html` file upon loading the web application for the first time.
* Specific CSS rules which are injected into the main application shell on runtime. These particular rules are stylesheets whose hi-specificity CSS is scoped to a single component only, which are injected into the appshell when said component is rendered and get removed once the component is destroyed.

## General principles and conventions

In order to keep consistency in our code for all current and future contributors, we conform to the following conventions:

* Apply a custom prefix to our CSS sselectors such as `.syn-` in order to prevent name collisions with CSS classnames brought by other 3rd party libraries in use.
* Use the [BEM pattern](http://getbem.com/) for naming class names and properly nesting them to ensure stylesheets provide an informative picture of the DOM elements hierarichy in our documents.
* 

## Folders and partials filesystem

### The /base folder

### The /components folder

### The /utils folder

## How to proceed when creating new CSS rules

### CSS reusability within components in the same context tree

## CSS development with the Angular CLI