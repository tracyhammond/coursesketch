package connection;

/**
 * Contains values for permission to allow or disallow access to certain content.
 * @author gigemjt
 */
public enum Permission {
	ADMIN(100), INSTRUCTOR(10), ASSISTANT(5), PEER(3), MONITOR(2), NO_PERMISSION(0), ERROR_PERMISSON(-1);
	private final int permission;

	private Permission(int p) {
		permission = p;
	}

	/**
	 * Returns true if the permissions of a > the permissions of b.
	 */
	public static boolean greaterThan(Permission a, Permission b) {
		return a.permission > b.permission;
	}

	/**
	 * Returns true if the permissions of a >= the permissions of b.
	 */
	public static boolean greaterThanEqual(Permission a, Permission b) {
		return a.permission >= b.permission;
	}

	/**
	 * Returns true if the permissions of a < the permissions of b.
	 */
	public static boolean lessThan(Permission a, Permission b) {
		return a.permission < b.permission;
	}

	/**
	 * Returns true if the permissions of a <= the permissions of b.
	 */
	public static boolean lessThanEqual(Permission a, Permission b) {
		return a.permission <= b.permission;
	}
}