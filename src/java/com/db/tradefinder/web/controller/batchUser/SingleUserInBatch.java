package com.db.tradefinder.web.controller.batchUser;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Model for the CSV file containing the users that are to be deleted from our DB (table TRADEFINDER_USERS)
 * @author Bernhard
 */
public class SingleUserInBatch {
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private String account;
	private String owner;
	private String action;
	private String reason;
	private int requestNo;
	private Date creationDate;

	@Override
	public String toString() {
		String s = "BatchUserCsv [account=%s, owner=%s, action=%s, reason=%s, requestNo=%s, creationDate=%s]";
		return String.format(s, account, owner, action, reason, requestNo, sdf.format(creationDate));
	}
	
	public String toJSON() {
		String s = "{\"account\": \"%s\", \"owner\": \"%s\", \"action\": \"%s\", \"reason\": \"%s\", " + 
				"\"requestNo\": \"%s\", \"creationDate\": \"%s\"}";
		return String.format(s, account, owner, action, reason, requestNo, sdf.format(creationDate));
	}

	public String getAccount() {
		return account;
	}

	public String getOwner() {
		return owner;
	}

	public String getAction() {
		return action;
	}

	public String getReason() {
		return reason;
	}

	public int getRequestNo() {
		return requestNo;
	}

	public Date getCreationDate() {
		return creationDate;
	}

    public void setSdf(SimpleDateFormat sdf) {
        this.sdf = sdf;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setRequestNo(int requestNo) {
        this.requestNo = requestNo;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
