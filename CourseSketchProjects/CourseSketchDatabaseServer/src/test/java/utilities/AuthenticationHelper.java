package utilities;

import database.DatabaseAccessException;
import database.auth.AuthenticationChecker;
import database.auth.AuthenticationException;
import protobuf.srl.school.School;
import protobuf.srl.services.authentication.Authentication;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by gigemjt on 9/5/15.
 */
public class AuthenticationHelper {

    /**
     * TODO: add this to a general course sketch testing library.
     * If element is not null it will create an eq matcher if element is not null then it will create an any matcher
     * @param element
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T createAnyEqMatcher(T element, Class<T> tClass) {
        if (element == null && tClass == null) {
            return eq(null);
        } else if(element == null) {
            return any(tClass);
        } else {
            return eq(element);
        }
    }

    /**
     * Send in null to match any type
     * @param type
     * @param itemId
     */
    public static void setMockPermissions(AuthenticationChecker authChecker, School.ItemType type, String itemId, String userId,
            Authentication.AuthType authType, Authentication.AuthResponse.PermissionLevel result)
            throws DatabaseAccessException, AuthenticationException {
        // specific results
        when(authChecker.isAuthenticated(
                createAnyEqMatcher(type, School.ItemType.class),
                createAnyEqMatcher(itemId, String.class),
                createAnyEqMatcher(userId, String.class),
                createAnyEqMatcher(authType, Authentication.AuthType.class)))
                .thenReturn(Authentication.AuthResponse.newBuilder()
                        .setPermissionLevel(result)
                        .build());
    }
}
