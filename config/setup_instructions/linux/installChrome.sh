#!/bin/bash

# https://github.com/travis-ci/travis-ci/issues/938
# https://github.com/travis-ci/travis-ci/issues/2555

export DISPLAY=:99.0
sudo sh -e /etc/init.d/xvfb start
sudo apt-get remove chromium-browser
sudo apt-get update
echo ttf-mscorefonts-installer msttcorefonts/accepted-mscorefonts-eula select true | sudo debconf-set-selections
sudo apt-get install ttf-mscorefonts-installer
sudo apt-get install x-ttcidfont-conf
sudo mkfontdir
sudo apt-get install defoma libgl1-mesa-dri xfonts-100dpi xfonts-75dpi xfonts-scalable xfonts-cyrillic libappindicator1
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo mkdir -p /usr/share/desktop-directories
sudo dpkg -i google-chrome-stable_current_amd64.deb
sudo apt-get install -f
sudo dpkg -i google-chrome-stable_current_amd64.deb
export CHROME_SANDBOX=/opt/google/chrome/chrome-sandbox
sudo rm -f $CHROME_SANDBOX
sudo wget https://googledrive.com/host/0B5VlNZ_Rvdw6NTJoZDBSVy1ZdkE -O $CHROME_SANDBOX
sudo chown root:root $CHROME_SANDBOX; sudo chmod 4755 $CHROME_SANDBOX
sudo md5sum $CHROME_SANDBOX
sudo chmod 1777 /dev/shm

