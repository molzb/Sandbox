package com.db.tomcat.tfsecurity;

import java.util.ArrayList;
import java.util.Collection;

public class TFUser {

	private final Collection<TFDetailedPermission> permissions = new ArrayList<TFDetailedPermission>();
	String username;

	public TFUser(String username, String[] permissionArray) {
		this.username = username;
		for (String p : permissionArray) {
			permissions.add(new TFDetailedPermission(p));
		}
	}

	public boolean isPermitted(String p) {
		for (TFDetailedPermission tfdp : permissions) {
			if (tfdp.getName().equals(p)) {
				return true;
			}
		}
		return false;
	}
	
	public String getUsername() {
		return username;
	}
	
	public Collection<TFDetailedPermission> getAllMyPermissions() {
		return permissions;
	}
}
