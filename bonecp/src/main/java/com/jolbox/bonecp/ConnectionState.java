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

/**
 * An enum to indicate the state of a connection to be used as a signal via the onMarkPossiblyBroken
 * hook.
 * 
 * @author wallacew
 *
 */
public enum ConnectionState {
	/** Not sure/don't override default action. */
	NOP,
	/** Test this connection next time it goes back to the pool because it might be broken. */ 
	CONNECTION_POSSIBLY_BROKEN,
	/** Not only is this connection broken but all connections in this pool should be considered
	 * broken too.
	 */
	TERMINATE_ALL_CONNECTIONS;
}
