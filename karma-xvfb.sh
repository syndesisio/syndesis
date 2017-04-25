#!/bin/bash

nohup /usr/bin/Xvfb :99 -ac -screen 0 1280x720x24 &
export DISPLAY=:99
yarn --no-progress
npm rebuild node-sass
exec yarn ng test -- --watch=false -cc --no-progress
