cd "/home/sketchlab/VirtualBox VMs/Shared/coursesketch/"
cd config
cd dev_info
cd github_auto

/bin/bash automerge.sh
success=$?
echo "Setting permisions back to run"
chmod 775 buildupdate.sh
if [[ $success -eq 0 ]];
then
    echo "updating branches Successful"
else
    echo "merging failed"
    exit 1
fi
cd ../../../
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

mv "/home/sketchlab/VirtualBox VMs/Shared/coursesketch/CourseSketchProjects/coursesketchwebclient/target/website" "/home/sketchlab/VirtualBox VMs/Shared/coursesketch/CourseSketchProjects/coursesketchwebclient/target/coursesketchwebclient"

cp -r "/home/sketchlab/VirtualBox VMs/Shared/coursesketch/CourseSketchProjects/coursesketchwebclient/target/coursesketchwebclient/" "/home/sketchlab/coursesketch/"

#scp -r "/home/sketchlab/VirtualBox VMs/Shared/coursesketch/CourseSketchProjects/coursesketchwebclient/target/coursesketchwebclient" hammond@goldberglinux01.tamu.edu:local

#ssh hammond@goldberglinux01.tamu.edu

exit 0
