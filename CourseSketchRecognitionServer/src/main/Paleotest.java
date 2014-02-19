package main;

//import srl.core.sketch.*;
import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import srl.core.sketch.Sketch;
import protobuf.srl.commands.Commands.SrlUpdateList;
import response.Response;
import srl.core.sketch.*;
import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;

//import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Paleotest {
        public static void main(String[] args) throws IOException, FileNotFoundException, Exception {

// From Here Block1 Starts =========================================================>        	
        	//          DemoDrawing.DemoDrawing(tester);
            
          JFrame frmMain = new JFrame();
          frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frmMain.setSize(1024, 768);

          //Canvas cnvs = new Canvas();
          //cnvs.setSize(400, 400);
          JPanel panel = new JPanel();
          panel.setSize(1024, 768);
          
          //frmMain.add(cnvs);
          frmMain.add(panel);
          frmMain.setVisible(true);

          Graphics g = panel.getGraphics();
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
            
            Sketch tester = Response.viewTest(updates);
           
// From Here Block2 Starts =========================================================>    
            //get the boundary, used for normalization
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
             	for (Point p : m_stroke.getPoints()) {
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
            		g.drawLine((int)((p.getX() - xmin + 50) * 500 / x_range), (int)((p.getY() - ymin + 50) * 500 / y_range),
            		(int)((s.getX() - xmin + 50) * 500 / x_range), (int)((s.getY() - ymin + 50) * 500 / y_range));
            		s = p;
//            		Or just use large canvas and original points, no normalization and relocation
//            		g.drawLine((int)p.getX(), (int)p.getY(), (int)s.getX(), (int)s.getY());
//                    		s = p;
            	}
            }
    		panel.paintComponents(g);
    		//System.out.println("Number of Points  contained in the file: " + pcount);
    		//System.out.println("Number of Strokes contained in the file: " + scount);

    		//System.out.println("Xmin: " + xmin + "Xmax: " + xmax + "Xrange: " + x_range);
    		//System.out.println("Ymin: " + ymin + "Ymax: " + ymax + "Yrange: " + y_range);
// From Here Block2 Ends =========================================================>    
        }
}