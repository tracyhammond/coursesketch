package database.institution.mongo;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import protobuf.srl.grading.Grading.ProtoGrade;
import protobuf.srl.grading.Grading.GradeHistory;
import protobuf.srl.utils.Util;
import protobuf.srl.school.School.SrlCourse;

/**
 * Tests for GradeManager.
 *
 * Created by matt on 4/11/15.
 */
public class GradeManagerTest {
    @Rule
    public FongoRule fongo = new FongoRule();

    public DB db;
    public Authenticator fauth;
    public Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
    public SrlCourse.Builder courseBuilder = SrlCourse.newBuilder();

    public ProtoGrade.Builder fakeProtoGrade = ProtoGrade.newBuilder();
}
