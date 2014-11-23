#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import webapp2
import os

class MainPage(webapp2.RequestHandler):

    def get_class_from_file_name(self, file_name):
        if file_name.endswith("Test.html"):
            return "unitTest"
        elif file_name.endswith("FakePage.html"):
            return "fakePage"
        else:
            return ""

    def searchTestFiles(self):
        fileList = []
        directory = 'src/test/src/'
        absPath = os.path.abspath(directory)
        self.response.write('looking at files in directory ' + directory + ' <br>')
        counter = 1
        for r, d, f in os.walk(directory):
            testFiles = [];
            for files in f:
                    if files.endswith(".html"):
                        testFiles.append(files)
            print r
            print f
            print len(f)
            if len(testFiles) >= 1:
                self.response.write('<div class="row">')
                self.response.write('<b>' + r + '</b><br><br>')

                self.response.write('<a href="#hide' + str(counter) + '" class="hide myButton" id="hide' + str(counter) + '">Expand</a>')
                self.response.write('<a href="#show' + str(counter) + '" class="show myButton" id="show' + str(counter) + '">Collapse</a><div class="list">')
                self.response.write('<ul>')
                for files in testFiles:
                    fileNameWithPath = os.path.join(r,files);
                    fileName = str(files);
                    self.response.write('<li><a class="testFile ' + self.get_class_from_file_name(fileName) + '"  href="' + fileNameWithPath + '" target="_blank">'+ fileName +'</a></li>')
                self.response.write('</ul>')
                self.response.write('</div></div>')
                counter = counter + 1
        if counter == 1:
            self.response.write('<br><b>There are no test files in: ' + directory + '</b>')


    def get(myOwn):
        myOwn.response.write('<style type="text/css">')
        myOwn.response.write('b {margin-bottom:5px;}')
        myOwn.response.write('.row { vertical-align: top; height:auto !important; background:#eeeeee; display:inline-block; padding:5px; border-style:ridge; border-color:#dddddd; border-width:2px;}')
        myOwn.response.write(' .list {display:none; } .show {display: none; } .hide:target + .show {display: inline; } ')
        myOwn.response.write('.hide:target {display: none; } .hide:target ~ .list {display:inline; } @media print { .hide, .show { display: none; } }')
        myOwn.response.write('.myButton {border-style:ridge; border-color:#dddddd; border-width:2px; border-radius:4px; background-color:#FFFFFF; padding:2px;}')
        myOwn.response.write('.myButton:hover {background-color:#DDDDDD;}')
        myOwn.response.write('a {text-decoration:none; color:#000000;} ')
        myOwn.response.write('li {padding:2px;}')
        myOwn.response.write('h1 a {text-decoration:underline; color:#00F0F0;}')
        myOwn.response.write('.testFile {text-decoration:none; color:#000000;}')
        myOwn.response.write('.testFile:hover {color:#ff0000;} ')
        myOwn.response.write('.fakePage {text-decoration:none; color:#00B26B;}')
        myOwn.response.write('.fakePage:hover {color:#006B40;} ')
        myOwn.response.write('.unitTest {text-decoration:none; color:#990099;}')
        myOwn.response.write('.unitTest:hover {color:#CC0052;} ')
        myOwn.response.write(' </style>')
        myOwn.response.write('<h1><a href="/index.html" target="_blank">Main Page</a></h1>')
        myOwn.response.write('List of test files to use<br>')
        myOwn.searchTestFiles()
        #for file in fileList:
        #	self.response.write('<a href="' + file + '">'+ file +'</a><br>')

app = webapp2.WSGIApplication([('/testList', MainPage), ('/', MainPage)], debug=True)

