package database;

import protobuf.srl.submission.Submission.SrlSolution;

public class Database {
	SrlSolution sol;
	public void saveSolution(SrlSolution list /*, other data*/) {
		sol = list;
	}

	public SrlSolution getSolution(/*, other data*/) {
		return sol;
	}
}
