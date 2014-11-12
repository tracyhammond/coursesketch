

import java.lang.Exception;
import java.sql.*;
import java.util.ArrayList;
//import com.mysql.jdbc.*;
//import com.mysql.jdbc.Connection;
import java.util.List;
import java.util.Scanner;

import protobuf.srl.school.School.SrlUser;
import database.DatabaseAccessException;
import database.institution.Institution;
import database.user.UserClient;



/**
 * @author turner
 * @copyright Michael Turenr, Sketch Recognition Lab, Texas A&M University
 */
public class UserTranseferTester {

    /**
     * @param args
     */
    public static void main(String[] args) {

        Connection conn = null;
        Statement stmt = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://srl03.tamu.edu/SketchRec?" +
                    "user=srl&password=sketchrec");




        }
        catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    conn.close();
            }catch(SQLException se){
            }// do nothing
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");

    }
}