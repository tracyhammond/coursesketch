CS_HOME="/home/sketchlab/link_to_shared/coursesketch"

cd $CS_HOME
cd "config/dev_info/github_auto"

/bin/bash automerge.sh
success=$?
cd $CS_HOME
echo "Setting permisions back to able to be read"
sudo find \( -type f -execdir chmod 775 {} \; \) \
                  -o \( -type d -execdir chmod 777 {} \; \)

echo "Setting permisions back to able to execute scripts"
cd $CS_HOME
cd "config/dev_info/github_auto"
sudo find \( -type f -execdir chmod 775 {} \; \) \
                  -o \( -type d -execdir chmod 777 {} \; \)

if [[ $success -eq 0 ]];
then
    echo "updating branches Successful"
else
    echo "merging failed"
    exit 1
fi
cd $CS_HOME
mvn clean install
STATUS=$?
if [ $STATUS -eq 0 ]; then
    echo "install Successful"
else
    echo "Maven Failed"
    exit 1
fi

cd config

/bin/bash copyjars.sh <<< "/home/sketchlab/VirtualBox VMs/Shared/"

mv "${CS_HOME}/CourseSketchProjects/coursesketchwebclient/target/website" "${CS_HOME}/CourseSketchProjects/coursesketchwebclient/target/coursesketchwebclient"

cp -r "${CS_HOME}/CourseSketchProjects/coursesketchwebclient/target/coursesketchwebclient/" "/home/sketchlab/coursesketch/"

cd "/home/sketchlab/coursesketch/"
sudo find \( -type f -execdir chmod 644 {} \; \) \
                  -o \( -type d -execdir chmod 771 {} \; \)

#scp -r "/home/sketchlab/VirtualBox VMs/Shared/coursesketch/CourseSketchProjects/coursesketchwebclient/target/coursesketchwebclient" hammond@goldberglinux01.tamu.edu:local

#ssh hammond@goldberglinux01.tamu.edu

exit 0
