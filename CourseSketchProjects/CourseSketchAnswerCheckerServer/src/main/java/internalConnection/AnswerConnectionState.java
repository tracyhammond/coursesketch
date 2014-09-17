package internalConnection;

import java.util.HashMap;

import multiconnection.MultiConnectionState;
import protobuf.srl.submission.Submission.SrlExperiment;

public class AnswerConnectionState extends MultiConnectionState {
	private HashMap<String,SrlExperiment> pendingExperiments = new HashMap<String,SrlExperiment>();

	public AnswerConnectionState(String key) {
		super(key);
	}

	public void addPendingExperiment(String key, SrlExperiment exp) {
		pendingExperiments.put(key, exp);
	}

	public SrlExperiment getExperiment(String key) {
		return pendingExperiments.remove(key);
	}
}
