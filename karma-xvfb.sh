#!/bin/bash
#nohup /usr/bin/Xvfb :99 -ac -screen 0 1280x720x24 > /dev/null 2>&1 &
#PID=$!
#export DISPLAY=:99
#yarn --no-progress
#npm rebuild node-sass
yarn ng test --watch=false --log-level=DEBUG -cc --no-progress
#pkill Xvfb
#wait $PID
