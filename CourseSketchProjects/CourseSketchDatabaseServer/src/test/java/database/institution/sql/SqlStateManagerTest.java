package database.institution.sql;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.dbunit.*;

import static org.easymock.EasyMock.captureBoolean;
import static org.junit.Assert.*;
import database.DatabaseAccessException;
import org.bson.types.ObjectId;
import protobuf.srl.school.School.State;
import database.institution.sql.SqlStateManager;
import java.sql.*;

import   static org.easymock.EasyMock.createControl;
import   static org.easymock.EasyMock.expect;
import   static org.junit.Assert.assertEquals;

import org.easymock.IMocksControl;

public class SqlStateManagerTest{

    @Test
    public void insertTest() throws SQLException {
        String userId = "test";
        String classification = "test";
        String itemId="test";

        final State.Builder state = State.newBuilder();
        state.setCompleted(true);
        state.setStarted(true);
        state.setGraded(true);

        // Create Mock Objects
        IMocksControl control = createControl (); // create multiple Mock objects when by IMocksControl management

        Connection conn = control.createMock (Connection. class);
        Statement st = control.createMock (Statement. class);
        ResultSet rs = control.createMock (ResultSet. class);

        // Record set Mock Object expected behavior and output
        // Mock objects need to be performed must be recorded, such as pst.setInt (2 pas), rs.close ()
        expect (conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)).andReturn(st);

        expect(st.executeQuery("SELECT * FROM State WHERE UserID=\'" + userId + "\' AND SchoolItemType=\'" + classification + "\' AND SchoolItemID=\'" + itemId + "\';")).andReturn(rs);
        expect(rs.next()).andReturn(false);

        rs.moveToInsertRow();
        rs.updateString("UserID", userId);
        rs.updateString("SchoolItemType", classification);
        rs.updateString("SchoolItemID", itemId);
        rs.updateBoolean("Completed", state.getCompleted());
        rs.updateBoolean("Started", state.getStarted());
        rs.updateBoolean("Graded", state.getGraded());
        rs.insertRow();
        rs.moveToCurrentRow();

        st.close();
        rs.close();

        // Recording is completed, switch the replay state
        control.replay ();

        // The actual method is invoked
        String res="";
        try {
            res = SqlStateManager.setState(conn, userId, classification, itemId, state.build());
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

        final State.Builder state = State.newBuilder();
        state.setCompleted(true);
        state.setStarted(true);
        state.setGraded(true);

        // Create Mock Objects
        IMocksControl control = createControl (); // create multiple Mock objects when by IMocksControl management

        Connection conn = control.createMock (Connection. class);
        Statement st = control.createMock (Statement. class);
        ResultSet rs = control.createMock (ResultSet. class);

        // Record set Mock Object expected behavior and output
        // Mock objects need to be performed must be recorded, such as pst.setInt (2 pas), rs.close ()
        expect (conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)).andReturn(st);

        expect(st.executeQuery("SELECT * FROM State WHERE UserID=\'" + userId + "\' AND SchoolItemType=\'" + classification + "\' AND SchoolItemID=\'" + itemId + "\';")).andReturn(rs);
        expect(rs.next()).andReturn(true);

        rs.updateBoolean("Completed", state.getCompleted());
        rs.updateBoolean("Started", state.getStarted());
        rs.updateBoolean("Graded", state.getGraded());
        rs.updateRow();

        st.close();
        rs.close();

        // Recording is completed, switch the replay state
        control.replay ();

        // The actual method is invoked
        String res="";
        try {
            res = SqlStateManager.setState(conn, userId, classification, itemId, state.build());
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

        final State.Builder state = State.newBuilder();
        state.setCompleted(true);
        state.setStarted(true);
        state.setGraded(true);

        // Create Mock Objects
        IMocksControl control = createControl (); // create multiple Mock objects when by IMocksControl management

        Connection conn = control.createMock (Connection. class);
        Statement st = control.createMock (Statement. class);
        ResultSet rs = control.createMock (ResultSet. class);

        // Record set Mock Object expected behavior and output
        // Mock objects need to be performed must be recorded, such as pst.setInt (2 pas), rs.close ()
        expect (conn.createStatement()).andReturn(st);

        expect(st.executeQuery("SELECT * FROM State WHERE UserID=\'" + userId + "\' AND SchoolItemType=\'" + classification + "\' AND SchoolItemID=\'" + itemId + "\';")).andReturn(rs);

        expect(rs.getBoolean("Completed")).andReturn(true);
        expect(rs.getBoolean("Started")).andReturn(true);
        expect(rs.getBoolean("Graded")).andReturn(true);

        st.close();
        rs.close();

        // Recording is completed, switch the replay state
        control.replay ();

        // The actual method is invoked
        State res=null;
        try {
            res = SqlStateManager.getState(conn, userId, classification, itemId);
        }
        catch (DatabaseAccessException e) {

        }
        State expected = state.build();
        assertEquals (expected, res);

        // Verification
        control.verify ();
    }

}