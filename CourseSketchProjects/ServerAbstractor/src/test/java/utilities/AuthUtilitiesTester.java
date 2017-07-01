package utilities;

import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import org.junit.Assert;
import org.junit.Test;
import protobuf.srl.services.authentication.Authentication;

/**
 * Tests for the auth utilities
 * Created by dtracers on 9/16/2015.
 */
public class AuthUtilitiesTester {

    @Test
    public void checkShifter() {
        long one = AuthUtilities.convertAndShift(true, 0);
        Assert.assertEquals(1, one);
        long ten = AuthUtilities.convertAndShift(true, 1);
        Assert.assertEquals(0b10, ten);
        long zero = AuthUtilities.convertAndShift(false, 0);
        Assert.assertEquals(0, zero);
        long zero10 = AuthUtilities.convertAndShift(false, 10);
        Assert.assertEquals(0, zero10);
    }
    @Test
    public void checkAccessOnlyReturnsCheckAccess() {
        Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .build();
        Authentication.AuthType fixedAuthType = AuthUtilities.fixCheckType(authType);
        new ProtobufComparisonBuilder().build().equals(authType, fixedAuthType);
    }

    @Test
    public void checkUserOnlyReturnsCheckAccessAndStudent() {
        Authentication.AuthType expected = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckingUser(true)
                .build();

        Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingUser(true)
                .build();
        Authentication.AuthType fixedAuthType = AuthUtilities.fixCheckType(authType);

        new ProtobufComparisonBuilder().build().equals(expected, fixedAuthType);
    }

    @Test
    public void checkPeerTeacherOnlyReturnsEverythingBelow() {
        Authentication.AuthType expected = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckingUser(true)
                .setCheckingPeerTeacher(true)
                .build();

        Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingPeerTeacher(true)
                .build();
        Authentication.AuthType fixedAuthType = AuthUtilities.fixCheckType(authType);

        new ProtobufComparisonBuilder().build().equals(expected, fixedAuthType);
    }

    @Test
    public void checkModOnlyReturnsEverythingBelow() {
        Authentication.AuthType expected = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckingUser(true)
                .setCheckingPeerTeacher(true)
                .setCheckingMod(true)
                .build();

        Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingMod(true)
                .build();
        Authentication.AuthType fixedAuthType = AuthUtilities.fixCheckType(authType);

        new ProtobufComparisonBuilder().build().equals(expected, fixedAuthType);
    }

    @Test
    public void checkAdminOnlyReturnsEverythingBelow() {
        Authentication.AuthType expected = Authentication.AuthType.newBuilder()
                .setCheckAccess(true)
                .setCheckingUser(true)
                .setCheckingPeerTeacher(true)
                .setCheckingMod(true)
                .setCheckingAdmin(true)
                .build();

        Authentication.AuthType authType = Authentication.AuthType.newBuilder()
                .setCheckingAdmin(true)
                .build();
        Authentication.AuthType fixedAuthType = AuthUtilities.fixCheckType(authType);

        new ProtobufComparisonBuilder().build().equals(expected, fixedAuthType);
    }
}
