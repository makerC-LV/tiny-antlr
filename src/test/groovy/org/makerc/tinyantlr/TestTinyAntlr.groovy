package org.makerc.tinyantlr

import org.junit.Test
import org.makerc.cfg.ParseNode
import org.makerc.cfg.Rule
import org.makerc.cfg.StringDocument
import org.makerc.swingcfg.ParseTreeNode
import org.makerc.swingcfg.ParseTreeView
import org.makerc.tinyantlr.TinyAntlr

import static org.junit.Assert.*

class TestTinyAntlr {

	TinyAntlr sg = new TinyAntlr();
	
	@Test
	public void testGroup() {
		ParseNode pn2 = test("(b | c)", sg.Group)
		assertTrue(pn2.matched)
		assertEquals(7, pn2.matchLength)
		
		pn2 = test("( b c )  ", sg.Group)
		assertTrue(pn2.matched)
		assertEquals(7, pn2.matchLength)
		
	}

	
	@Test
	public void testSeq() {
		ParseNode pn = test("a a* b+ c? (d | e)", sg.Seq)
		assertTrue(pn.matched)
		assertEquals(18, pn.matchLength)

		ParseNode pn2 = test("(b | c) a", sg.Seq)
		assertTrue(pn2.matched)
		assertEquals(9, pn2.matchLength)
		
	}
	
	@Test
	public void testAlt() {
		ParseNode pn = test("a | a* | (b c)+ | c?", sg.Alt)
		//ParseTreeView.show(ParseTreeNode.construct(pn))
		assertTrue(pn.matched)
		assertEquals(20, pn.matchLength)
		
	}
	
	@Test 
	public void testRegex() {
		Rule.DEBUG = true
		ParseNode pn = test("/abc/", sg.Regex)
		assertTrue(pn.matched)
		assertEquals(5, pn.matchLength)
		
	}
	
	@Test
	public void testUnit() {
		
		ParseNode pn = test("a", sg.Unit)
		assertTrue(pn.matched)
		assertEquals(1, pn.matchLength)
		ParseTreeView.show(ParseTreeNode.construct(pn))
		
		pn = test("a*", sg.Unit)
		assertTrue(pn.matched)
		assertEquals(2, pn.matchLength)
		
	}
	
	@Test
	public void testPostfixOp() {
		
		ParseNode pn = test("a", sg.PostfixOp)
		assertTrue(pn.matched)
		assertEquals(1, pn.matchLength)
	}
	
	@Test
	public void testRuledef() {
		
		ParseNode pn = test("a : (b c)* d (e | f) ;", sg.RuleDef)
		assertTrue(pn.matched)
		assertEquals(22, pn.matchLength)
		
		
		pn = test("VAR : /[A-Z][A-Z0-9_]+/ ;", sg.RuleDef)
		assertTrue(pn.matched)
		assertEquals(25, pn.matchLength)
	}
	
	static private ParseNode test(String sentence, Rule rule) {
		
		StringDocument doc = new StringDocument(sentence);
		//rule.resetCache()
		rule.setDescriptions()
		ParseNode pn = rule.apply(doc, 0)
//		ParseTreeView.show(ParseTreeNode.construct(pn))
		println pn
		return pn
	}
	
	static void main(String[] args) {
		new TestTinyAntlr().testUnit();
	}

}
