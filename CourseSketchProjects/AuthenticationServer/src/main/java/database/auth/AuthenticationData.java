package database.auth;

/**
 * Created by gigemjt on 9/4/15.
 */

import protobuf.srl.utils.Util;

import java.util.List;

/**
 * Contains data used for authentication.
 * @author gigemjt
 */
public final class AuthenticationData {
    /**
     * A list of users that have access to the given data.
     */
    private List<String> userList;

    /**
     * A list of moderators that have access to the given data.
     */
    private List<String> moderatorList;

    /**
     * A list of admins that have access to the given data.
     */
    private List<String> adminList;

    /**
     * The date that the data is accessable.
     */
    private Util.DateTime accessDate;

    /**
     * The data that the data is no longer accessable.
     */
    private Util.DateTime closeDate;

    /**
     * False if the class does not require registration to view data.
     */
    private boolean registrationRequired;

    /**
     * @return the userList
     */
    public List<String> getUserList() {
        return userList;
    }

    /**
     * @param iUserList the userList to set
     */
    public void setUserList(final List<String> iUserList) {
        this.userList = iUserList;
    }

    /**
     * @return the moderatorList
     */
    public List<String> getModeratorList() {
        return moderatorList;
    }

    /**
     * @param iModeratorList the moderatorList to set
     */
    public void setModeratorList(final List<String> iModeratorList) {
        this.moderatorList = iModeratorList;
    }

    /**
     * @return the adminList
     */
    public List<String> getAdminList() {
        return adminList;
    }

    /**
     * @param iAdminList the adminList to set
     */
    public void setAdminList(final List<String> iAdminList) {
        this.adminList = iAdminList;
    }

    /**
     * @return the accessDate
     */
    public Util.DateTime getAccessDate() {
        return accessDate;
    }

    /**
     * @param iAccessDate the accessDate to set
     */
    public void setAccessDate(final Util.DateTime iAccessDate) {
        this.accessDate = iAccessDate;
    }

    /**
     * @return the closeDate
     */
    public Util.DateTime getCloseDate() {
        return closeDate;
    }

    /**
     * @param iCloseDate the closeDate to set
     */
    public void setCloseDate(final Util.DateTime iCloseDate) {
        this.closeDate = iCloseDate;
    }

    /**
     * @return False if the class is open and does not require registration
     */
    public boolean isRegistrationRequired() {
        return registrationRequired;
    }

    /**
     * @param isRegistrationRequired sets if the class requires registration
     */
    public void setRegistrationRequired(final boolean isRegistrationRequired) {
        this.registrationRequired = isRegistrationRequired;
    }
}
