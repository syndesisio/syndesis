#!/usr/bin/env python

__license__ = """
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
under the License.
"""
# Parse ascii doc files and generate json file.
# The json file will be used in the enmasse console to display UI elements like form labels and tooltips

import re
import json
import argparse
import os
from glob import glob
import sys

# flatten the keys in a dict
def flatten(d,result=None,index=None,Key=None):
    if result is None:
        result = {}
    if isinstance(d, dict):
        for key in d:
            # *.external keys are optional so remove them from the flattened output
            if key == 'external' and isinstance(d[key], basestring):
                continue
            value = d[key]
            if Key is not None and index is not None:
                newkey = ".".join([Key,(str(key).replace(" ", "") + str(index))])
            elif Key is not None:
                newkey = ".".join([Key,(str(key).replace(" ", ""))])
            else:
                newkey= str(key).replace(" ", "")
            flatten(value, result, index=None, Key=newkey)
    else:
        result[Key]=1

    return result.keys()

# test to see if the keys in the expected file are in the dictionary
def complete(expected, jobj):
    try:
        with open(expected, "r") as in_file:
            expected = json.load(in_file)
            generated = flatten(jobj)
            missing = [x for x in expected if x not in generated]
            extra = [x for x in generated if x not in expected]
            if len(missing):
                print "The following keys are missing from the ascii docs:"
                for line in missing:
                    print line
            if len(extra):
                print "The following keys are in the ascii docs but not in the expected file:"
                for line in extra:
                    print line
            if len(missing) or len(extra):
                return False
    except IOError:
        print >> sys.stderr, "Unable to open or parse", args.input
        return False

    return True

# parse a list of files for formatted comments
# the list of files should already be tested for existance
def parse(names, verbose):
    if verbose:
        print "Parsing ascii doc files for comments:"
    base = {}
    p = re.compile("^// (.*?)\:(.*)")

    for fname in names:
        try:
            modeldoc = open(fname, "r")
        except IOError:
            print >> sys.stderr, "Unable to open ", fname
            continue

        if verbose:
            print "    ", fname

        tooltip = None

        for line in modeldoc:
            s = p.search(line)
            if s:
                obj = base
                keystr = s.group(1)     # // address.queue.label
                value = s.group(2)      # :the text after the first :

                keys = keystr.split('.')
                for i, key in enumerate(keys):
                    # the first keys define the object heirarchy
                    if i < len(keys) - 1:
                        if not key in obj:
                            obj[key] = {}
                        obj = obj[key]
                    # the last key gets the value
                    else:
                        # :start and :stop lines surround block values
                        if value == 'start':
                            tooltip = []                    # start accumulating tooltip lines
                            continue
                        elif value == 'stop':
                            obj[key] = ' '.join(tooltip)    # join the tooltip lines in to one line
                            tooltip = None
                            continue

                        # assign the value to the last key
                        obj[key] = value

            # are we accumulating tooltip lines
            elif tooltip is not None:
                tooltip.append(line.rstrip())

        modeldoc.close()

    return base

parser = argparse.ArgumentParser(description='Output json file made from Extracting comments from one or more asciidoc files.')
parser.add_argument('fnames', metavar='f', type=str, nargs='+',
                    help='Name[s] of the input asciidoc file[s] or directories')
parser.add_argument('-v', "--verbose", action='store_true', help='verbose output')
parser.add_argument("-o", "--output", help="Output file name. If ommitted the output will go to stdout.")
parser.add_argument("-i", "--input", help="Name of the file that contains all the expected help keys.")
args = parser.parse_args()

# extract all the directories from the list of file names on the command line
dirs = [x for x in args.fnames if os.path.isdir(x)]

# extract all the flat files from the list of file names. Any files that don't exist are ignored
files = [x for x in args.fnames if os.path.isfile(x)]

# get list of all *.adoc files in all passed in dirs and their subdirs
result = [y for z in dirs for x in os.walk(z) for y in glob(os.path.join(x[0], '*.adoc'))]

# combine files found in dirs with explicitly passed in file names
result.extend(files)
if len(result) == 0:
    print "No ascii docs found"
    exit (1)

# parse all the files and create a single dict
jfile = parse(result, args.verbose)

# test the dict against the passed in key file for completeness
if args.input:
    if not complete(args.input, jfile):
        exit (1)
else:
    print "Warning: output not checked against a key file for completeness."

# convert dict into string for output
jstr = json.dumps(jfile, sort_keys=True, indent=4, separators=(',', ': '))

# either save the json to a file or output it to the console
if args.output:
    try:
        with open(args.output, "w") as text_file:
            text_file.write(jstr)
        if args.verbose:
            print args.output, "created"
    except IOError:
        print >> sys.stderr, "Unable to open ", args.output, "for output."
        exit(1)
else:
    print jstr

exit(0)
