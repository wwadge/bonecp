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
