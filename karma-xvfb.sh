#!/bin/bash

nohup /usr/bin/Xvfb :99 -ac -screen 0 1280x1024x24 &
export DISPLAY=:99
yarn
npm rebuild node-sass
exec yarn ng test -- --watch=false -cc --no-progress
