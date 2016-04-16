package coursesketch.recognition;

import coursesketch.recognition.defaults.DefaultRecognition;
import coursesketch.recognition.framework.TemplateDatabaseInterface;
import coursesketch.recognition.framework.exceptions.RecognitionException;
import coursesketch.recognition.pdollar.Point;
import protobuf.srl.commands.Commands;
import protobuf.srl.sketch.Sketch;

import java.util.List;

/**
 * Created by gigemjt on 4/16/16.
 */
public class BasicRecognition extends DefaultRecognition {

    public BasicRecognition(final TemplateDatabaseInterface templateDatabase) {
        super(templateDatabase);
    }

    @Override public Commands.SrlUpdateList addUpdate(final String s, final Commands.SrlUpdate srlUpdate) throws RecognitionException {
        return Commands.SrlUpdateList.getDefaultInstance();
    }

    @Override public Commands.SrlUpdateList setUpdateList(final String s, final Commands.SrlUpdateList srlUpdateList) throws RecognitionException {
        return Commands.SrlUpdateList.getDefaultInstance();
    }

    @Override public Sketch.SrlSketch setSketch(final String s, final Sketch.SrlSketch srlSketch) throws RecognitionException {
        return null;
    }

    @Override public Commands.SrlUpdateList recognize(final String s, final Commands.SrlUpdateList srlUpdateList) throws RecognitionException {
        List<Point> pointCloud = convert(srlUpdateList);
        return Commands.SrlUpdateList.getDefaultInstance();
    }

    @Override public Sketch.SrlSketch recognize(final String s, final Sketch.SrlSketch srlSketch) throws RecognitionException {
        return null;
    }

    private List<Point> convert(Commands.SrlUpdateList srlUpdateList) {
        return null;
    }
}
