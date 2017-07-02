package coursesketch.update.command;

import com.google.protobuf.ByteString;
import coursesketch.update.Command;
import protobuf.srl.commands.Commands.CommandType;
import protobuf.srl.sketch.Sketch.SrlInterpretation;
import protobuf.srl.sketch.Sketch.SrlShape;
import protobuf.srl.sketch.Sketch.SrlSketch;

import java.util.UUID;

/**
 * This Command object creates an empty container at first to have recognition
 * be performed on, will return all interpretations in its bytestring.
 *
 * @author Matthew Dillard
 *
 */
public class AddShape extends Command {
    protected SrlShape data;

    public AddShape(SrlShape input) {
        id = UUID.fromString(input.getId());
        type = CommandType.ADD_SHAPE;

        data = input;
        /*
        data.setId(UUID.fromString(input.getId()));
        data.setName(input.getName());
        */
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

        shapebuilder.setId(data.getId());
        shapebuilder.setTime(data.getTime());

        SrlInterpretation.Builder interpretationbuilder = SrlInterpretation.newBuilder();
        for (SrlInterpretation i : data.getInterpretationsList()) {
            interpretationbuilder.setLabel(i.getLabel());
            interpretationbuilder.setConfidence(i.getConfidence());

            shapebuilder.addInterpretations(interpretationbuilder.build());
        }

        return shapebuilder.build().toByteString();
    }

    @Override
    public void execute(SrlSketch sketch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undo(SrlSketch sketch) {
        throw new UnsupportedOperationException();
    }
}
