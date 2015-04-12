package database.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Used for tests so only one auth list has to be set for multiple collections.
 * For example, in grades the COURSE_COLLECTION and ASSIGNMENT_COLLECTION are accessed among other collections.
 * Using FakeAuthenticator means the permissions needs to be set once and then both collections will use the same permissions.
 * Example setup is below as well as a test where this is implemented.
 * @see database.institution.mongo.GradeManagerTest
 * 
 * <pre><code>
 * public Authenticator fauth;
 * public FakeAuthenticator fakeAuthenticator;
 * fakeAuthenticator = new FakeAuthenticator();
 * fauth = new Authenticator(fakeAuthenticator);
 * </code></pre>
 *
 * Created by matt on 4/12/15.
 */
public class FakeAuthenticator implements AuthenticationDataCreator {
    private List<String> userList;
    private List<String> modList;
    private List<String> adminList;

    public FakeAuthenticator(List<String> userList, List<String> modList, List<String> adminList) {
        this.userList = userList;
        this.modList = modList;
        this.adminList = adminList;
    }

    public FakeAuthenticator() {
        this(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
    }

    @Override public Authenticator.AuthenticationData getAuthGroups(String collection, String itemId) {
        Authenticator.AuthenticationData result = new Authenticator.AuthenticationData();
        result.setUserList(userList);
        result.setModeratorList(modList);
        result.setAdminList(adminList);
        return result;
    }

    public void setUserList(String ... users) {
        userList = Arrays.asList(users);
    }

    public void setModList(String ... mods) {
        modList = Arrays.asList(mods);
    }

    public void setAdminList(String ... admins) {
        adminList = Arrays.asList(admins);
    }

    /**
     * Always returns user list.
     * @param groupId
     *            The group Id
     * @return UserList
     */
    @Override public List<String> getUserList(String groupId) { return userList; }
}
