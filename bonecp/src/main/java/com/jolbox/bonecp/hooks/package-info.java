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

/**
 * Support for event notification on a connection state. 
 * 
 * Use the hook mechanism to register callbacks when a connection's state changes. Most applications will want to extend
 * {@link com.jolbox.bonecp.hooks.AbstractConnectionHook} rather than implementing the {@link com.jolbox.bonecp.hooks.ConnectionHook} interface directly.
 * 
 */
package com.jolbox.bonecp.hooks;