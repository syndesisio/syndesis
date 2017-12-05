# syndesis
A flexible, customizable, open source platform that provides core integration capabilities as a service.

Currently this repo just pulls in all the other syndesis project repos as submodules, to clone it locally run:

git clone --recursive https://github.com/syndesisio/syndesis.git

### Building everything

    app/build.sh
    
To see all the available options:

    app/build.sh --help
    
### Resume from module    
To resume from a particular module:

    app/build.sh --resume-from ui
    
### Using the image streams    
To build everything using image streams (instead of directly talking to docker):

    app/build.sh --with-image-streams
    
Note that this assumes that you are using a template flavor that also supports image streams.
