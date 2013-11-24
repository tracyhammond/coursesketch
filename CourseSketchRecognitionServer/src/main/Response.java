package main;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;

<<<<<<< HEAD
import protobuf.srl.commands.Commands.SrlCommand;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.sketch.Sketch.Interpretation;
import protobuf.srl.sketch.Sketch.SrlShape;
=======
import protobuf.srl.commands.Commands.Update;
import protobuf.srl.commands.Commands.AddStroke;
import connection.Decoder;
>>>>>>> parent of 3592875... added print and the workings for real recognition
import protobuf.srl.sketch.Sketch.SrlStroke;
import protobuf.srl.sketch.Sketch.SrlPoint;
import srl.core.sketch.Point;
import srl.core.sketch.Stroke;

public class Response {
	private static PaleoSketchRecognizer m_recognizer;
	private UpdateList m_SyncList; 
	
	/**
	 * Default constructor that initializes PaleoSketch with all primitives on
	 */
	public Response(){
		if(m_recognizer == null)
			m_recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
	}
	
	/**
	 * optional constructor to specify what primitives you want on
	 * @param PaleoSketch configuration of which primitives you would like
	 */
	public Response(PaleoConfig config){
		m_recognizer = new PaleoSketchRecognizer(config);
	}
	
	/**
	 * Simple response function to take in a Protobuf file, convert it into Paleosketch
	 * and then manipulate it and package it back up to return
	 * @param protobuf.srl.commands.Commands.Update
	 * @return protobuf.srl.sketch.Sketch.SrlStroke
	 * @throws InvalidProtocolBufferException
	 */
	public SrlStroke mirror(SrlUpdate up) throws InvalidProtocolBufferException{
		Stroke s = unpackage(up);
		
		ArrayList<Point> points = (ArrayList<Point>) s.getPoints();
		for (Point p: points){
			double z = p.x;
			p.x = p.y;
			p.y = z;
		}
		s.setPoints(points);
<<<<<<< HEAD

		SrlStroke response = repackage(s);
		return response;
	}
	
	/**
	 * Void function that merely prints out the recognized details of the
	 * stroke contained up
	 * @param Update up
	 * @throws InvalidProtocolBufferException
	 */
	public void print(SrlUpdate up) throws InvalidProtocolBufferException{
		Sketch sketch = new Sketch();
		sketch.add(unpackage(up));
		
		IRecognitionResult result = m_recognizer.recognize(sketch.getFirstStroke());
		
		result.sortNBestList();
		List<Shape> shapes = result.getNBestList();
		System.out.println("Sorted result list of size " + shapes.size());
		for (Shape s: shapes){
			System.out.println("Shape "+s.getName()+", "+s.getShapes().size());
			List<srl.core.sketch.Interpretation> interpretations = s.getNBestList();
			for (srl.core.sketch.Interpretation i: interpretations){
				System.out.println("Interpretation "+i.label+", confidence "+i.confidence);
			}
			System.out.println();
		}
	}
	
	/**
	 * Simple response function to take in a Protobuf file, convert it into Paleosketch
	 * and then recognize it and package up the results for display
	 * @param protobuf.srl.commands.Commands.Update
	 * @return protobuf.srl.sketch.Sketch.SrlStroke
	 * @throws InvalidProtocolBufferException
	 */
	public SrlShape interpret(SrlUpdate up) throws InvalidProtocolBufferException{
		Sketch sketch = new Sketch();
		sketch.add(unpackage(up));
		
		IRecognitionResult result = m_recognizer.recognize(sketch.getFirstStroke());
		
		SrlShape response = repackage(result);
=======
		
		SrlStroke response = repackage(s);
>>>>>>> parent of 3592875... added print and the workings for real recognition
		return response;
	}
	
	/**
	 * Converts a Protobuf type update into a Paleosketch type stroke
	 * @param protobuf.srl.commands.Commands.Update
	 * @return srl.core.sketch.Stroke
	 * @throws InvalidProtocolBufferException
	 */
	private static Stroke unpackage(SrlUpdate up) throws InvalidProtocolBufferException{
		System.out.println("Number of commands " + up.getCommandsCount());
		Stroke stroke = null;
		for(int i=0;i<up.getCommandsCount();i++){
			SrlCommand c = up.getCommands(i);
			switch(c.getCommandType()){
			case ADD_STROKE:
				stroke = new Stroke();
				SrlStroke s_stroke = SrlStroke.parseFrom(c.getCommandData());
				stroke.setId(Stroke.nextID());
				stroke.setName(s_stroke.getName());
				for (SrlPoint s_point : s_stroke.getPointsList()) {
					stroke.addPoint(new Point(s_point.getX(), s_point.getY(), s_point.getTime(),Point.nextID()));
				}
				break;
			case ADD_SHAPE:
				break;
			case PACKAGE_SHAPE:
				break;
			}
		}
		return stroke;
	}
	
	/**
	 * Repackages a Paleosketch type stroke into a Protobuf type SrlStroke
	 * @param srl.core.sketch.Stroke
	 * @return protobuf.srl.sketch.Sketch.SrlStroke
	 */
	private SrlStroke repackage(Stroke s){
		SrlStroke.Builder strokebuilder = SrlStroke.newBuilder();
		
		strokebuilder.setId(s.getId().toString());
		strokebuilder.setTime(s.getTimeEnd());
		strokebuilder.setName(s.getName());
		
		for (Point p: s.getPoints()){
			SrlPoint.Builder pointbuilder = SrlPoint.newBuilder();
			pointbuilder.setX(p.x);
			pointbuilder.setY(p.y);
			pointbuilder.setTime(p.time);
			pointbuilder.setId(p.getId().toString());
			strokebuilder.addPoints(pointbuilder.build());
		}
		
		return strokebuilder.build();
	}
<<<<<<< HEAD
	
	/**
	 * Repackages a Paleosketch interpretation into a Protobuf type SrlShape
	 * @param srl.core.sketch.Stroke
	 * @return protobuf.srl.sketch.Sketch.SrlStroke
	 */
	private SrlShape repackage(IRecognitionResult result){
		SrlShape.Builder shapebuilder = SrlShape.newBuilder();
		
		result.sortNBestList();
		List<Shape> shapes = result.getNBestList();
		
		shapebuilder.setId(shapes.get(0).getId().toString());
		shapebuilder.setTime(shapes.get(0).getTimeEnd());
		
		Interpretation.Builder interpretationbuilder = Interpretation.newBuilder();
		for (Shape s: shapes){
			srl.core.sketch.Interpretation i = s.getInterpretation();
			interpretationbuilder.setLabel(i.label);
			interpretationbuilder.setConfidence(i.confidence);
			
			shapebuilder.addInterpretations(interpretationbuilder.build());
		}
		
		return shapebuilder.build();
	}
=======
>>>>>>> parent of 3592875... added print and the workings for real recognition
}
