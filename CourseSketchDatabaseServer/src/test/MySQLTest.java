/**
 * 
 */
package test;

import java.sql.*;
import java.util.ArrayList;
//import com.mysql.jdbc.*;
//import com.mysql.jdbc.Connection;
import java.util.List;


/**
 * @author hammond
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 */
public class MySQLTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			System.out.println("try");
			Connection conn;	
				conn = DriverManager.getConnection("jdbc:mysql://srl03.tamu.edu/problembank?" +
				                               "user=srl&password=sketchrec");

			Statement stmt = conn.createStatement();
			// course -> assignments -> problems
			String query2 = "select * from CourseInfo";
			ResultSet courses = stmt.executeQuery(query2);

			List<Integer> courseIds = new ArrayList<Integer>();
			List<String> names = new ArrayList<String>();
			while (courses.next()) {
//				int numColumns = rs.getMetaData().getColumnCount();
				courseIds.add(courses.getInt("id"));
				names.add(courses.getString("ShortTitle"));
			}
			courses.close();

			for (int i = 0; i < courseIds.size(); ++i) {
				System.out.println("The course called \"" 
						+ names.get(i) 
						+ "\" has the following assignments:");

				String assignmentQuery = "select * from CourseInfo where id="
						+ courseIds.get(i);

				ResultSet assignments = stmt.executeQuery(assignmentQuery);
				List<Integer> assignmentIds = new ArrayList<Integer>();
				while (assignments.next()) {
					int assignmentId = assignments.getInt("id");
					assignmentIds.add(assignmentId);
				}
				assignments.close();

				for (int as : assignmentIds) {
					System.out.println("Assignment " + as + ":");
					String problemQuery = "select * from AssignmentProblemList where assignmentId="
							+ as;
					ResultSet problems = stmt.executeQuery(problemQuery);
					List<Integer> problemIds = new ArrayList<Integer>();
					while (problems.next()) {
						int problemId = problems.getInt("problemId");
						problemIds.add(problemId);
					}
					problems.close();

					for (int ps : problemIds) {
						System.out.println("Problem " + ps + ":");
						String statementQuery = "select * from Problems where id="+ps;
						ResultSet statements = stmt.executeQuery(statementQuery);
						while (statements.next()) {
							System.out.println(statements.getString("questiontext"));
						}
					}
				}
			}
		System.out.println("done");
		
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
		e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}

	}
}
