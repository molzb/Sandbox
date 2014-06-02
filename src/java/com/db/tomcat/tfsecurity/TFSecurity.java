package com.db.tomcat.tfsecurity;

public class TFSecurity {
	public static TFUser getTFUser() {
		return new TFUser("Bernhard", new String[] {"histsim_var", "tso", "dbclear_ldmanagement", "batch_user"});
	}
}
