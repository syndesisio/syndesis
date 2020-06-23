# Connector/Connection icons

To update all backend icons in known connectors run:

    $ yarn icons

To update your running minishift install:

    $ oc login # to login to minishift if not logged in already
    $ syndesis build --flash --clean --image --module server --dependencies # to rebuild and install backend

Make sure to keep the `mapping.json` file up to date.

To generate optmized SVGs in the `optimized` directory run:

    $ yarn optimize
    
To generate PNG files into the neighboring PNG directory run (install inkscape first):

for each in *.svg; do inkscape $each --export-width=300 --export-filename=../png/`basename $each .svg`.png; done
   
   
