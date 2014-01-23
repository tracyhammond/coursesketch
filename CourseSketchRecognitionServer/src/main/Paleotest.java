package main;

//import srl.core.sketch.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import srl.core.sketch.Sketch;

import protobuf.srl.commands.Commands.SrlUpdateList;

import response.Response;

public class Paleotest {
	public static void main(String[] args) throws IOException, FileNotFoundException, Exception {
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
	}
}
