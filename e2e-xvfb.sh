#!/bin/bash

nohup /usr/bin/Xvfb :99 -ac -screen 0 1280x720x24 &
export DISPLAY=:99
yarn
yarn webdriver-manager update
exec yarn e2e:ipaas-qe
