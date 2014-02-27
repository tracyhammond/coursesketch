package response;

import java.util.LinkedList;
import java.util.List;

import protobuf.srl.commands.Commands.ActionPackageShape;
import protobuf.srl.commands.Commands.IdChain;
import protobuf.srl.commands.Commands.SrlCommand;
import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlStroke;

import srl.core.sketch.Sketch;
import srl.recognition.paleo.PaleoConfig;
import srl.recognition.paleo.PaleoSketchRecognizer;

public class Response {
	private PaleoSketchRecognizer m_recognizer;
	private UpdateDeque m_syncDeque;
	private Sketch m_drawspace;
	
	/**
	 * Default constructor that initializes the recognizer with all primitives on
	 */
	public Response(){
		m_syncDeque = new UpdateDeque();
		m_drawspace = new Sketch();
		m_recognizer = new PaleoSketchRecognizer(PaleoConfig.allOn());
	}
	
	/**
	 * optional constructor to specify recognition domain
	 * @param domain
	 */
	public Response(PaleoConfig domain){
		m_syncDeque = new UpdateDeque();
		m_drawspace = new Sketch();
		m_recognizer = new PaleoSketchRecognizer(domain);
	}
	
	/**
	 * Point of communication, takes an update and runs the recognizer
	 * and returns the results
	 * @param protobuf.srl.commands.Commands.Update
	 * @return protobuf.srl.sketch.Sketch.SrlStroke
	 * @throws Exception Unsupported Command
	 */
	public SrlUpdate recognize(SrlUpdate call) throws Exception{
		m_syncDeque.add(parseUpdate(call));
		m_syncDeque.executeLast(m_drawspace);
		if(m_syncDeque.front().getStroke() == null)
			return null;
		
		//perform recognition
		Update actions = new Update();
		actions.add(new AddShape(m_recognizer.recognize(m_syncDeque.front().getStroke())));
		
		List<String> ids = new LinkedList<String>();
		ids.add(m_syncDeque.front().getStroke().getId().toString());
		actions.add(new PackageShape(null, m_syncDeque.front().getShape(), ids));
		
		actions.setTime(System.currentTimeMillis());
		m_syncDeque.add(actions);
		m_syncDeque.executeLast(m_drawspace);
		
		return repackage(actions);
	}
	
	public static Sketch viewTest(SrlUpdateList updates) throws Exception {
		Sketch returnSketch = new Sketch();
		UpdateDeque list = new UpdateDeque();
		for(SrlUpdate u : updates.getListList()) {
			list.add(parseUpdate(u));
			//list.executeLast(returnSketch);
		}
		//returnSketch = new Sketch();
		list.executeAll(returnSketch);
		return returnSketch;
	}
	
	/**
	 * Parses a Protobuf type update into a usable commands
	 * @param protobuf.srl.commands.Commands.Update
	 * @throws Exception Unsupported Command
	 */
	private static Update parseUpdate(SrlUpdate call) throws Exception{
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
			case UNDO:
				com = new UndoObject();
				break;
			case REDO:
				com = new RedoObject();
				break;
			case MARKER:
				com = null;
				break;
			default:
				throw new Exception("Unsupported command: "+c.getCommandType());
			}
			if(com != null){
				up.add(com);
			}
		}
		return up;
	}
	
	/**
	 * Empty function designed to return a properly packaged update
	 * @param u Update
	 * @return SrlUpdate
	 * @throws Exception Unsupported Command
	 */
	private static SrlUpdate repackage(Update u) throws Exception{
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