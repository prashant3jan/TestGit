/**
 * 
 */
package com.struts2.action;

import com.opensymphony.xwork2.ActionSupport;

/**
 * @author prashant
 *
 */
public class LoginAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	private String username;
	private String password;
	
	public String showHome(){
		return "success";
	}
	
	public String showIndex(){
		System.out.println("show index executed");
		return "success";
	}
		
		
		public String authenticate(){
			System.out.println("username" + username);
			System.out.println("password"+password);
			
			if(this.username.equals("admin") && this.password.equals("admin")){
				return "success";
			}else{
				//addActionError(getText("error.login"));
				//a function from ActionSupport, to get properties value from properties file
				//we will explore this below.
				return "error";
				
			}
		}
		
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	

}
