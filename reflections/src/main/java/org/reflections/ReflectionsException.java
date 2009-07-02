/*
 * User: ophir
 * Date: Mar 28, 2009
 * Time: 12:52:22 AM
 */
package org.reflections;

public class ReflectionsException extends RuntimeException {
	// TODO: ReflectionsException should really be a checked exception

	public ReflectionsException(String message) {
		super(message);
	}

	public ReflectionsException(String message, Throwable cause) {
		super(message, cause);
	}
}
