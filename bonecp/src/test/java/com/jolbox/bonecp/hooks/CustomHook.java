/**
 * Copyright 2009 Wallace Wadge
 *
 * This file is part of BoneCP.
 *
 * BoneCP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BoneCP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BoneCP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jolbox.bonecp.hooks;

import com.jolbox.bonecp.ConnectionHandle;

/** JUnit helper.
 * @author wallacew
 *
 */
public class CustomHook extends AbstractConnectionHook{
    /** junit helper.*/
	int acquire;
	/** junit helper.*/
	int checkin;
	/** junit helper.*/
	int checkout;
	/** junit helper.*/
	int destroy;
	/** junit helper.*/
	int fail;

	@Override
	public void onAcquire(ConnectionHandle connection) {
		this.acquire++;
	}

	@Override
	public void onCheckIn(ConnectionHandle connection) {
		this.checkin++;
	}

	@Override
	public void onCheckOut(ConnectionHandle connection) {
		this.checkout++;
	}
	
	@Override
	public void onDestroy(ConnectionHandle connection) {
		this.destroy++;
	}

	@Override
	public boolean onAcquireFail(Throwable t) {
		this.fail++;
		if (this.fail < 3){
			return true; // try 3 times
		} 
		
		return false;

	}
}
