screenName=('database' 'login' 'submission' 'answer' 'recognition' 'proxy' 'mongo')
cd ../CourseSketchProjects
length=$(expr ${#screenName[@]} - 1)
echo $length
for (( i=0; i<=$length; i++ ))
do
	dir="CourseSketch${serverName[$i]}Server"
	result=($(find $dir -name '*-run.jar') )
	if [ -n "$result" ]; then
		fullPath="$result"
		command1="java -jar $fullPath"
		screen -X -S "${screenName[$i]}" quit
		echo "killing screen ${screenName[$i]}"
	fi
done
