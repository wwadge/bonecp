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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.slf4j.Logger;

import static org.easymock.EasyMock.createNiceMock;


/**
 * Utility methods to simplify testing.
 *
 * @author Mark Woon
 */
public class TestUtils {

  /**
   * Creates a mock logger, sets it in the specified class, and returns it for configuration.
   *
   * @param loggingClass the class with a private static final Logger
   * @return the mocked logger
   * @throws Exception if anything goes wrong
   */
  static Logger mockLogger(Class loggingClass) throws NoSuchFieldException, IllegalAccessException {

    Logger mockLogger = createNiceMock(Logger.class);
    Field field = loggingClass.getDeclaredField("logger");
    setFinalStatic(field, mockLogger);
    return mockLogger;
  }

  static void setFinalStatic(Field field, Object newValue) throws NoSuchFieldException,
      IllegalAccessException  {

    field.setAccessible(true);
    // remove final modifier from field
    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.set(null, newValue);
  }
}
