/**
 * 
 
srl03.tamu.edu/phpmyadmin/
user: srl
password: sketchrec
 
 */
package test;

import java.sql.*;
import java.util.ArrayList;
//import com.mysql.jdbc.*;
//import com.mysql.jdbc.Connection;
import java.util.List;
import java.util.Scanner;


/**
 * @author hammond
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 */
public class MySQLTest_AK3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner user_input = new Scanner( System.in );
		String assnID;
		System.out.println("Which assignment would you like to pull up? ");
		assnID = user_input.next();
		
		/*System.out.println("Which assignment would you like to pull up? ");
		assignment_variable = user_input.next();*/
		
		// TODO Auto-generated method stub
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				System.out.println("trying...");
				Connection conn;	
				conn = DriverManager.getConnection("jdbc:mysql://srl03.tamu.edu/problembank?" +
				                               "user=srl&password=sketchrec");

			Statement stmt = conn.createStatement() ;
			//String query = "select * from AssignmentProblemList;" ;
			/*System.out.println("Which assignment would you like to pull up? ");
			assignment_variable = user_input.next();*/
			String query = "select * from AssignmentProblemList where assignmentId = " + assnID + ";" ;
			ResultSet rs = stmt.executeQuery(query) ;
			List<Integer> problemIds = new ArrayList<Integer>();
			while ( rs.next() ) {
				//System.out.println("increment");
				//System.out.println("\n");
//				int numColumns = rs.getMetaData().getColumnCount();
				int problemId = rs.getInt("problemId");				
				//System.out.println(problemId);
				problemIds.add(problemId);
			}
			rs.close();
			for (int problemId: problemIds){
//				for ( int i = 1 ; i <= numColumns ; i++ ) {
					//System.out.println( "COLUMN " + i + " = " + rs.getObject(i) );
				String query2 = "select * from Problems where id=" + problemId ;
				ResultSet rs2 = stmt.executeQuery(query2) ;
				while ( rs2.next() ) {
					System.out.println("\ntest");
					System.out.println(rs2.getString("questiontext") + "\n");
				}	
			}
		//	System.out.println(rs.first());			
			//	rs2.close();
			
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
