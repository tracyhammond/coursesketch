package connection;

import coursesketch.server.interfaces.MultiConnectionState;
import protobuf.srl.submission.Submission.SrlExperiment;

import java.util.HashMap;

/**
 * An object that contains the state of connection with another server.
 *
 * @author gigemjt
 *
 */
public class AnswerConnectionState extends MultiConnectionState {
    /**
     * Used because experiments are not actually stored for right now so we do not want to overload this server.
     */
    private static final long TEMP_MAX_SIZE = 1000;

    /**
     * Experiments that are waiting to be checked by the answer checker.
     */
    private HashMap<String, SrlExperiment> pendingExperiments = new HashMap<String, SrlExperiment>();

    /**
     * Creates a {@link coursesketch.server.interfaces.MultiConnectionState} with the given Key.
     *
     * @param key
     *            Uniquely Identifies this connection from any other connection.
     */
    public AnswerConnectionState(final String key) {
        super(key);
    }

    /**
     * Adds a new experiment that is waiting to be checked from the given proxy server.
     * @param key The session of this unique experiment.
     * @param exp The {@link protobuf.srl.submission.Submission.SrlExperiment} that is waiting to be checked.
     */
    public final void addPendingExperiment(final String key, final SrlExperiment exp) {
        pendingExperiments.put(key, exp);
        if (pendingExperiments.size() > TEMP_MAX_SIZE) {
            pendingExperiments.clear();
        }
    }

    /**
     * Gets an experiment with the given key so that it can be checked.
     * @param key The key that identifies this specific experiment.
     * @return {@link protobuf.srl.submission.Submission.SrlExperiment}.
     */
    public final SrlExperiment getExperiment(final String key) {
        return pendingExperiments.remove(key);
    }
}
