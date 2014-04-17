package test;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
//import com.mysql.jdbc.*;
//import com.mysql.jdbc.Connection;

public class SQLGrades {
//	private Date timeSubmitted = null;
//	private String comment = "";
//	private float grade = 0;
	private Connection connG;
	private Statement gradeStmt;
	
	public SQLGrades() {
		try {
			connG = DriverManager.getConnection("jdbc:mysql://srl03.tamu.edu/grades?" +
			                    "user=srl&password=sketchrec");
			gradeStmt = connG.createStatement();
			// course -> assignments -> problems
		} catch(Exception e) { e.printStackTrace(); }
	}
	
	public java.sql.Date getDate(String experimentId) {
		try {
			String gradeQ = "select * from problem_grades where problem_id='" + experimentId + "'";
			ResultSet results = gradeStmt.executeQuery(gradeQ);
			results.next();
			return results.getDate("date_graded");
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public float getGrade(String experimentId) {
		try {
			String gradeQ = "select * from problem_grades where problem_id='" + experimentId + "'";
			ResultSet results = gradeStmt.executeQuery(gradeQ);
			results.next();
			return results.getFloat("grade");
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public String getComment(String experimentId) {
		try {
			String gradeQ = "select * from problem_grades where problem_id='" + experimentId + "'";
			ResultSet results = gradeStmt.executeQuery(gradeQ);
			results.next();
			return results.getString("comment");
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setComment(String experimentId, String comment) {
		try {
			String gradeQ = "UPDATE problem_grades SET comment='" + comment + "' where problem_id='" + experimentId + "'";
			gradeStmt.execute(gradeQ);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setGrade(String experimentId, float grade) {
		try {
			String gradeQ = "UPDATE problem_grades SET grade=" + grade + " where problem_id='" + experimentId + "'";
			gradeStmt.execute(gradeQ);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setDate(String experimentId, java.sql.Date date) {
		try {
			String gradeQ = "UPDATE problem_grades SET date_graded=" + date + " where problem_id='" + experimentId + "'";
			gradeStmt.execute(gradeQ);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SQLGrades g = new SQLGrades();
		System.out.println(g.getComment("5348558ae4b062975ab9d8d2"));
		System.out.println(g.getDate("5348558ae4b062975ab9d8d2"));
		g.setComment("5348558ae4b062975ab9d8d2", "Scooby-Dooby-Doo!!!!! Where Are You!!!");
		g.setGrade("5348558ae4b062975ab9d8d2", (float)5.343);
		g.setDate("5348558ae4b062975ab9d8d2", new java.sql.Date(1234567890));
	}
}