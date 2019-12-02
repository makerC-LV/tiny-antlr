package org.makerc.cfg

import static org.junit.Assert.*

import groovy.transform.CompileStatic

import static Rules.*
import org.junit.Test
import org.makerc.cfg.Document
import org.makerc.cfg.ParseNode
import org.makerc.cfg.Rule
import org.makerc.cfg.StringDocument

class TestRules {

	Document doc = new StringDocument("aaa b b    ")
	
	String str = "012345 \
    c d "
	Document doc2 = new StringDocument(str)
	
	@Test
	public void testWs() {
		Rule ws = ws()
		ParseNode n = ws.apply(doc, 0)
		assertFalse(n.matched);
		n = ws.apply(doc, 3)
		assertTrue(n.matched);
		n = ws.apply(doc, 7)
		assertTrue(n.matched);
		assertEquals(4, n.matchLength)
		
		n = ws.apply(doc2, 6)
		assertTrue(n.matched);
		assertEquals(5 , n.matchLength)
	}

	@Test
	public void testEof() {
		fail("Not yet implemented") // TODO
	}

	@Test
	public void testOpt() {
		Rule opt = opt(lit("a"));
		ParseNode n = opt.apply(doc, 0)
		assertTrue(n.matched);
		assertEquals(1, n.matchLength)
		n = opt.apply(doc, 3)
		assertTrue(n.matched);
		assertEquals(0, n.matchLength)
	}

	@Test
	public void testLit() {
		fail("Not yet implemented") // TODO
	}

	@Test
	public void testStar() {
		Rule st1 = star(lit("a"))
		ParseNode n = st1.apply(doc, 0)
		assertTrue(n.matched);
		assertEquals(3, n.matchLength)
		n = st1.apply(doc, 3)
		assertTrue(n.matched);
		assertEquals(0, n.matchLength)
		
		
		Rule st2 = star(lit("b"))
		n = st2.apply(doc, 4)
		assertTrue(n.matched);
		assertEquals(1, n.matchLength)
		
		st2.resetCache()
		st2.skipWs()
		
		n = st2.apply(doc, 4)
		assertTrue(n.matched);
		assertEquals(3, n.matchLength)
		
	}

	@Test
	public void testPlus() {
		Rule plus = plus(lit("a"));
		ParseNode n = plus.apply(doc, 0)
		assertTrue(n.matched);
		assertEquals(3, n.matchLength)
		n = plus.apply(doc, 3)
		assertFalse(n.matched);
		assertEquals(-1, n.matchLength)
	}

	@Test
	public void testAlt() {
		fail("Not yet implemented") // TODO
	}

	@Test
	public void testSeq() {
		Rule a = lit("a")
		Rule b = lit("b")
		ParseNode n
		Sequence sq = seq(a, a, a)
		n = sq.apply(doc, 0)
		assertTrue(n.matched);
		assertEquals(3, n.matchLength)
		
		
		Sequence sq2 = seq(a, a, a, b, b).skipWs()
		n = sq2.apply(doc, 0)
		assertTrue(n.matched);
		
		
		Sequence sq4 = seq(a, a, a, b, b)
		n = sq4.apply(doc, 0)
		assertFalse(n.matched);
		
		Sequence sq3 = seq(b, b).skipWs()
		n = sq3.apply(doc, 4)
		assertTrue(n.matched);
		assertEquals(3, n.matchLength)
		
		
		
		
	}

	@Test
	public void testLazy() {
		Lazy lazy = lazy();
		Rule al = seq(lit("a"), alt(lazy, eps()))
		lazy.setRule(al)
		ParseNode n = al.apply(doc, 0)
		assertTrue(n.matched);
		assertEquals(3, n.matchLength)
		n = al.apply(doc, 3)
		assertFalse(n.matched);
		assertEquals(-1, n.matchLength)
	}
	
	String lcStr = "01234// this is some text"
	Document lcDoc = new StringDocument(lcStr)
	@Test
	public void testLineComment() {
		Rule lc = lineComment()
		ParseNode n = lc.apply(lcDoc, 5)
		assertTrue(n.matched);
		assertEquals(20, n.matchLength)
		n = lc.apply(lcDoc, 0)
		assertFalse(n.matched);
	}
	
	String bcStr = "01234/* this is some text \
1234*/"
	Document bcDoc = new StringDocument(bcStr)
	@Test
	public void testBlockComment() {
		Rule lc = blockComment()
		ParseNode n = lc.apply(bcDoc, 5)
		assertTrue(n.matched);
		assertEquals(27, n.matchLength)
		n = lc.apply(bcDoc, 0)
		assertFalse(n.matched);
	}
	

}
