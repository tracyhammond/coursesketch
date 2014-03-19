package grade;
//import srl.core.sketch.*;
import static database.StringConstants.COURSE_PROBLEM_ID;

import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import srl.core.sketch.Sketch;
import srl.core.sketch.Point;
import srl.core.sketch.Stroke;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import response.Response;
import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
/*
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;
import srl.core.sketch.Point;
import srl.core.sketch.Sketch;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import response.Response;
import srl.core.sketch.*;
import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;
*/


import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;

//import java.awt.Canvas;
public class Grader {
    static int pxmax = 1024;
    static int pymax = 768;

    public static void show(String[] args) throws IOException, FileNotFoundException, Exception {
            
          JFrame frmMain = new JFrame();
          frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

          frmMain.setSize(pxmax, pymax);

          //Canvas cnvs = new Canvas();
          //cnvs.setSize(400, 400);
          final TestSketchHolder holder = new TestSketchHolder();
          JPanel panel = new JPanel() {

        	  @Override
        	  public void paint(Graphics g) {
        		  Sketch tester = holder.tester;
        		  if (tester!= null) {
        			  Integer xmin = Integer.MAX_VALUE;
        	            Integer xmax = Integer.MIN_VALUE;
        	            Integer ymin = Integer.MAX_VALUE;
        	            Integer ymax = Integer.MIN_VALUE;
        	            int pcount = 0;
        	            int scount = 0;
        	            
        	    		PaleoSketchRecognizer recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
        	    		
        	            for (Stroke m_stroke : tester.getStrokes()) {
        	            	scount++;
        	           		IRecognitionResult result = recognizer.recognize(m_stroke);
        	           		System.out.println(result.getBestShape().getInterpretation().label);
        	           		List<Point> points = m_stroke.getPoints();
        	           		boolean cont = true;
        	           		/*
        	           		for(Shape x : result.getNBestList() ) {
        	           			String label = x.getInterpretation().label;
        	           			if(cont && label == "Curve" || label == "Arc" ) {
        	           				System.out.println("CURVE BEAUT TYPE?::!::" + x.getBeautifiedShape().getShape());
        	           				org.openawt.Shape shape = x.getBeautifiedShape().getShape();
        	           				PathIterator path = shape.getPathIterator(null,0.1);
        	           				points.clear();
        	           				float[] coords = new float[2];
        	           				while(!path.isDone()) {
        	           					path.currentSegment(coords);
        	           					points.add(new Point(coords[0],coords[1]));
        	           					path.next();
        	           					System.out.println("SUPER SECRET INTERP PT ACTIVATE!");
        	           				}
        	           				
        	           			}
        	           			cont = false;
        	           		}
        	           		*/

        	           		if(cont == true && points.size() > 10) {
        	           			for(int q = 0; q < 8; q++) {
        			           		//Subdivide
        			           		List<Point> points2 = new ArrayList<Point>(points);
        			           		points.clear();
        			           		for(int i = 0; i+1 < points2.size(); i ++){
        			           			Point p1 = points2.get(i);
        			           			Point p2 = points2.get(i+1);
        			           			points.add(p1);
        			           			points.add(new Point((p1.x+p2.x)/2,(p1.y+p2.y)/2));
        			           		}
        			           		points.add(points2.get(points2.size()-1));
        			           		
        			           		//Apply fake sinc4 .25 .5 1 .5 .25
        			           		points2 = new ArrayList<Point>(points);
        			           		points.clear();
        			           		points.add(points2.get(0));
        			           		for(int i = 1; i+1 < points2.size(); i++){
        			           			Point p1 = new Point();
        			           			if(i >= 2 && i+2 < points2.size()) {
        			           				p1.x = (points2.get(i-2).x/4 + points2.get(i-1).x/2 + points2.get(i).x + points2.get(i+1).x/2 + points2.get(i+2).x/4)/2.5;
        			           				p1.y = (points2.get(i-2).y/4 + points2.get(i-1).y/2 + points2.get(i).y + points2.get(i+1).y/2 + points2.get(i+2).y/4)/2.5;
        			           			}
        			           			else {
        				           			p1.x = (points2.get(i-1).x/2 + points2.get(i).x + points2.get(i+1).x/2)/2;
        				           			p1.y = (points2.get(i-1).y/2 + points2.get(i).y + points2.get(i+1).y/2)/2;
        			           			}
        			           			points.add(p1);
        			           		}
        			           		points.add(points2.get(points2.size()-1));
        			           		
        			           		//Discard points
        			           		/*
        			           		if(q > 4){
        			           			for(int i = (q%2==0)?1:2; i+1 < points.size(); i++) {
        			           				points.remove(i);
        			           			}
        			           		}
        			           		*/
        	           			}
        	           		}
        	             	for (Point p : points) {
        	            		if (p.getX() < xmin) xmin = (int)p.getX();
        	            		if (p.getX() > xmax) xmax = (int)p.getX();
        	            		if (p.getY() < ymin) ymin = (int)p.getY();
        	            		if (p.getY() > ymax) ymax = (int)p.getY();
        	            		pcount++;
        	            	}
        	            }
        	            
        	            int x_range = xmax - xmin;
        	            int y_range = ymax - ymin;
        	            
        	            for (Stroke m_stroke : tester.getStrokes()) {
        	            	List<Point> pl = m_stroke.getPoints();
        	            	Point s = pl.remove(0);
        	            	for (Point p : pl) {
        	            		/*System.out.println("S:(" + s.getX() + "," + s.getY() + ")  P:(" + p.getX() + "," + p.getY() + ")" + 
        	            	"      " + "SN:(" + (int)((s.getX() - xmin + 50) * 500 / x_range) + "," + (int)((s.getY() - ymin + 0) * 500 / y_range)
        	            	+ ")  PN:(" + (int)((p.getX() - xmin + 50) * 500 / x_range) + "," + (int)((p.getY() - ymin + 50) * 500 / y_range) + ")");*/
        	            		g.drawLine((int)((p.getX() - xmin) * (pxmax-20) / x_range + 10), (int)((p.getY() - ymin) * (pymax-40) / y_range + 10),
        	            		(int)((s.getX() - xmin) * (pxmax-20) / x_range + 10), (int)((s.getY() - ymin) * (pymax-40) / y_range + 10));
        	            		s = p;
//        	            		Or just use large canvas and original points, no normalization and relocation
//        	            		g.drawLine((int)p.getX(), (int)p.getY(), (int)s.getX(), (int)s.getY());
//        	                    		s = p;
        	            	}
        	            }
        	    		super.paintComponents(g);
        		  }
        	  }
          };
          panel.setSize(1024, 768);
          //frmMain.add(cnvs);
          frmMain.add(panel);
          frmMain.setVisible(true);

                JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Binary files", "dat");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
               System.out.println("You chose to open this file: " +
                    chooser.getSelectedFile().getName());
            } else {
                    return;
            }
            File datafile = new File(chooser.getSelectedFile(), "");
            
            BufferedInputStream instream = new BufferedInputStream(new FileInputStream(datafile));
            SrlUpdateList updates = SrlUpdateList.parseFrom(instream);
            instream.close();
            
            System.out.println(updates.getListCount());
            
            holder.tester = Response.viewTest(updates);
            panel.repaint();
            
// From Here Block2 Starts =========================================================>    
            //get the boundary, used for normalization
            
    		//System.out.println("Number of Points  contained in the file: " + pcount);
    		//System.out.println("Number of Strokes contained in the file: " + scount);

    		//System.out.println("Xmin: " + xmin + "Xmax: " + xmax + "Xrange: " + x_range);
    		//System.out.println("Ymin: " + ymin + "Ymax: " + ymax + "Yrange: " + y_range);
// From Here Block2 Ends =========================================================>    
        }

    static DBCollection trash;
	static DBCollection experiments;
	public static void main(String args[]) throws UnknownHostException, AuthenticationException, DatabaseAccessException, InterruptedException {		
		System.out.println("Starting program");
		String mastId = "0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332";
		MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB sub = mongoClient.getDB("submissions");
		DBCollection exp = sub.getCollection("Experiments");
		experiments = exp;
		trash = sub.getCollection("Trash");
		ArrayList<String> couresId = new ArrayList<String>();
		couresId.add("52d55a580364615fe8a4496c");
		ArrayList<SrlCourse> courses = Institution.mongoGetCourses(couresId, mastId);
		final IntegerHolder k = new IntegerHolder();
		final IntegerHolder q = new IntegerHolder();
		final IntegerHolder r = new IntegerHolder();
		for (k.value = 0; k.value < courses.size(); k.value++) {
			String courseId = courses.get(k.value).getId();
			System.out.println(courses.get(k.value).getAssignmentListList());
			ArrayList<SrlAssignment> assignments = Institution.mongoGetAssignment(courses.get(k.value).getAssignmentListList(), mastId);
			System.out.println("number of assignments found: " + assignments.size());
			
			for (q.value = 0; q.value < assignments.size(); q.value++) { // 3rd and 4th are fine (which are 0 and 1)
				String assignmentId = assignments.get(q.value).getId();
				System.out.println("\n\nLooking at assignment " + assignments.get(q.value).getName() + " " + assignmentId);
				ArrayList<SrlProblem> problems =  Institution.mongoGetCourseProblem(assignments.get(q.value).getProblemListList(), mastId);
				System.out.println("number of problems found: " + problems.size());

				for (r.value = 0; r.value < problems.size(); r.value++) {
					System.out.println("\n\nLooking at problem " +  problems.get(r.value).getName() + " " + problems.get(r.value).getId());
					BasicDBObject findQuery = new BasicDBObject(COURSE_PROBLEM_ID, problems.get(r.value).getId());
					gradeProblem(exp.find(findQuery), courses.get(k.value), assignments.get(q.value), problems.get(r.value));
				}
			}
		}
	}
	private static void gradeProblem(DBCursor find, SrlCourse srlCourse, SrlAssignment srlAssignment, SrlProblem srlProblem) {
	}
}

class TestSketchHolder {
	public Sketch tester;
}

class IntegerHolder {
	public int value;
}