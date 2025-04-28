package io.joshuasalcedo.commonlibs;

/**
 * Utility class for entity operations.
 */
public class EntityUtil {

    /**
     * Checks if the given object is null.
     *
     * @param object the object to check
     * @return true if the object is null, false otherwise
     */
    public boolean isNull(Object object) {
        return object == null;
    }

    /**
     * Checks if the given string is null or empty.
     *
     * @param str the string to check
     * @return true if the string is null or empty, false otherwise
     */
    public boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}