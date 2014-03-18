package main;

//import srl.core.sketch.*;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import srl.core.sketch.Point;
import srl.core.sketch.Sketch;
import protobuf.srl.commands.Commands.SrlUpdateList;
import response.Response;
import srl.core.sketch.*;
import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;

import org.openawt.geom.PathIterator;

//import java.awt.Canvas;
public class Paleotest {
    static int pxmax = 1024;
    static int pymax = 768;
        public static void main(String[] args) throws IOException, FileNotFoundException, Exception {

// From Here Block1 Starts =========================================================>        	
        	//          DemoDrawing.DemoDrawing(tester);
            
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
// From Here Block1 Ends =========================================================>
        	
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

}

class TestSketchHolder {
	public Sketch tester;
}