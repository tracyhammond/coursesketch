screenName=('database' 'login' 'submission' 'answer' 'recognition' 'proxy')
serverName=('Database' 'Login' 'Submission' 'AnswerChecker' 'Recognition' 'Proxy')
cd ../CourseSketchProjects
length=$(expr ${#screenName[@]} - 1)
echo $length
for (( i=0; i<=$length; i++ ))
do
	dir="CourseSketch${serverName[$i]}Server"
	echo $dir
	result=($(find $dir -name '*-run.jar') )
	if [ -n "$result" ]; then
		fullPath="$result"
		echo $fullPath
		command1="java -jar $fullPath"
		screen -d -m -S "${screenName[$i]}" bash -c "$command1"
		echo "attach: screen -r ${screenName[$i]}"
	fi
done

echo "To leave a running screen: ctrl-a d"
echo "http://www.cyberciti.biz/tips/linux-screen-command-howto.html"