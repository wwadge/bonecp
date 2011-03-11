/**
 *  Copyright 2010 Wallace Wadge
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.jolbox.bonecp;

import com.google.common.base.Objects;

/**
 * Composite key handle for datasource.
 * @author wallacew
 *
 */
public class UsernamePassword {

	/** Handle to store a username. */
	private String username;
	/** Handle to store a password. */
	private String password;

	/** Default constructor.
	 * @param username
	 * @param password
	 */
	public UsernamePassword(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Returns the username field.
	 * @return username
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Returns the password field.
	 * @return password
	 */
	public String getPassword() {
		return this.password;
	}

	
	@Override
	public boolean equals(Object obj) {
		   if(obj instanceof UsernamePassword){
		        final UsernamePassword that = (UsernamePassword) obj;
		        return Objects.equal(this.username, that.getUsername())
		            && Objects.equal(this.password, that.getPassword());
		    } 
		        return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.username, this.password);
	}
}
