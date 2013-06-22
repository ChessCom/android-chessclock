package com.chess.backend.entity;

/**
 * FBContactItem class
 *
 * @author alien_roger
 * @created at: 21.08.12 22:41
 */
public class ContactItem {

	private String name;
	private String iconUrl;
	private String email;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


}
