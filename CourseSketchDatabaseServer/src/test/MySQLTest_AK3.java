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

			Statement stmt = conn.createStatement() ;
			//String query = "select * from AssignmentProblemList;" ;
			String query = "select * from AssignmentProblemList where assignmentId=1;" ;
			/*String query = "select * from AssignmentProblemList where assignmentId=1;" ;
			ResultSet rs = stmt.executeQuery(query) ;
			List<Integer> problemIds = new ArrayList<Integer>();
			while ( rs.next() ) {
				System.out.println("increment");
//				int numColumns = rs.getMetaData().getColumnCount();
				int problemId = rs.getInt("problemId");				
				System.out.println(problemId);
				problemIds.add(problemId);
			}
			rs.close();
			for (int problemId: problemIds){
//				for ( int i = 1 ; i <= numColumns ; i++ ) {
					//System.out.println( "COLUMN " + i + " = " + rs.getObject(i) );
				String query2 = "select * from Problems where id=" + problemId ;
				ResultSet rs2 = stmt.executeQuery(query2) ;
				while ( rs2.next() ) {
					System.out.println("test");
					System.out.println(rs2.getString("questiontext") + "\n");
				}	
			}*/
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
