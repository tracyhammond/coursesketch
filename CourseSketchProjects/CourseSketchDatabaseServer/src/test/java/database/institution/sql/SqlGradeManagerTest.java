package database.institution.sql;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.dbunit.*;
import static org.junit.Assert.*;
import database.DatabaseAccessException;
import org.bson.types.ObjectId;
import protobuf.srl.school.School.SrlGrade;
import database.institution.sql.SqlStateManager;
import java.sql.*;

import   static org.easymock.EasyMock.createControl;
import   static org.easymock.EasyMock.expect;
import   static org.junit.Assert.assertEquals;

import org.easymock.IMocksControl;

public class SqlGradeManagerTest{

    @Test
    public void insertTest() throws SQLException {
        String userId = "test";
        String classification = "test";
        String itemId="test";

        final SrlGrade.Builder grade = SrlGrade.newBuilder();
        grade.setId("");
        grade.setProblemId("");
        grade.setComment("test");
        grade.setGrade(0);

        // Create Mock Objects
        IMocksControl control = createControl (); // create multiple Mock objects when by IMocksControl management

        Connection conn = control.createMock (Connection. class);
        Statement st = control.createMock (Statement. class);
        ResultSet rs = control.createMock (ResultSet. class);

        // Record set Mock Object expected behavior and output
        // Mock objects need to be performed must be recorded, such as pst.setInt (2 pas), rs.close ()
        expect(conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)).andReturn(st);

        expect(st.executeQuery("SELECT * FROM Grades WHERE UserID=\'" + userId + "\' AND SchoolItemType=\'" + classification + "\' AND SchoolItemID=\'" + itemId + "\';")).andReturn(rs);
        expect(rs.next()).andReturn(false);

        rs.moveToInsertRow();
        rs.updateString("UserID", userId);
        rs.updateString("SchoolItemType", classification);
        rs.updateString("SchoolItemID", itemId);
        rs.updateFloat("Grade", grade.getGrade());
        rs.updateString("Comments", grade.getComment());
        rs.insertRow();
        rs.moveToCurrentRow();

        st.close();
        rs.close();

        // Recording is completed, switch the replay state
        control.replay ();

        // The actual method is invoked
        String res="";
        try {
            res = SqlGradeManager.setGrade(conn, userId, classification, itemId, grade.build());
        }
        catch (DatabaseAccessException e) {

        }
        String expected = "INSERT";
        assertEquals (expected, res);


        // Verification
        control.verify ();
    }

    @Test
    public void setTest() throws SQLException {
        String userId = "test";
        String classification = "test";
        String itemId="test";

        final SrlGrade.Builder grade = SrlGrade.newBuilder();
        grade.setId("");
        grade.setProblemId("");
        grade.setComment("test");
        grade.setGrade(0);

        // Create Mock Objects
        IMocksControl control = createControl (); // create multiple Mock objects when by IMocksControl management

        Connection conn = control.createMock (Connection. class);
        Statement st = control.createMock (Statement. class);
        ResultSet rs = control.createMock (ResultSet. class);

        // Record set Mock Object expected behavior and output
        // Mock objects need to be performed must be recorded, such as pst.setInt (2 pas), rs.close ()
        expect(conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)).andReturn(st);

        expect(st.executeQuery("SELECT * FROM Grades WHERE UserID=\'" + userId + "\' AND SchoolItemType=\'" + classification + "\' AND SchoolItemID=\'" + itemId + "\';")).andReturn(rs);
        expect(rs.next()).andReturn(true);

        rs.updateFloat("Grade", grade.getGrade());
        rs.updateString("Comments", grade.getComment());
        rs.updateRow();

        st.close();
        rs.close();

        // Recording is completed, switch the replay state
        control.replay ();

        // The actual method is invoked
        String res="";
        try {
            res = SqlGradeManager.setGrade(conn, userId, classification, itemId, grade.build());
        }
        catch (DatabaseAccessException e) {

        }
        String expected = "SET";
        assertEquals (expected, res);

        // Verification
        control.verify ();
    }

    @Test
    public void getTest() throws SQLException {
        String userId = "test";
        String classification = "test";
        String itemId="test";

        final SrlGrade.Builder grade = SrlGrade.newBuilder();
        grade.setId("");
        grade.setProblemId("");
        grade.setComment("test");
        grade.setGrade(0);

        // Create Mock Objects
        IMocksControl control = createControl (); // create multiple Mock objects when by IMocksControl management

        Connection conn = control.createMock (Connection. class);
        Statement st = control.createMock (Statement. class);
        ResultSet rs = control.createMock (ResultSet. class);
        // Record set Mock Object expected behavior and output
        // Mock objects need to be performed must be recorded, such as pst.setInt (2 pas), rs.close ()
        expect(conn.createStatement()).andReturn(st);

        expect(st.executeQuery("SELECT * FROM Grades WHERE UserID=\'" + userId + "\' AND SchoolItemType=\'" + classification + "\' AND SchoolItemID=\'" + itemId + "\';")).andReturn(rs);

        expect(rs.getFloat("Grade")).andReturn(0.0f);
        expect(rs.getString("Comments")).andReturn("test");

        st.close();
        rs.close();

        // Recording is completed, switch the replay state
        control.replay();
        // The actual method is invoked
        SrlGrade res=null;
        try {
            res = SqlGradeManager.getGrade(conn, userId, classification, itemId);
        }
        catch (DatabaseAccessException e) {

        }
        SrlGrade expected = grade.build();
        assertEquals (expected, res);

        // Verification
        control.verify ();
    }

}