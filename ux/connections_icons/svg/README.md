# Connector/Connection icons

To update all backend icons in known connectors run:

    $ yarn icons

To update your running minishift install:

    $ oc login # to login to minishift if not logged in already
    $ syndesis build --flash --clean --image --module server --dependencies # to rebuild and install backend

Make sure to keep the `mapping.json` file up to date.

To generate optmized SVGs in the `optimized` directory run:

    $ yarn optimize
