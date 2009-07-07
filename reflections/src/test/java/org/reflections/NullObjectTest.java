package org.reflections;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class NullObjectTest {

	@Test
	public void testToString() {
		assertNull(NullObject.NULL.toString());
	}

	@Test
	public void testHashCode() {
		assertEquals(1, NullObject.NULL.hashCode());
	}

	@Test
	public void testEquals() {
		assertEquals(NullObject.NULL, NullObject.NULL);
	}
}
