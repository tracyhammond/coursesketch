package internalConnection;

import java.util.HashMap;

import coursesketch.server.interfaces.MultiConnectionState;
import protobuf.srl.submission.Submission.SrlExperiment;

public class AnswerConnectionState extends MultiConnectionState {
    private HashMap<String, SrlExperiment> pendingExperiments = new HashMap<String, SrlExperiment>();

    public AnswerConnectionState(final String key) {
        super(key);
    }

    public final void addPendingExperiment(final String key, final SrlExperiment exp) {
        pendingExperiments.put(key, exp);
    }

    public final SrlExperiment getExperiment(final String key) {
        return pendingExperiments.remove(key);
    }
}
