package response;

import java.util.LinkedList;

import protobuf.srl.commands.Commands.ActionPackageShape;
import protobuf.srl.commands.Commands.IdChain;
import protobuf.srl.commands.Commands.SrlCommand;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlStroke;

import srl.core.sketch.Sketch;
//import srl.recognition.IRecognitionResult;
import srl.recognition.paleo.PaleoConfig;
//import srl.recognition.paleo.PaleoSketchRecognizer;

public class Response {
	//private PaleoSketchRecognizer m_recognizer;
	private UpdateList m_syncList;
	private Sketch m_drawspace;

	/**
	 * Default constructor that initializes PaleoSketch with all primitives on
	 */
	public Response(){
		//Instantiate All sketch recognition objects/recognizers
	}

	/**
	 * optional constructor to specify what primitives you want on
	 * @param PaleoSketch configuration of which primitives you would like
	 */
	/*
	public Response(PaleoConfig config){
		//Instantiate All sketch recognition objects
	}
	*/

	/**
	 * Advanced function that takes an update as a list of commands and
	 * interprets multiple things at once, including
	 * @param protobuf.srl.commands.Commands.Update
	 * @return protobuf.srl.sketch.Sketch.SrlStroke
	 * @throws Exception
	 */
	public SrlUpdate interpret(SrlUpdate call) throws Exception{
		m_syncList.add(parseUpdate(call));
		m_syncList.executeLast(m_drawspace);

		Update result = new Update();

		//perform recognition

		result.setTime(System.currentTimeMillis());
		m_syncList.add(result);
		m_syncList.executeLast(m_drawspace);

		return repackage(result);
	}

	/**
	 * Parses a Protobuf type update into a usable commands
	 * @param protobuf.srl.commands.Commands.Update
	 * @throws Exception Unsupported Command
	 */
	private Update parseUpdate(SrlUpdate call) throws Exception{
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
		return up;
	}
	
	/**
	 * Empty function designed to return a properly packaged update
	 * @param u Update
	 * @return SrlUpdate
	 * @throws Exception Unsupported Command
	 */
	private SrlUpdate repackage(Update u) throws Exception{
		SrlUpdate.Builder updateBuilder = SrlUpdate.newBuilder();
		
		updateBuilder.setTime(u.getTime());
		
		LinkedList<SrlCommand> container = new LinkedList<SrlCommand>();
		for(Command c: u.getCommandList()){
			SrlCommand.Builder com = SrlCommand.newBuilder();
			
			com.setCommandType(c.getType());
			com.setCommandData(c.toByteString());
			
			container.add(com.build());
		}
		updateBuilder.addAllCommands(container);
		
		return updateBuilder.build();
	}
}
