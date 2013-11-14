package response;

import java.util.LinkedList;

import protobuf.srl.commands.Commands.ActionPackageShape;
import protobuf.srl.commands.Commands.IdChain;
import protobuf.srl.commands.Commands.SrlCommand;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlStroke;

import srl.core.sketch.Sketch;
import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;

public class Response {
	private PaleoSketchRecognizer m_recognizer;
	private UpdateList m_syncList;
	private Sketch m_drawspace;
	
	/**
	 * Default constructor that initializes PaleoSketch with all primitives on
	 */
	public Response(){
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
	 * Advanced function that takes an update as a list of commands and
	 * interprets multiple things at once, including
	 * @param protobuf.srl.commands.Commands.Update
	 * @return protobuf.srl.sketch.Sketch.SrlStroke
	 * @throws Exception
	 */
	public void interpret(SrlUpdate call) throws Exception{
		parseUpdate(call);
		m_syncList.executeLast(m_drawspace);
		
		IRecognitionResult result = m_recognizer.recognize(m_drawspace.getLastStroke());
		
	}
	
	/**
	 * Parses a Protobuf type update into a usable commands
	 * @param protobuf.srl.commands.Commands.Update
	 * @throws Exception Unsupported Command
	 */
	private void parseUpdate(SrlUpdate call) throws Exception{
		System.out.println("Number of commands " + call.getCommandsCount());
		Update up = new Update(call.getTime());
		
		for(SrlCommand c: call.getCommandsList()){
			Command com;
			switch(c.getCommandType()){
			case ADD_STROKE:
				com = new AddStroke(SrlStroke.parseFrom(c.getCommandData()));
				break;
			case ADD_SHAPE:
				com = new AddShape(SrlShape.parseFrom(c.getCommandData()));
				break;
			case PACKAGE_SHAPE:
				com = new PackageShape(ActionPackageShape.parseFrom(c.getCommandData()));
				break;
			case REMOVE_OBJECT:
				com = new RemoveObject(IdChain.parseFrom(c.getCommandData()));
				break;
			default:
				throw new Exception("Unsupported command: "+c.getCommandType());
			}
			up.add(com);
		}
		m_syncList.add(up);
	}
	
	/**
	 * Empty function designed to return a properly packaged update
	 * @param u Update
	 * @return SrlUpdate
	 * @throws Exception Unsupported Command
	 */
	private SrlUpdate repackage(Update u) throws Exception{
		SrlUpdate.Builder build = SrlUpdate.newBuilder();
		
		build.setTime(u.getTime());
		LinkedList<SrlCommand> container = new LinkedList<SrlCommand>();
		for(Command c: u.getCommandList()){
			SrlCommand.Builder com = SrlCommand.newBuilder();
			switch(c.getType()){
			case ADD_STROKE:
				break;
			case ADD_SHAPE:
				break;
			case PACKAGE_SHAPE:
				break;
			case REMOVE_OBJECT:
				break;
			default:
				throw new Exception("Unsupported command: "+c.getType());
			}
			container.add(com.build());
		}
		build.addAllCommands(container);
		
		return build.build();
	}
}
