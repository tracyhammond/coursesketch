package database.institution;

import static database.DatabaseStringConstants.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.DateTime;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;
import protobuf.srl.school.School.State;
import protobuf.srl.school.School.SrlPermission.Builder;
import protobuf.srl.school.School.SrlProblem;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.DatabaseAccessException;
import database.RequestConverter;
import database.UserUpdateHandler;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.Authenticator.AuthType;

public class CourseManager {
    static String mongoInsertCourse(final DB dbs, final SrlCourse course) {
        final DBCollection new_user = dbs.getCollection(COURSE_COLLECTION);
        final BasicDBObject query = new BasicDBObject(DESCRIPTION, course.getDescription()).append(NAME, course.getName())
                .append(COURSE_ACCESS, course.getAccess().getNumber()).append(COURSE_SEMESTER, course.getSemester())
                .append(ACCESS_DATE, course.getAccessDate().getMillisecond()).append(CLOSE_DATE, course.getCloseDate().getMillisecond())
                .append(IMAGE, course.getImageUrl()).append(ADMIN, course.getAccessPermission().getAdminPermissionList())
                .append(MOD, course.getAccessPermission().getModeratorPermissionList())
                .append(USERS, course.getAccessPermission().getUserPermissionList());
        if (course.getAssignmentListList() != null) {
            query.append(ASSIGNMENT_LIST, course.getAssignmentListList());
        }
        new_user.insert(query);
        final DBObject corsor = new_user.findOne(query);
        return corsor.get(SELF_ID).toString();
    }

    static SrlCourse mongoGetCourse(final DB dbs, final String courseId, final String userId, final long checkTime)
            throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
        final DBObject corsor = myDbRef.fetch();
        if (corsor == null) {
            throw new DatabaseAccessException("Course was not found with the following ID " + courseId);
        }

        final ArrayList adminList = (ArrayList<Object>) corsor.get(ADMIN); // convert
                                                                     // to
                                                                     // ArrayList<String>
        final ArrayList modList = (ArrayList<Object>) corsor.get(MOD); // convert to
                                                                 // ArrayList<String>
        final ArrayList usersList = (ArrayList<Object>) corsor.get(USERS); // convert
                                                                     // to
                                                                     // ArrayList<String>
        boolean isAdmin, isMod, isUsers;
        isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
        isMod = Authenticator.checkAuthentication(dbs, userId, modList);
        isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);

        if (!isAdmin && !isMod && !isUsers) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final SrlCourse.Builder exactCourse = SrlCourse.newBuilder();
        exactCourse.setDescription((String) corsor.get(DESCRIPTION));
        exactCourse.setName((String) corsor.get(NAME));
        if (corsor.get(COURSE_SEMESTER) != null) {
            exactCourse.setSemester((String) corsor.get(COURSE_SEMESTER));
        }

        // TODO: delete this!
        boolean ignoreDates = false;
        try {
            exactCourse.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(ACCESS_DATE)).longValue()));
            exactCourse.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) corsor.get(CLOSE_DATE)).longValue()));
        } catch (Exception e) {
            ignoreDates = true;
            e.printStackTrace();
        }
        exactCourse.setId(courseId);

        // states
        final State.Builder stateBuilder = State.newBuilder();
        if (!ignoreDates && exactCourse.getCloseDate().getMillisecond() > checkTime) {
            stateBuilder.setPastDue(true);
        }

        // TODO: add this to all fields!
        // A course is only publishable after a certain criteria is met
        if (corsor.containsField(STATE_PUBLISHED)) {
            try {
                final boolean published = (Boolean) corsor.get(STATE_PUBLISHED);
                if (published) {
                    stateBuilder.setPublished(true);
                } else {
                    if (!isAdmin || !isMod) {
                        throw new DatabaseAccessException("The specific course is not published yet", true);
                    } else {
                        stateBuilder.setPublished(false);
                    }
                }
            } catch (Exception e) {

            }
        }

        if (corsor.get(IMAGE) != null) {
            exactCourse.setImageUrl((String) corsor.get(IMAGE));
        }

        // if you are a user, the course must be open to view the assignments
        if (isAdmin || isMod
                || (isUsers && (ignoreDates || Authenticator.isTimeValid(checkTime, exactCourse.getAccessDate(), exactCourse.getCloseDate())))) {
            if (corsor.get(ASSIGNMENT_LIST) != null) {
                exactCourse.addAllAssignmentList((List) corsor.get(ASSIGNMENT_LIST));
            }
            stateBuilder.setAccessible(true);
        } else if ((isUsers && !Authenticator.isTimeValid(checkTime, exactCourse.getAccessDate(), exactCourse.getCloseDate()))) {
            System.err.println("USER CLASS TIME IS CLOSED SO THE COURSE LIST HAS BEEN PREVENTED FROM BEING USED!");
            System.err
                    .println(exactCourse.getAccessDate().getMillisecond() + " < " + checkTime + " < " + exactCourse.getCloseDate().getMillisecond());
            stateBuilder.setAccessible(false);
        }

        exactCourse.setState(stateBuilder);

        if (isAdmin) {
            try {
                exactCourse.setAccess(SrlCourse.Accessibility.valueOf((Integer) corsor.get(COURSE_ACCESS))); // admin
            } catch (Exception e) {

            }
            final SrlPermission.Builder permissions = SrlPermission.newBuilder();
            permissions.addAllAdminPermission((ArrayList) corsor.get(ADMIN)); // admin
            permissions.addAllModeratorPermission((ArrayList) corsor.get(MOD)); // admin
            permissions.addAllUserPermission((ArrayList) corsor.get(USERS)); // admin
            exactCourse.setAccessPermission(permissions.build());
        }
        return exactCourse.build();

    }

    public static boolean mongoUpdateCourse(final DB dbs, final String courseId, final String userId, final SrlCourse course)
            throws AuthenticationException, DatabaseAccessException {
        boolean update = false;
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
        final DBObject corsor = myDbRef.fetch();
        DBObject updateObj = null;
        final DBCollection courses = dbs.getCollection(COURSE_COLLECTION);

        final ArrayList adminList = (ArrayList<Object>) corsor.get(ADMIN);
        final ArrayList modList = (ArrayList<Object>) corsor.get(MOD);
        boolean isAdmin, isMod;
        isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
        isMod = Authenticator.checkAuthentication(dbs, userId, modList);

        if (!isAdmin && !isMod) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final BasicDBObject updated = new BasicDBObject();
        if (isAdmin) {
            if (course.hasSemester()) {
                updateObj = new BasicDBObject(COURSE_SEMESTER, course.getSemester());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (course.hasAccessDate()) {

                updateObj = new BasicDBObject(ACCESS_DATE, course.getAccessDate().getMillisecond());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (course.hasCloseDate()) {
                updateObj = new BasicDBObject(CLOSE_DATE, course.getCloseDate().getMillisecond());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }

            if (course.hasImageUrl()) {
                updateObj = new BasicDBObject(IMAGE, course.getImageUrl());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (course.hasDescription()) {
                updateObj = new BasicDBObject(DESCRIPTION, course.getDescription());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (course.hasName()) {
                updateObj = new BasicDBObject(NAME, course.getName());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            if (course.hasAccess()) {
                updateObj = new BasicDBObject(COURSE_ACCESS, course.getAccess().getNumber());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
            // Optimization: have something to do with pulling values of an
            // array and pushing values to an array
            if (course.hasAccessPermission()) {
                System.out.println("Updating permissions!");
                final SrlPermission permissions = course.getAccessPermission();
                if (permissions.getAdminPermissionList() != null) {
                    updateObj = new BasicDBObject(ADMIN, permissions.getAdminPermissionList());
                    courses.update(corsor, new BasicDBObject("$set", updateObj));
                }
                if (permissions.getModeratorPermissionList() != null) {
                    updateObj = new BasicDBObject(MOD, permissions.getModeratorPermissionList());
                    courses.update(corsor, new BasicDBObject("$set", updateObj));
                }
                if (permissions.getUserPermissionList() != null) {
                    updateObj = new BasicDBObject(USERS, permissions.getUserPermissionList());
                    courses.update(corsor, new BasicDBObject("$set", updateObj));
                }
            }
        }
        if (isAdmin || isMod) {
            if (course.getAssignmentListList() != null) {
                updateObj = new BasicDBObject(ASSIGNMENT_LIST, course.getAssignmentListList());
                courses.update(corsor, new BasicDBObject("$set", updateObj));
                update = true;
            }
        }
        // courses.update(corsor, new BasicDBObject ("$set",updateObj));

        // get user list
        // send updates
        if (update) {
            UserUpdateHandler.InsertUpdates(dbs, ((List) corsor.get(USERS)), courseId, UserUpdateHandler.COURSE_CLASSIFICATION);
        }
        return true;

    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * With that being said this allows a course to be updated adding the
     * assignmentId to its list of items.
     */
    static boolean mongoInsertIntoCourse(final DB dbs, final String courseId, final String assignmentId) {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
        final DBObject corsor = myDbRef.fetch();
        DBObject updateObj = null;
        final DBCollection courses = dbs.getCollection(COURSE_COLLECTION);
        updateObj = new BasicDBObject(ASSIGNMENT_LIST, assignmentId);
        courses.update(corsor, new BasicDBObject("$addToSet", updateObj));

        UserUpdateHandler.InsertUpdates(dbs, ((List) corsor.get(USERS)), courseId, UserUpdateHandler.COURSE_CLASSIFICATION);
        return true;

    }

    public static ArrayList<SrlCourse> mongoGetAllPublicCourses(final DB dbs) {
        final DBCollection courseTable = dbs.getCollection(COURSE_COLLECTION);

        final ArrayList<SrlCourse> resultList = new ArrayList<SrlCourse>();

        // checks for all public courses.
        final DBObject publicCheck = new BasicDBObject(COURSE_ACCESS, SrlCourse.Accessibility.PUBLIC.getNumber()); // the
                                                                                                             // value
                                                                                                             // for
                                                                                                             // a
                                                                                                             // public
                                                                                                             // course
        DBCursor cursor = courseTable.find(publicCheck);
        while (cursor.hasNext()) {
            final SrlCourse.Builder build = SrlCourse.newBuilder();
            final DBObject foundCourse = cursor.next();
            build.setId(foundCourse.get(SELF_ID).toString());
            build.setDescription(foundCourse.get(DESCRIPTION).toString());
            build.setName(foundCourse.get(NAME).toString());
            build.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(ACCESS_DATE)).longValue()));
            build.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(CLOSE_DATE)).longValue()));
            resultList.add(build.build());
        }

        // checks for all super public courses.
        final DBObject superPublicCheck = new BasicDBObject(COURSE_ACCESS, SrlCourse.Accessibility.SUPER_PUBLIC.getNumber()); // the
                                                                                                                        // value
                                                                                                                        // for
                                                                                                                        // a
                                                                                                                        // superpublic
                                                                                                                        // course
        cursor = courseTable.find(superPublicCheck);
        while (cursor.hasNext()) {
            final SrlCourse.Builder build = SrlCourse.newBuilder();
            final DBObject foundCourse = cursor.next();
            build.setId(foundCourse.get(SELF_ID).toString());
            build.setDescription(foundCourse.get(DESCRIPTION).toString());
            build.setName(foundCourse.get(NAME).toString());
            build.setAccessDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(ACCESS_DATE)).longValue()));
            build.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number) foundCourse.get(CLOSE_DATE)).longValue()));
            resultList.add(build.build());
        }
        try {
            System.out.println("SQL attempt");
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn;
            conn = DriverManager.getConnection("jdbc:mysql://srl03.tamu.edu/problembank?" + "user=srl&password=sketchrec");
            final Statement stmt = conn.createStatement();
            System.out.println("SQL connected");
            final String query = "select * from CourseInfo where id=2";
            final ResultSet rs = stmt.executeQuery(query);
            System.out.println("SQL selected");
            final SrlCourse.Builder build = SrlCourse.newBuilder();
            build.setId("3");
            build.setDescription(rs.getString("Description"));
            build.setName(rs.getString("Name"));
            System.out.println(rs.getString("Name"));
            // build.setAccessDate(new DateTime());
            System.out.println("SQL worked");
            // RequestConverter.getProtoFromMilliseconds(((Number)
            // foundCourse.get(ACCESS_DATE)).longValue()));
            // build.setCloseDate(RequestConverter.getProtoFromMilliseconds(((Number)
            // foundCourse.get(CLOSE_DATE)).longValue()));
            // ADD THIS! resultList.add(build.build());
        } catch (Exception e) {
        }
        return resultList;
    }

    /**
     * NOTE: This is meant for internal use do not make this method public.
     *
     * With that being said this allows the default ids to be inserted
     */
    static void mongoInsertDefaultGroupId(final DB dbs, final String courseId, final String userGroupId, final String modGroupId, final String adminGroupId) {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
        final DBObject corsor = myDbRef.fetch();
        final DBCollection courses = dbs.getCollection(COURSE_COLLECTION);
        final BasicDBObject listQueries = new BasicDBObject(ADMIN_GROUP_ID, adminGroupId).append(MOD_GROUP_ID, modGroupId).append(USER_GROUP_ID,
                userGroupId);
        final DBObject courseQuery = new BasicDBObject("$set", listQueries);
        courses.update(corsor, courseQuery);
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * Returns a list of Id for the default group for an assignment.
     *
     * The list are ordered as so: AdminGroup, ModGroup, UserGroup
     */
    static ArrayList<String>[] mongoGetDefaultGroupList(final DB dbs, final String courseId) {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
        final DBObject corsor = myDbRef.fetch();
        final ArrayList<String>[] returnValue = new ArrayList[3];
        returnValue[0] = (ArrayList) corsor.get(ADMIN);
        returnValue[1] = (ArrayList) corsor.get(MOD);
        returnValue[2] = (ArrayList) corsor.get(USERS);
        return returnValue;
    }

    /**
     * NOTE: This is meant for internal use do not make this method public
     *
     * Returns a list of Id for the default group for an assignment.
     *
     * The Ids are ordered as so: AdminGroup, ModGroup, UserGroup
     */
    static String[] mongoGetDefaultGroupId(final DB dbs, final String courseId) {
        final DBRef myDbRef = new DBRef(dbs, COURSE_COLLECTION, new ObjectId(courseId));
        final DBObject corsor = myDbRef.fetch();
        final String[] returnValue = new String[3];
        returnValue[0] = corsor.get(ADMIN_GROUP_ID).toString();
        returnValue[1] = corsor.get(MOD_GROUP_ID).toString();
        returnValue[2] = corsor.get(USER_GROUP_ID).toString();
        return returnValue;
    }
}
