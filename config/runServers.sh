#start mongo
name="mongo"
command1="mongod"
screen -d -m -S "$name" bash -c "$command1"
echo "attach: screen -r $name"
sleep 1 #wait 4 seconds before starting up the servers
echo "waiting 4 seconds for mongo to start"
sleep 1
echo "waiting 3 seconds for mongo to start"
sleep 1
echo "waiting 2 seconds for mongo to start"
sleep 1
echo "waiting 1 seconds for mongo to start"
sleep 1
echo "Starting up other servers"
echo ""

#start up the servers
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
		command1="java -jar $fullPath local"
		screen -d -m -S "${screenName[$i]}" bash -c "$command1"
		echo "attach: screen -r ${screenName[$i]}"
	fi
done

sleep 2 #wait 5 seconds before starting up the servers
echo "To leave a running screen: ctrl-a d"
echo "http://www.cyberciti.biz/tips/linux-screen-command-howto.html"