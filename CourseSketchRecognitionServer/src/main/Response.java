package main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.protobuf.InvalidProtocolBufferException;

import protobuf.srl.commands.Commands.Update;
import protobuf.srl.commands.Commands.AddStroke;
import connection.Decoder;
import protobuf.srl.sketch.Sketch.SrlStroke;
import protobuf.srl.sketch.Sketch.SrlPoint;
import srl.core.sketch.Point;
import srl.core.sketch.Stroke;

public class Response {
	public static SrlStroke mirror(Update up) throws InvalidProtocolBufferException{
		Stroke s = unpackage(up);
		
		ArrayList<Point> points = (ArrayList<Point>) s.getPoints();
		for (Point p: points){
			System.out.println("("+p.x+","+p.y+")");
			double z = p.x;
			p.x = p.y;
			p.y = z;
		}
		s.setPoints(points);
		
		SrlStroke response = repackage(s);
		return response;
	}
	
	public static Stroke unpackage(Update up) throws InvalidProtocolBufferException{
		Stroke stroke = new Stroke();
		SrlStroke s_stroke = SrlStroke.parseFrom(((AddStroke)Decoder.prarseCommand(up.getCommands(0))).getStroke());
		stroke.setId(Stroke.nextID());
		stroke.setName(s_stroke.getName());
		for (SrlPoint s_point : s_stroke.getPointsList()) {
			stroke.addPoint(new Point(s_point.getX(), s_point.getY(), s_point.getTime()));
		}		
		return stroke;
	}
	
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
			strokebuilder.addPoints(pointbuilder.build());
		}
		
		return strokebuilder.build();
	}
}
