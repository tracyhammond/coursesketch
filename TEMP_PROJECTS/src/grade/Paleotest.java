package grade;

//import srl.core.sketch.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import response.Response;
import srl.core.sketch.Point;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;
import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;

//import java.awt.Canvas;
public class Paleotest {
	static int pxmax = 1024;
	static int pymax = 768;
	static int current = 1776;
	//If true use remote server
	static boolean mode = true;
	static JFrame frmMain = new JFrame();
	static JPanel panel;
	static final TestSketchHolder holder = new TestSketchHolder();
	static final ActionListener updateGui = makeGui();
	static SrlUpdateList fetchUpdates(int i) {
		SrlUpdateList updates = null;

		MongoClient mongoClient;
		try {
			mongoClient = new MongoClient("goldberglinux.tamu.edu");
		} catch (UnknownHostException e) {
			System.out.println("CANNOT CONNECT TO SERVER!");
			return updates;
		}
		DB db = mongoClient.getDB("submissions");
		DBCollection collection = db.getCollection("Experiments");
		DBCursor cursor = collection.find().skip(i);
		int numbers = cursor.count();
		System.out.println("Number of submissions: " + numbers);
		DBObject object = cursor.next();
		Object obj = object.get("UpdateList");
		//Object aid = object.get("AssignmentId");
		//Object cpid = object.get("CourseProblemId");
		Object uid = object.get("UserId");
		DB ldb = mongoClient.getDB("login");
		DBCollection lcollection = ldb.getCollection("CourseSketchUsers");
		//lcollection.findOne().
		//DBCursor lcursor = lcollection.findOne().containsField("{\"ServerId\":"+uid+"\"}");
		BasicDBObject query = new BasicDBObject("ServerId",uid);
		DBCursor lcursor = lcollection.find(query);
		if (lcursor.hasNext()) {
			DBObject result = lcursor.next();
			display.studentUserName = result.get("UserName").toString();
			frmMain.setTitle("SUBMISSION#: " + current + " USER:"+result.get("UserName").toString());
		}

		try {
		updateDisplay(mongoClient.getDB("Institution"), object.get("CourseProblemId").toString());
		} catch(Exception e) {
			
		}

		updateGui.actionPerformed(new ActionEvent(updateGui, 0, "")); // updates the gui
		//frmMain.setTitle("USER:<" + uid.toString() + ">");

		try {
			byte[] bytes = (byte[]) obj;
			updates = SrlUpdateList.parseFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			System.out.println("Couldn't parse updates!");
			mongoClient.close();
			return updates;
		}
		mongoClient.close();
		return updates;
		/* FILE DIALOG NO LONGER
		} else {
			//FILE NAME MUST BE <NUMBER>.dat
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Binary files", "dat");
			chooser.setFileFilter(filter);
			int returnVal = chooser.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("You chose to open this file number: " + chooser.getSelectedFile().getName());
			} else { return updates; }
			File datafile = new File(chooser.getSelectedFile(), "");

			BufferedInputStream instream;
			try {
				instream = new BufferedInputStream(new FileInputStream(datafile));
				updates = SrlUpdateList.parseFrom(instream);
			} catch (Exception e1) {
				return updates;
			}
			return updates;
		}
		*/
	}
	
	private static ArrayList<String> toStringArray(String str) {
		ArrayList<String> strs = new ArrayList<String>();
		strs.add(str);
		return strs;
	}

	/**
	 * Uses the problem Id to look up the problem
	 * @param db
	 * @param problemId
	 * @throws DatabaseAccessException 
	 * @throws AuthenticationException 
	 */
	private static void updateDisplay(DB db, String problemId) throws AuthenticationException, DatabaseAccessException {
		final String mastId = "0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332";
		SrlProblem currentProblem =  Institution.mongoGetCourseProblem(toStringArray(problemId), mastId).get(0);
		SrlAssignment currentAssignment = Institution.mongoGetAssignment(toStringArray(currentProblem.getAssignmentId()), mastId).get(0);
		SrlCourse currentCourse = Institution.mongoGetCourses(toStringArray(currentProblem.getCourseId()), mastId).get(0);

		display.courseName = currentCourse.getName();
		display.assignmentName = currentAssignment.getName();
		display.problemName = currentProblem.getName();
		display.problemText = currentProblem.getProblemInfo().getQuestionText();
	}

	public static void main(String[] args) throws IOException, FileNotFoundException, Exception {

		// From Here Block1 Starts
		// =========================================================>
		// DemoDrawing.DemoDrawing(tester);

		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frmMain.setSize(pxmax, pymax);

		// Canvas cnvs = new Canvas();
		// cnvs.setSize(400, 400);
		panel = new JPanel() {

		private static final long serialVersionUID = -3271766505513485833L;

		@Override
		public void paint(Graphics g) {
			Sketch tester = holder.tester;
			if (tester != null) {
				Integer xmin = Integer.MAX_VALUE;
				Integer xmax = Integer.MIN_VALUE;
				Integer ymin = Integer.MAX_VALUE;
				Integer ymax = Integer.MIN_VALUE;
				int pcount = 0;
				int scount = 0;


//				PaleoSketchRecognizer recognizer = new PaleoSketchRecognizer(
//						PaleoConfig.allOn());
				PaleoConfig config = new PaleoConfig();//.basicPrimsOnly();
				List<PaleoConfig.Option> options = new ArrayList<PaleoConfig.Option>();
//				options.add(PaleoConfig.Option.Line);
//				options.add(PaleoConfig.Option.Arc);
				options.add(PaleoConfig.Option.Ellipse);
				options.add(PaleoConfig.Option.Circle);
				options.add(PaleoConfig.Option.Curve);
				options.add(PaleoConfig.Option.Helix);
				options.add(PaleoConfig.Option.Spiral);
				options.add(PaleoConfig.Option.Arrow);
				options.add(PaleoConfig.Option.Complex);
//				options.add(PaleoConfig.Option.Polyline);
				options.add(PaleoConfig.Option.Polygon);
				options.add(PaleoConfig.Option.Rectangle);
				
				options.add(PaleoConfig.Option.Square);
				options.add(PaleoConfig.Option.Diamond);

				options.add(PaleoConfig.Option.Dot);
				options.add(PaleoConfig.Option.Wave);
				options.add(PaleoConfig.Option.Gull);
				options.add(PaleoConfig.Option.Blob);
				options.add(PaleoConfig.Option.Infinity);

				config.disableOptions(options);
				PaleoSketchRecognizer recognizer = new PaleoSketchRecognizer(config);

				for (Stroke m_stroke : tester.getStrokes()) {
					scount++;
					IRecognitionResult result = recognizer.recognize(m_stroke);
					//System.out.println(result.getBestShape().getInterpretation().label);
					System.out.println(result.getBestShape());
					List<Point> points = m_stroke.getPoints();
					boolean cont = true;
					/*
					 * for(Shape x : result.getNBestList() ) { String label
					 * = x.getInterpretation().label; if(cont && label ==
					 * "Curve" || label == "Arc" ) {
					 * System.out.println("CURVE BEAUT TYPE?::!::" +
					 * x.getBeautifiedShape().getShape()); org.openawt.Shape
					 * shape = x.getBeautifiedShape().getShape();
					 * PathIterator path = shape.getPathIterator(null,0.1);
					 * points.clear(); float[] coords = new float[2];
					 * while(!path.isDone()) { path.currentSegment(coords);
					 * points.add(new Point(coords[0],coords[1]));
					 * path.next();
					 * System.out.println("SUPER SECRET INTERP PT ACTIVATE!"
					 * ); }
					 * 
					 * } cont = false; }
					 */
					if (cont == true && points.size() > 10) {
						for (int q = 0; q < 3; q++) {
							// Subdivide
							List<Point> points2 = new ArrayList<Point>(points);
							points.clear();
							for (int i = 0; i + 1 < points2.size(); i++) {
								Point p1 = points2.get(i);
								Point p2 = points2.get(i + 1);
								points.add(p1);
								points.add(new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2));
							}
							points.add(points2.get(points2.size() - 1));

							// Apply fake sinc4 .25 .5 1 .5 .25
							points2 = new ArrayList<Point>(points);
							points.clear();
							points.add(points2.get(0));
							for (int i = 1; i + 1 < points2.size(); i++) {
								Point p1 = new Point(points2.get(i).x,points2.get(i).y);
								/*
								//Unfinished fake sinc
								int width = Math.min(i, points2.size() - 2 - i);
								int total = 1;
								int weight = 1;
								for(int k = 1; (k <= width) && (i-k >= 0) && (i+k < points2.size() - 1);k ++) {
									total += weight;
									weight = weight / 2;
									Point pa = points2.get(i - k);
									Point pb = points2.get(i + k);
									p1.x += pa.x * weight + pb.x*weight;
									p1.y += pa.y * weight + pb.y*weight;
								}
								p1.x = p1.x/total;
								p1.y = p1.y/total;
								*/
								
								if (i >= 2 && i + 2 < points2.size()) {
									p1.x = (points2.get(i - 2).x / 4 + points2.get(i - 1).x / 2
											+ points2.get(i).x + points2.get(i + 1).x / 2 + points2
											.get(i + 2).x / 4) / 2.5;
									p1.y = (points2.get(i - 2).y / 4 + points2.get(i - 1).y / 2
											+ points2.get(i).y + points2.get(i + 1).y / 2 + points2
											.get(i + 2).y / 4) / 2.5;
								} else {
									p1.x = (points2.get(i - 1).x / 2 + points2.get(i).x + points2
											.get(i + 1).x / 2) / 2;
									p1.y = (points2.get(i - 1).y / 2 + points2.get(i).y + points2
											.get(i + 1).y / 2) / 2;
								}
								
								points.add(p1);
							}
							points.add(points2.get(points2.size() - 1));

							// Discard points
							/*
							 * if(q > 4){ for(int i = (q%2==0)?1:2; i+1 <
							 * points.size(); i++) { points.remove(i); } }
							 */
						}
					}
					for (Point p : points) {
						if (p.getX() < xmin)
							xmin = (int) p.getX();
						if (p.getX() > xmax)
							xmax = (int) p.getX();
						if (p.getY() < ymin)
							ymin = (int) p.getY();
						if (p.getY() > ymax)
							ymax = (int) p.getY();
						pcount++;
					}
				}

				int x_range = xmax - xmin;
				int y_range = ymax - ymin;

				for (Stroke m_stroke : tester.getStrokes()) {
					List<Point> pl = m_stroke.getPoints();
					g.setColor(Color.BLACK);
					if ((recognizer.recognize(m_stroke).getBestShape()) != null) {
						if ((recognizer.recognize(m_stroke).getBestShape().getInterpretation().label).equals("Line")) {
							g.setColor(Color.RED);
						}
						if ((recognizer.recognize(m_stroke).getBestShape().getInterpretation().label).equals("Arc")) {
							g.setColor(Color.BLUE);
						}
//						if ((recognizer.recognize(m_stroke).getBestShape().getInterpretation().label).equals("Polyline")) {
//							g.setColor(Color.RED);
//						}
					}
					Point s = pl.remove(0);
					for (Point p : pl) {
						/*
						 * System.out.println("S:(" + s.getX() + "," +
						 * s.getY() + ")  P:(" + p.getX() + "," + p.getY() +
						 * ")" + "      " + "SN:(" + (int)((s.getX() - xmin
						 * + 50) * 500 / x_range) + "," + (int)((s.getY() -
						 * ymin + 0) * 500 / y_range) + ")  PN:(" +
						 * (int)((p.getX() - xmin + 50) * 500 / x_range) +
						 * "," + (int)((p.getY() - ymin + 50) * 500 /
						 * y_range) + ")");
						 */
						
						g.drawLine((int) ((p.getX() - xmin) * (pxmax - 20) / x_range + 10),
								(int) ((p.getY() - ymin) * (pymax - 40) / y_range + 10),
								(int) ((s.getX() - xmin) * (pxmax - 20) / x_range + 10),
								(int) ((s.getY() - ymin) * (pymax - 40) / y_range + 10));
						s = p;
						// Or just use large canvas and original points, no
						// normalization and relocation
						// g.drawLine((int)p.getX(), (int)p.getY(),
						// (int)s.getX(), (int)s.getY());
						// s = p;
					}
				}
				System.out.println("Scount: " + scount);
				System.out.println("Pcount: " + pcount);
				super.paintComponents(g);
			}
		}
		};
		panel.setSize(pxmax, pymax);
		panel.setBackground(Color.white);
		panel.setOpaque(true);
		// frmMain.add(cnvs);
		frmMain.add(panel);
		frmMain.setVisible(true);
		// From Here Block1 Ends
		// =========================================================>
		 
		// New and improved network stream getter
		Scanner s = new Scanner(System.in);
		System.out.println("INPUT NUMBER!");
		current = s.nextInt();
		s.close();
		SrlUpdateList updates = fetchUpdates(current);
		
		holder.tester = Response.viewTest(updates);
		panel.repaint();
		KeyListener kl = new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				int keyCode = e.getKeyCode();
				System.out.println(e.toString());
				boolean change = false;
				switch( keyCode ){
				case KeyEvent.VK_KP_LEFT:
				case KeyEvent.VK_LEFT:
					current -= 1;
					current = (current <= 1)?1:current;
					change = true;
					System.out.println("KeyEvent.VK_LEFT");
					break;
				case KeyEvent.VK_KP_RIGHT:
				case KeyEvent.VK_RIGHT:
					current += 1;
					change = true;
					System.out.println("KeyEvent.VK_RIGHT");
					break;
				}
				if(change) {
					SrlUpdateList updates = fetchUpdates(current);
					
					try {
						holder.tester = Response.viewTest(updates);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					panel.repaint();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		};
		panel.setFocusable( true );
		panel.addKeyListener(kl);
		frmMain.setFocusable( true );
		frmMain.addKeyListener(kl);
		// From Here Block2 Starts
		// =========================================================>
		// get the boundary, used for normalization

		// System.out.println("Number of Points  contained in the file: " +
		// pcount);
		// System.out.println("Number of Strokes contained in the file: " +
		// scount);

		// System.out.println("Xmin: " + xmin + "Xmax: " + xmax + "Xrange: " +
		// x_range);
		// System.out.println("Ymin: " + ymin + "Ymax: " + ymax + "Yrange: " +
		// y_range);
		// From Here Block2 Ends
		// =========================================================>
	}
	
	// DAVID CODE

	final static NavigationDisplay display = new NavigationDisplay();

	public static ActionListener makeGui() {
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
    		}
    	};

    	totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
    	totalPanel.add(displayPanel);

    	frame.add(totalPanel);
    	frame.pack();
    	frame.setVisible(true);
    	
    	return result;
    }
	
}

class TestSketchHolder {
	public Sketch tester;
}

class NavigationDisplay {
	public String courseName;
	public String assignmentName;
	public String problemName;
	public String problemText;
	public String studentUserName;
	public long dueDate;
	public long submissionTime;
	public boolean late;
}