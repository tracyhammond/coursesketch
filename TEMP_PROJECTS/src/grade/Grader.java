package grade;
//import srl.core.sketch.*;
import static util.StringConstants.COURSE_PROBLEM_ID;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSubmission;

import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;

//import java.awt.Canvas;
public class Grader {
	final static NavigationHolder courseNavigation = new NavigationHolder();
	final static NavigationHolder assignmentNavigation = new NavigationHolder();
	final static NavigationHolder problemNavigation = new NavigationHolder();
	final static NavigationHolder sketchNavigation = new NavigationHolder();
	final static NavigationDisplay display = new NavigationDisplay();
	final static SketchPanel sketchDisplay = new SketchPanel(); 

    public static void main(String[] args) throws Exception {
    	ActionListener navigator = createRefreshListener();
    	makeGui(navigator);
    }

    public static ActionListener createRefreshListener() throws AuthenticationException, UnknownHostException {
    	ArrayList<String> courseId = new ArrayList<String>();
    	courseId.add("52d55a580364615fe8a4496c");

    	final String mastId = "0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332";
    	final ArrayList<SrlCourse> courses = Institution.mongoGetCourses(courseId, mastId);
    	courseNavigation.size = courses.size();
    	
    	MongoClient client = new MongoClient("goldberglinux.tamu.edu");
    	final DBCollection experiments = client.getDB("submissions").getCollection("Experiments");
    	final DBCollection users = client.getDB("login").getCollection("CourseSketchUsers");
    	
    	return new ActionListener() {
    		SrlCourse currentCourse;
    		SrlAssignment currentAssignment;
    		SrlProblem currentProblem;
    		SrlExperiment currentExperiment;

    		ArrayList<SrlAssignment> currentAssignments = new ArrayList<SrlAssignment>();
    		ArrayList<SrlProblem> currentProblems = new ArrayList<SrlProblem>();
    		ArrayList<SrlExperiment> currentExperiments  = new ArrayList<SrlExperiment>();
    		@Override
			public void actionPerformed(ActionEvent arg0) {
    			// we assume that all values are correct.
    			if (courseNavigation.changed) {
    				// we need to change everything
    				try {
						changeCourse();
    				} catch (Exception e) {
						e.printStackTrace();
					}
    				courseNavigation.changed = false;
    			} else if (assignmentNavigation.changed) {
    				try {
						changeAssignment();
    				} catch (Exception e) {
						e.printStackTrace();
					}
    				assignmentNavigation.changed = false;
    			}  else if (problemNavigation.changed) {
    				try {
						changeProblem();
					} catch (Exception e) {
						e.printStackTrace();
					}
    				problemNavigation.changed = false;
    			}  else if (sketchNavigation.changed) {
    				try {
    					changeSketch();
    				} catch (Exception e) {
						e.printStackTrace();
					}
    				sketchNavigation.changed = false;
    			}
    			System.out.println("Done changing now setting values");
    			// set values for displaying to gui frame
    			display.courseName = currentCourse.getName();
    			display.assignmentName = currentAssignment.getName();
    			display.problemName = currentProblem.getName();
    			display.problemText = currentProblem.getProblemInfo().getQuestionText();
    			display.dueDate = currentAssignment.getDueDate().getMillisecond();
    			if (currentExperiment != null) {
	    			display.submissionTime = currentExperiment.getSubmission().getSubmissionTime();
	    			if (display.submissionTime < display.dueDate) {
	    				display.late = false;
	    			}
    			} else {
    				display.late = true;
    			}
    			// display sketch!
    			try {
				//	sketchDisplay.setSketch(SrlUpdateList.parseFrom(currentExperiment.getSubmission().getUpdateList()));
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}

    		private void changeCourse() throws AuthenticationException, DatabaseAccessException {
    			System.out.println("Changing Course");
    			currentCourse = courses.get(courseNavigation.value);
    			String courseId = currentCourse.getId();
    			currentAssignments = Institution.mongoGetAssignment(currentCourse.getAssignmentListList(), mastId);
    			assignmentNavigation.size = currentAssignments.size();
    			assignmentNavigation.value = 0;
    			changeAssignment();
    		}

    		private void changeAssignment() throws AuthenticationException, DatabaseAccessException {
    			System.out.println("Changing Assignment");
    			currentAssignment = currentAssignments.get(assignmentNavigation.value);
    			currentProblems =  Institution.mongoGetCourseProblem(currentAssignment.getProblemListList(), mastId);
    			problemNavigation.value = 0;
    			problemNavigation.size = currentProblems.size();
    			changeProblem();
    		}

    		private void changeProblem() throws AuthenticationException, DatabaseAccessException {
    			System.out.println("Changing Problem");
    			currentProblem = currentProblems.get(problemNavigation.value);
    			currentProblems =  Institution.mongoGetCourseProblem(currentAssignment.getProblemListList(), mastId);
    			BasicDBObject findQuery = new BasicDBObject(COURSE_PROBLEM_ID, currentProblem.getId());
    			DBCursor dbCursor = experiments.find(findQuery);
    			currentExperiments = new ArrayList<SrlExperiment>();
    			System.out.println("Looking at " + dbCursor.count() + " Sketches");
    			while (dbCursor.hasNext()) {
    				DBObject obj = dbCursor.next();
    				//UserId
    				//time
    				Object result = obj.get(util.StringConstants.USER_ID);
    				if (result != null && !result.equals("")) {
    					String userId = (String) result;
    					SrlExperiment.Builder nextExperiment = SrlExperiment.newBuilder();
    					nextExperiment.setAssignmentId(currentAssignment.getId());
    					nextExperiment.setCourseId(currentCourse.getId());
    					nextExperiment.setProblemId(currentProblem.getId());
    					nextExperiment.setUserId(userId);

    					SrlSubmission.Builder build = SrlSubmission.newBuilder();
    					build.setSubmissionTime((Long) obj.get("time"));
    					build.setId(obj.get(util.StringConstants.SELF_ID).toString());
    					byte[] byteArray = (byte[])obj.get("UpdateList");
    					build.setUpdateList(ByteString.copyFrom(byteArray));
    					nextExperiment.setSubmission(build);

    					currentExperiments.add(nextExperiment.build());
    				}
    			}
    			sketchNavigation.size = currentExperiments.size();
				sketchNavigation.value = 0;
    			changeSketch();
    		}

    		private void changeSketch() {
    			System.out.println("Changing Sketch");
    			try {
	    			currentExperiment = currentExperiments.get(sketchNavigation.value);
	    			display.studentUserName = "" + users.find(new BasicDBObject("ServerId", currentExperiment.getUserId())).next().get("UserName");
    			} catch(Exception e) {
    				currentExperiment = null;
    				display.studentUserName = "No Sketches";
    			}
    		}

    	};
    }

    public static void makeGui(final ActionListener refresh) {
    	JFrame frame = new JFrame();
    	frame.setVisible(false);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
    	JPanel totalPanel = new JPanel();

    	final JPanel displayPanel = new JPanel();
    	final JLabel courseNumbers = new JLabel("Course 0 out of 0");
    	final JLabel courseName = new JLabel("Course:");
    	final JLabel assignmentNumbers = new JLabel("Assignmetn 0 out of 0");
    	final JLabel assignmentName  = new JLabel("Assignment:");
    	final JLabel problemNumbers = new JLabel("Problem 0 out of 0");
    	final JLabel problemName  = new JLabel("Problem:");
    	final JTextArea problemText = new JTextArea("Question Text:");
    	final JLabel sketchNumbers = new JLabel("Sketch 0 out of 0");
    	final JLabel studentUserName = new JLabel("UserName:");
    	final JLabel dueDate = new JLabel("DueDate:");
    	final JLabel submissionTime = new JLabel("Submission:");
    	final JLabel late = new JLabel("Late:");

    	problemText.setEditable(false);
    	problemText.setColumns(50);
    	problemText.setRows(4);
    	problemText.setLineWrap(true);

    	displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
    	displayPanel.add(courseNumbers);
    	displayPanel.add(courseName);
    	displayPanel.add(assignmentNumbers);
    	displayPanel.add(assignmentName);
    	displayPanel.add(dueDate);
    	displayPanel.add(problemNumbers);
    	displayPanel.add(problemName);
    	displayPanel.add(problemText);
    	displayPanel.add(sketchNumbers);
    	displayPanel.add(studentUserName);
    	displayPanel.add(submissionTime);
    	displayPanel.add(late);

    	
    	ActionListener result = new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			try {
    				refresh.actionPerformed(e);
    			} catch(Exception e2) {
    				e2.printStackTrace();
    			}
    			courseName.setText("Course: " + display.courseName);
    			assignmentName.setText("Assignment: " + display.assignmentName);
    			dueDate.setText("Due Date: " + display.dueDate);
    			problemName.setText("Problem: " + display.problemName);
    			problemText.setText("Question Text: " + display.problemText);
    			studentUserName.setText("UserName: " + display.studentUserName);
    			submissionTime.setText("Submission Time: " + display.submissionTime);
    			if (display.late) {
    				late.setText("LATE");
    				late.setForeground(new Color(255, 0, 0));
    			} else {
    				late.setText("ON-TIME");
    				late.setForeground(new Color(0, 255, 0));
    			}

    			courseNumbers.setText("Course " + (courseNavigation.value + 1) + " out of " + courseNavigation.size);
    			assignmentNumbers.setText("Assignment " + (assignmentNavigation.value + 1) + " out of " + assignmentNavigation.size);
    			problemNumbers.setText("Problem " + (problemNavigation.value + 1) + " out of " + problemNavigation.size);
    			sketchNumbers.setText("Sketch " + (sketchNavigation.value + 1) + " out of " + sketchNavigation.size);
    		}
    	};

    	JPanel buttonPanel = new JPanel();
    	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    	buttonPanel.add(makeButton(courseNavigation, "Navigate Course", result));
    	buttonPanel.add(makeButton(assignmentNavigation, "Navigate Assignment", result));
    	buttonPanel.add(makeButton(problemNavigation, "Navigate Problem", result));
    	buttonPanel.add(makeButton(sketchNavigation, "Navigate Student", result));

    	totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
    	totalPanel.add(displayPanel);
    	totalPanel.add(buttonPanel);
    	
    	frame.add(totalPanel);
    	frame.pack();
    	frame.setVisible(true);
    }

    public static JPanel makeButton(final NavigationHolder hold, String text, final ActionListener refresh) {
    	JPanel panel = new JPanel();
    	JButton left = new JButton();
    	left.setText("Previous");
    	left.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			if (hold.value <= 0) {
    				hold.value = hold.size - 1;
    			} else {
    				hold.value -= 1;
    			}
    			hold.changed = true;
    			refresh.actionPerformed(e);
    		}
    	});

    	JButton right = new JButton();
    	right.setText("Next");
    	right.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			if (hold.value >= hold.size) {
    				hold.value = 0;
    			} else {
    				hold.value += 1;
    			}
    			hold.changed = true;
    			refresh.actionPerformed(e);
    		}
    	});

    	JButton reset = new JButton();
    	reset.setText("Reset");
    	reset.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			hold.value = 0;
    			hold.changed = true;
    			refresh.actionPerformed(e);
    		}
    	});
    	
    	panel.add(new JLabel(text));
    	panel.add(left);
    	panel.add(right);
    	panel.add(reset);

    	return panel;
    }


    static class NavigationHolder {
		public int value;
		public int size;
		public boolean changed;
	}

	static class NavigationDisplay {
		public String courseName;
		public String assignmentName;
		public String problemName;
		public String problemText;
		public String studentUserName;
		public long dueDate;
		public long submissionTime;
		public boolean late;
	}
}



