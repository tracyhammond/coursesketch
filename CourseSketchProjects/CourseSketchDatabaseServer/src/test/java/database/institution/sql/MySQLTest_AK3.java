/**
 * 
 
srl03.tamu.edu/phpmyadmin/
user: srl
password: sketchrec
 
 */
package database.institution.sql;

import protobuf.srl.school.School;

import java.sql.*;


/**
 * @author hammond
 * @copyright Tracy Hammond, Sketch Recognition Lab, Texas A&M University
 */
public class MySQLTest_AK3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Scanner user_input = new Scanner( System.in );
//		String assnID;
//		System.out.println("Which assignment would you like to pull up? ");
//		assnID = user_input.next();

        String userId = "test";
        String classification = "test";
        String itemId = "test";
        School.State.Builder state = School.State.newBuilder();
        state.setStarted(false);
        state.setCompleted(false);
        state.setGraded(false);
		/*System.out.println("Which assignment would you like to pull up? ");
		assignment_variable = user_input.next();*/
		
		// TODO Auto-generated method stub
			try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                System.out.println("trying...");
                Connection conn;
                conn = DriverManager.getConnection("jdbc:mysql://srl03.tamu.edu/SketchRec?" +
                        "user=srl&password=sketchrec");
                System.out.println("conected...");
                try {
                    Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    ResultSet rs = stmt.executeQuery("SELECT * FROM State WHERE UserID=\"" + userId + "\" AND SchoolItemType=\"" + classification + "\" AND SchoolItemID=\"" + itemId + "\";");
                    System.out.println("queried...");
                    if(rs.next()) {
                        System.out.println("editing...");
                        rs.updateBoolean("Completed", state.getCompleted());
                        rs.updateBoolean("Started", state.getStarted());
                        rs.updateBoolean("Graded", state.getGraded());
                        rs.updateRow();
                        System.out.println("edited...");
                    }
                    else {
                        System.out.println("inserting...");
                        rs.moveToInsertRow();
                        rs.updateString("UserID", userId);
                        rs.updateString("SchoolItemType", classification);
                        rs.updateString("SchoolItemID", itemId);
                        rs.updateBoolean("Completed", state.getCompleted());
                        rs.updateBoolean("Started", state.getStarted());
                        rs.updateBoolean("Graded", state.getGraded());
                        rs.insertRow();
                        rs.moveToCurrentRow();
                        System.out.println("inserted...");
                    }
                }catch(SQLException e) {
                    System.out.println("error... ");
                    e.printStackTrace();
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
