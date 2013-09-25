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

    def get(self):
        fileList = self.searchTestFiles()
        for file in fileList:
            self.response.write('<a href="' + file + '">'+ file +'</a><br>')
        #self.redirect('http://www.percussionstudio.org',permanent=True)


        #    template = JINJA_ENVIRONMENT.get_template('index.html')
        #    self.response.write(template.render())

    def searchTestFiles(self):
        fileList = []
        directory = 'html/test'
        for r,d,f in os.walk(directory):
            for files in f:
                if files.endswith(".html"):
                    fileList[len(fileList):] = [os.path.join(r,files)]
        return fileList

app = webapp2.WSGIApplication([('/testList', MainPage)], debug=True)

