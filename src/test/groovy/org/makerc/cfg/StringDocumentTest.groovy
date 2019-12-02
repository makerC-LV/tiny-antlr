package org.makerc.cfg

import static org.junit.Assert.*

import org.junit.Test
import org.makerc.cfg.StringDocument

class StringDocumentTest {

	@Test
	public void testSubseq() {
		StringDocument doc = new StringDocument("abcdef")
		assertEquals("de", doc.subseq(3, 5))
	}

	@Test
	public void testLength() {
		StringDocument doc = new StringDocument("abcdef")
		assertEquals(6, doc.length())
	}

	@Test
	public void testStartsWith() {
		StringDocument doc = new StringDocument("abcdef")
		assertTrue(doc.startsWith("abc", 0))
		assertTrue(doc.startsWith("bc", 1))
		assertFalse(doc.startsWith("abd", 0))
	}

	@Test
	public void testTranslateOffset() {
		StringDocument doc = new StringDocument("abcdef\nghijk")
		int[] r = doc.translateOffset(2)
		assertEquals(0, r[0])
		assertEquals(2, r[1])
		r = doc.translateOffset(8)
		assertEquals(1, r[0])
		assertEquals(2, r[1])
		
	}

	@Test
	public void testGetDiagnostic() {
		StringDocument doc = new StringDocument("abcdef")
		CharSequence cs = 
		assertEquals("def", doc.getDiagnostic(3, 50))
		assertEquals("d", doc.getDiagnostic(3, 1))
		assertEquals("<EOF>", doc.getDiagnostic(6, 50))
	}

}
