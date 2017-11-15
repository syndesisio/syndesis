#!/bin/bash

jq '.dependencies * .devDependencies | keys | .[]' package.json | xargs yarn upgrade
