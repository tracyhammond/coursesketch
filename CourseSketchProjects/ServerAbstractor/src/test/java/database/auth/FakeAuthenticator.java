package database.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Used for tests so only one auth list has to be set for multiple collections.
 * For example, in grades the COURSE_COLLECTION and ASSIGNMENT_COLLECTION are accessed among other collections.
 * Using FakeAuthenticator means the permissions needs to be set once and then both collections will use the same permissions.
 * Example setup is below. See DatabaseServer test database.institution.mongo.GradeManagerTest for an example usage.
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

    /**
     * This will create authentications with empty lists.
     */
    public FakeAuthenticator() {
        this(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
    }

    /**
     * @return data that contains the userList, modList, and the adminList.  The inputs are ignored.
     */
    @Override public Authenticator.AuthenticationData getAuthGroups(String collection, String itemId) {
        Authenticator.AuthenticationData result = new Authenticator.AuthenticationData();
        result.setUserList(userList);
        result.setModeratorList(modList);
        result.setAdminList(adminList);
        return result;
    }

    /**
     * A helper function that lets you set the list without requiring a list to be created.
     * @param users a list of users we want to fake authentication for
     */
    public void setUserList(String ... users) {
        userList = Arrays.asList(users);
    }

    /**
     * A helper function that lets you set the list without requiring a list to be created.
     * @param mods a list of moderators we want to fake authentication for
     */
    public void setModList(String ... mods) {
        modList = Arrays.asList(mods);
    }

    /**
     * A helper function that lets you set the list without requiring a list to be created.
     * @param admins a list of administrators we want to fake authentication for
     */
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
