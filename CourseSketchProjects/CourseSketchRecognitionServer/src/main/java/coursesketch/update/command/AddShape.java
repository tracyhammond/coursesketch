package coursesketch.update.command;

import java.util.UUID;

import com.google.protobuf.ByteString;

import coursesketch.update.Command;
import protobuf.srl.sketch.Sketch.SrlSketch;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlInterpretation;

import protobuf.srl.commands.Commands.CommandType;

/**
 * This Command object creates an empty container at first to have recognition
 * be performed on, will return all interpretations in its bytestring.
 *
 * @author Matthew Dillard
 *
 */
public class AddShape extends Command {
    protected SrlShape data;

    public AddShape(SrlShape input){
        id = UUID.fromString(input.getId());
        type = CommandType.ADD_SHAPE;

        data = new SrlShape();
        data.setId(UUID.fromString(input.getId()));
        data.setName(input.getName());
        //FIXME set the time to match client load time
    }

    /*
    public AddShape(IRecognitionResult input){
        data = SrlShape.getDefaultInstance();

        input.sortNBestList();
        for(SrlShape s: input.getNBestList()){
            data.addInterpretations(s.getInterpretations());
        }
    }
    */

    @Override
    public ByteString toByteString() {
        SrlShape.Builder shapebuilder = SrlShape.newBuilder();

        shapebuilder.setId(data.getId().toString());
        shapebuilder.setTime(data.getTime());

        SrlInterpretation.Builder interpretationbuilder = SrlInterpretation.newBuilder();
        for (Interpretation i: data.getInterpretations()){
            interpretationbuilder.setLabel(i.label);
            interpretationbuilder.setConfidence(i.confidence);

            shapebuilder.addInterpretations(interpretationbuilder.build());
        }

        return shapebuilder.build().toByteString();
    }

    @Override
    public void execute(SrlSketch s) {
        // s.add(data);
    }
    @Override
    public void undo(SrlSketch s) {
        // s.remove(data);
    }
}
