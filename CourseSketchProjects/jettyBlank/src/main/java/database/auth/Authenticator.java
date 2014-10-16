package database.auth;

import static database.DatabaseStringConstants.GROUP_PREFIX;
import static database.DatabaseStringConstants.GROUP_PREFIX_LENGTH;

import java.util.List;

import protobuf.srl.school.School.DateTime;

/**
 * A class that performs authentication.
 *
 * This is abstract as the actual location where the data is grabbed is up to
 * implementation.
 *
 * @author gigemjt
 */
public final class Authenticator {

    /**
     * An implementation of the {@link AuthenticationDataCreator}.
     * Allows the data from authentication to come from multiple sources depending on the server.
     */
    private final AuthenticationDataCreator dataGrabber;

    /**
     * @param iDataGrabber Implements where the data actually comes from.
     */
    public Authenticator(final AuthenticationDataCreator iDataGrabber) {
        dataGrabber = iDataGrabber;
        if (iDataGrabber == null) {
            throw new IllegalArgumentException("The AuthenticationDataCreator can not be null.");
        }
    }

    /**
     * Checks to see if a user is allowed within the current groupList.
     *
     * looks up the groupId if there are groupId associated with this groupList
     *
     * @param userId
     *            the userId within the program that is trying to be
     *            authenticated
     * @param groups The list of names to check if the user exist in.
     * @return True if the userId is in the list of the group.
     */
    public boolean checkAuthentication(final String userId, final List<String> groups) {
        if (groups == null) {
            return false;
        }
        for (String group : groups) {
            if (group.startsWith(GROUP_PREFIX)) {
                final List<String> list = dataGrabber.getUserList(group.substring(GROUP_PREFIX_LENGTH));
                if (checkAuthentication(userId, list)) {
                    return true;
                }
            } else {
                if (group.equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param time The input time that is being checked.
     * @param openDate the input time has to be larger or equal to this date.
     * @param closeDate the input time has to be smaller or equal to this date.
     * @return True if the input time is between openDate and CloseDate.
     */
    public static boolean isTimeValid(final long time, final DateTime openDate, final DateTime closeDate) {
        return time >= openDate.getMillisecond() && time <= closeDate.getMillisecond();
    }

    /**
     * @param time The input time that is being checked.
     * @param openDate the input time has to be larger or equal to this date.
     * @param closeDate the input time has to be smaller or equal to this date.
     * @return True if the input time is between openDate and CloseDate.
     */
    public static boolean isTimeValid(final long time, final org.joda.time.DateTime openDate, final org.joda.time.DateTime closeDate) {
        return time >= openDate.getMillis() && time <= closeDate.getMillis();
    }

    /**
     * A list of Authentication options.
     * Compares against values in {@link AuthenticationData}.
     * @author gigemjt
     */
    public static final class AuthType {
        /**
         * If true then it checks against the userList in {@link AuthenticationData}.
         */
        private boolean user = false;

        /**
         * If true then it checks against the modlist in {@link AuthenticationData}.
         */
        private boolean mod = false;

        /**
         * If true then it checks against the adminlist in {@link AuthenticationData}.
         */
        private boolean admin = false;

        /**
         * If true then it checks that the dates in {@link AuthenticationData}.
         */
        private boolean checkDate = false;

        /**
         * If true then it passes if either mod or admin exist. {@link AuthenticationData}.
         */
        private boolean checkAdminOrMod = false;

        /**
         * @return True if one of the values in AuthType is true.
         */
        public boolean validRequest() {
            return isUser() || isMod() || isAdmin() || isCheckDate() || isCheckAdminOrMod();
        }

        /**
         * @return the user
         */
        public boolean isUser() {
            return user;
        }

        /**
         * @param iUser the user to set
         */
        public void setUser(final boolean iUser) {
            this.user = iUser;
        }

        /**
         * @return the mod
         */
        public boolean isMod() {
            return mod;
        }

        /**
         * @param iMod the mod to set
         */
        public void setMod(final boolean iMod) {
            this.mod = iMod;
        }

        /**
         * @return the admin
         */
        public boolean isAdmin() {
            return admin;
        }

        /**
         * @param iAdmin the admin to set
         */
        public void setAdmin(final boolean iAdmin) {
            this.admin = iAdmin;
        }

        /**
         * @return the checkDate
         */
        public boolean isCheckDate() {
            return checkDate;
        }

        /**
         * @param iCheckDate the checkDate to set
         */
        public void setCheckDate(final boolean iCheckDate) {
            this.checkDate = iCheckDate;
        }

        /**
         * @return the checkAdminOrMod
         */
        public boolean isCheckAdminOrMod() {
            return checkAdminOrMod;
        }

        /**
         * @param iCheckAdminOrMod the checkAdminOrMod to set
         */
        public void setCheckAdminOrMod(final boolean iCheckAdminOrMod) {
            this.checkAdminOrMod = iCheckAdminOrMod;
        }
    }

    /**
     * Contains data used for authentication.
     * @author gigemjt
     */
    public static final class AuthenticationData {
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
        private DateTime accessDate;

        /**
         * The data that the data is no longer accessable.
         */
        private DateTime closeDate;

        /**
         * @return the userList
         */
        List<String> getUserList() {
            return userList;
        }

        /**
         * @param iUserList the userList to set
         */
        void setUserList(final List<String> iUserList) {
            this.userList = iUserList;
        }

        /**
         * @return the moderatorList
         */
        List<String> getModeratorList() {
            return moderatorList;
        }

        /**
         * @param iModeratorList the moderatorList to set
         */
        void setModeratorList(final List<String> iModeratorList) {
            this.moderatorList = iModeratorList;
        }

        /**
         * @return the adminList
         */
        List<String> getAdminList() {
            return adminList;
        }

        /**
         * @param iAdminList the adminList to set
         */
        void setAdminList(final List<String> iAdminList) {
            this.adminList = iAdminList;
        }

        /**
         * @return the accessDate
         */
        DateTime getAccessDate() {
            return accessDate;
        }

        /**
         * @param iAccessDate the accessDate to set
         */
        void setAccessDate(final DateTime iAccessDate) {
            this.accessDate = iAccessDate;
        }

        /**
         * @return the closeDate
         */
        DateTime getCloseDate() {
            return closeDate;
        }

        /**
         * @param iCloseDate the closeDate to set
         */
        void setCloseDate(final DateTime iCloseDate) {
            this.closeDate = iCloseDate;
        }
    }

    /**
     * Checks to make sure that the user is authenticated for all values that
     * are true.
     *
     * @param collection The table / collection where this data is store.
     * @param itemId
     *            The Id of the object we are checking against.
     * @param userId
     *            The user we are checking is valid
     * @param checkTime The time that the date check is checking against.
     * @param checkType The rules at that give a correct or false response.
     * @return True if all checked values are valid
     * @throws DatabaseAccessException
     */
    public boolean isAuthenticated(final String collection, final String itemId,
            final String userId, final long checkTime, final Authenticator.AuthType checkType) {

        if (!checkType.validRequest()) {
            return false;
        }

        final AuthenticationData result = dataGrabber.getAuthGroups(collection, itemId);

        final boolean validUser = authenticateUser(userId, result, checkType);

        boolean validModOrAdmin = authenticateModerator(userId, result, checkType);
        final boolean validMod = checkType.isMod() && validModOrAdmin;

        boolean validAdmin = authenticateAdmin(userId, result, checkType);
        validModOrAdmin = validAdmin || validModOrAdmin;
        validAdmin = validAdmin && checkType.isAdmin();

        boolean validDate = false;
        if (checkType.isCheckDate()) {
            validDate = Authenticator.isTimeValid(checkTime, result.getAccessDate(), result.getCloseDate());
        }

        return validUser == checkType.isUser() && validMod == checkType.isMod() && validAdmin == checkType.isAdmin()
                && validDate == checkType.isCheckDate() && validModOrAdmin == checkType.isCheckAdminOrMod();
    }

    /**
     * Authenticates just the user list.
     * @param userId the user being authenticated.
     * @param result contains the user list.
     * @param checkType contains data about what is being checked.
     * @return true if the user is authenticated.  false if it is not being checked or if the user is not authenticated.
     */
    private boolean authenticateUser(final String userId, final AuthenticationData result, final Authenticator.AuthType checkType) {
        boolean validUser = false;
        if (checkType.isUser()) {
            final List usersList = result.getUserList();
            validUser = this.checkAuthentication(userId, usersList);
        }
        return validUser;
    }

    /**
     * Authenticates just the moderator list.
     * @param userId the user being authenticated.
     * @param result contains the moderator list.
     * @param checkType contains data about what is being checked.
     * @return true if the user is authenticated.  false if it is not being checked or if the moderator is not authenticated.
     */
    private boolean authenticateModerator(final String userId, final AuthenticationData result, final Authenticator.AuthType checkType) {
        boolean validMod = false;
        if (checkType.isMod() || checkType.isCheckAdminOrMod()) {
            final List modList = result.getModeratorList();
            validMod = this.checkAuthentication(userId, modList);
        }
        return validMod;
    }

    /**
     * Authenticates just the admin list.
     * @param userId the user being authenticated.
     * @param result contains the admin list.
     * @param checkType contains data about what is being checked.
     * @return true if the user is authenticated.  false if it is not being checked or if the admin is not authenticated.
     */
    private boolean authenticateAdmin(final String userId, final AuthenticationData result, final Authenticator.AuthType checkType) {
        boolean validAdmin = false;
        if (checkType.isAdmin() || checkType.isCheckAdminOrMod()) {
            final List adminList = result.getAdminList();
            validAdmin = this.checkAuthentication(userId, adminList);
        }
        return validAdmin;
    }
}
