package main;

import java.util.List;
import java.util.UUID;

import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.commands.Commands.Update;
import connection.Decoder;
import protobuf.srl.sketch.Sketch.Interpretation;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlStroke;
import protobuf.srl.sketch.Sketch.SrlPoint;
import srl.core.sketch.Point;
import srl.core.sketch.Shape;
import srl.core.sketch.Sketch;
import srl.core.sketch.Stroke;
import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;

public class Response {
	
	/**
	 * Simple response function to take in a Protobuf file, convert it into Paleosketch
	 * and then manipulate it and package it back up to return
	 * @param protobuf.srl.commands.Commands.Update
	 * @return protobuf.srl.sketch.Sketch.SrlStroke
	 * @throws InvalidProtocolBufferException
	 */
	public static SrlStroke mirror(Update up) throws InvalidProtocolBufferException{
		Stroke s = unpackage(up);

		List<Point> points = s.getPoints();
		for (Point p: points){
			//System.out.println("("+p.x+","+p.y+")");
			double z = p.x;
			p.x = p.y;
			p.y = z;
		}
		s.setPoints(points);

		SrlStroke response = repackage(s);
		return response;
	}
	
	/**
	 * Void function that merely prints out the recognized details of the
	 * stroke contained up
	 * @param Update up
	 * @throws InvalidProtocolBufferException
	 */
	public static void print(Update up) throws InvalidProtocolBufferException{
		Sketch sketch = new Sketch();
		sketch.add(unpackage(up));
		
		PaleoSketchRecognizer recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
		IRecognitionResult result = recognizer.recognize(sketch.getFirstStroke());
		
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
	public static SrlShape interpret(Update up) throws InvalidProtocolBufferException{
		Sketch sketch = new Sketch();
		sketch.add(unpackage(up));
		
		PaleoSketchRecognizer recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
		IRecognitionResult result = recognizer.recognize(sketch.getFirstStroke());
		
		SrlShape response = repackage(result);
		return response;
	}
	
	/**
	 * Converts a Protobuf type update into a Paleosketch type stroke
	 * @param protobuf.srl.commands.Commands.Update
	 * @return srl.core.sketch.Stroke
	 * @throws InvalidProtocolBufferException
	 */
	public static Stroke unpackage(Update up) throws InvalidProtocolBufferException{
		Stroke stroke = new Stroke();
		System.out.println("Number of commands " + up.getCommandsCount());
		SrlStroke s_stroke = SrlStroke.parseFrom(up.getCommands(0).getCommandData());
		stroke.setId(Stroke.nextID());
		stroke.setName(s_stroke.getName());
		for (SrlPoint s_point : s_stroke.getPointsList()) {
			stroke.addPoint(new Point(s_point.getX(), s_point.getY(), s_point.getTime(),Point.nextID()));
		}		
		return stroke;
	}
	
	/**
	 * Repackages a Paleosketch type stroke into a Protobuf type SrlStroke
	 * @param srl.core.sketch.Stroke
	 * @return protobuf.srl.sketch.Sketch.SrlStroke
	 */
	public static SrlStroke repackage(Stroke s){
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
	
	/**
	 * Repackages a Paleosketch interpretation into a Protobuf type SrlShape
	 * @param srl.core.sketch.Stroke
	 * @return protobuf.srl.sketch.Sketch.SrlStroke
	 */
	public static SrlShape repackage(IRecognitionResult result){
		SrlShape.Builder shapebuilder = SrlShape.newBuilder();
		
		result.sortNBestList();
		List<Shape> shapes = result.getNBestList();
		
		shapebuilder.setId(shapes.get(0).getId().toString());
		shapebuilder.setTime(shapes.get(0).getTimeEnd());
		
		Interpretation.Builder interpretationbuilder = Interpretation.newBuilder();
		for (Shape s: shapes){
			srl.core.sketch.Interpretation i = s.getInterpretation();
			interpretationbuilder.setName(i.label);
			interpretationbuilder.setConfidence(i.confidence);
			
			shapebuilder.addInterpretations(interpretationbuilder.build());
		}
		
		return shapebuilder.build();
	}
}
