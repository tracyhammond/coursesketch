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
	
	
	def searchTestFiles(self):
		fileList = []
		directory = 'html/test'
		absPath = os.path.abspath(directory)
		self.response.write('looking at files in directory ' + directory + ' <br>')
		counter = 1
		for r, d, f in os.walk(directory):
			self.response.write('<div class="row">')
			self.response.write('<b>' + r + '</b><br><br>')
			
			self.response.write('<a href="#hide' + str(counter) + '" class="hide myButton" id="hide' + str(counter) + '">Expand</a>')
			self.response.write('<a href="#show' + str(counter) + '" class="show myButton" id="show' + str(counter) + '">Collapse</a><div class="list">')
			self.response.write('<ul>')
			for files in f:
				if files.endswith(".html"):
					fileNameWithPath = os.path.join(r,files);
					fileName = str(files);
					self.response.write('<li><a class="testFile" href="' + fileNameWithPath + '" target="_blank">'+ fileName +'</a></li>')
					#fileList[len(fileList):] = [os.path.join(r,files)]
			self.response.write('</ul>')
			self.response.write('</div></div>')
			counter = counter + 1

		
	def visitPath(arg, firname, names):
		self.response.write('<a href="' + fileName + '">'+ fileName +'</a><br>')
	
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
		myOwn.response.write('.testFile {text-decoration:none; color:#000000;}')
		myOwn.response.write('.testFile:hover {color:#ff0000;} ')
		myOwn.response.write(' </style>')
		myOwn.response.write('List of test files to use<br>')
		myOwn.searchTestFiles()
        #for file in fileList:
		#	self.response.write('<a href="' + file + '">'+ file +'</a><br>')

app = webapp2.WSGIApplication([('/testList', MainPage)], debug=True)

