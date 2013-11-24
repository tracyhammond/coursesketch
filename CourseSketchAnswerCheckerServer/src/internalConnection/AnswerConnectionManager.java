package internalConnection;

import multiConnection.MultiConnectionManager;
import multiConnection.MultiInternalConnectionServer;

public class AnswerConnectionManager extends MultiConnectionManager {

	public AnswerConnectionManager(MultiInternalConnectionServer parent) {
		super(parent);
	}
}
