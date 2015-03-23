read -p "enter the directory to put the servers: " -i "$HOME/" -e result_path
print "navigator to $current_path to start grabbing jars"

serverName=('Database' 'Login' 'Submission' 'AnswerChecker' 'Recognition' 'Proxy')
cd ../CourseSketchProjects
length=$(expr ${#serverName[@]} - 1)
echo $length
for (( i=0; i<=$length; i++ ))
do
	dir="CourseSketch${serverName[$i]}Server"
	echo $dir
	result=($(find $dir -name '*-run.jar') )
	if [ -n "$result" ]; then
		fullPath="$result"
		echo "copying jar $fullPath to $result_path"
                cp $fullPath "$result_path" 
	fi
done

