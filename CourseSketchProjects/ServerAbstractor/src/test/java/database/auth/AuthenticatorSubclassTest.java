package database.auth;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;

public class AuthenticatorSubclassTest {

    @Test(expected = IllegalArgumentException.class)
    public void creationFailsIfGivenNull() {
        new Authenticator(null);
    }

    @Test()
    public void creationSuccedsIfGivenValue() {
        final AuthenticationDataCreator data = Mockito.mock(AuthenticationDataCreator.class);
        new Authenticator(data);
    }

    @Test()
    public void userAuthFailsIfListIsNull() {
        final AuthenticationDataCreator data = Mockito.mock(AuthenticationDataCreator.class);
        final Authenticator auth = new Authenticator(data);
        assertFalse(auth.checkAuthentication("", null));
    }
}
