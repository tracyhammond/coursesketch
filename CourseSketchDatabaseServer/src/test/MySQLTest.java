/**
 * 
 */
package test;

import java.sql.*;
//import com.mysql.jdbc.*;
//import com.mysql.jdbc.Connection;


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
		//	Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Connection conn =
				       DriverManager.getConnection("jdbc:mysql://srl03.tamu.edu/problembank?" +
				                                   "user=srl&password=sketchrec");
			Statement stmt = conn.createStatement() ;
			String query = "select questiontext from Problems ;" ;
			ResultSet rs = stmt.executeQuery(query) ;
			while ( rs.next() ) {
				int numColumns = rs.getMetaData().getColumnCount();
				for ( int i = 1 ; i <= numColumns ; i++ ) {
					System.out.println( "COLUMN " + i + " = " + rs.getObject(i) );
					System.out.println(rs.getString("questiontext") + " ");
				}	
			}
						System.out.println(rs.first());
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("try");

		try {
		    String url ="jdbc:mysql://srl03.tamu.edu:3306/myfirstdb";
		    //Connection con = DriverManager.getConnection(url, "root", "1234");
		    System.out.println("connection Established");
		    }
		    catch(Exception e) {
		                System.out.println("Couldnt get connection");
		    }
		
	}

}
