package org.sinoc.shell.model.web;

public class ClientLoginInfo {
	private String userName;
	private String password;
	private boolean success;
	private String errMsg;
	
	public ClientLoginInfo() {
		super();
	}
	
	public ClientLoginInfo(String userName, String password) {
		super();
		this.userName = userName;
		this.password = password;
	}
	
	public ClientLoginInfo(boolean success, String errMsg) {
		this.success = success;
		this.errMsg = errMsg;
	}
	
	
	
	public ClientLoginInfo(String userName, String password, boolean success, String errMsg) {
		super();
		this.userName = userName;
		this.password = password;
		this.success = success;
		this.errMsg = errMsg;
	}

	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
}
