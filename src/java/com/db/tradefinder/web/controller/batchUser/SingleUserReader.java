package com.db.tradefinder.web.controller.batchUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Model for a User who will be locked (generally because his work ended).
 * The user will be generated after reading a CSV file, {@link SingleUserReader}
 * @author Bernhard
 */
public class SingleUserReader {
    private int COL_ACCOUNT, COL_OWNER, COL_ACTION,	COL_REASON, COL_REQUEST_NO, COL_CREATION_DATE;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
	 * First line in CSV, it contains the headers. Since the headers don't have always the same index,
	 * i.e. the column 'action' may be in column 2 or column 10, we need to adjust the column indices (COL_*)
	 * @param lineContainingHeader i.e. "account;owner;action;reason;request_no;creation_date;"
	 */
	public void parseHeader(String lineContainingHeader) {
		String[] headers = lineContainingHeader.split(";");
		for (int i = 0; i < headers.length; i++) {
			String token = headers[i].trim();
			if (token.equals("account"))
				COL_ACCOUNT = i;
			else if (token.equals("owner"))
				COL_OWNER = i;
			else if (token.equals("action"))
				COL_ACTION = i;
			else if (token.equals("reason"))
				COL_REASON = i;
			else if (token.equals("request_no"))
				COL_REQUEST_NO = i;
			else if (token.equals("creation_date"))
				COL_CREATION_DATE = i;
		}
	}

	/**
	 * Parse one line (containing one user who will be locked) from a CSV file
	 * @param lineContainingUser a user (former employee of Deutsche Bank) who will be locked
	 * @return A SingleUserInBatch object
	 * @throws ParseException
	 */
	public SingleUserInBatch parseUser(String lineContainingUser) throws ParseException {
//		lineContainingUser = "niklas.schimmel@db.com;niklas.schimmel@db.com;revoke account;terminated user HR;" +
//				"2539897;15.04.2014 12:50;"; // some columns omitted
		SingleUserInBatch user = new SingleUserInBatch();
        String[] tokens = lineContainingUser.split(";");
		if (tokens.length < 6) {
			throw new ParseException(lineContainingUser + " has only " + tokens.length + " tokens, " +
					"but needs at least 6 tokens", -1);
		}
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i].trim();
			if (i == COL_ACCOUNT)	user.setAccount(token);
			if (i == COL_OWNER)		user.setOwner(token);
			if (i == COL_ACTION)	user.setAction(token);
			if (i == COL_REASON)	user.setReason(token);
			if (i == COL_REQUEST_NO) user.setRequestNo(Integer.valueOf(token));
			if (i == COL_CREATION_DATE) user.setCreationDate(sdf.parse(token));
		}
		return user;
	}
}
