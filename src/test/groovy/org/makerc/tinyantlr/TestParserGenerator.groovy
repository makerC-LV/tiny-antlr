package org.makerc.tinyantlr

import org.junit.Test

import static org.junit.Assert.*

import org.makerc.cfg.ParseNode
import org.makerc.cfg.Parser
import org.makerc.cfg.Rule
import org.makerc.cfg.StringDocument
import org.makerc.swingcfg.ParseTreeNode
import org.makerc.swingcfg.ParseTreeView
import org.makerc.tinyantlr.GeneratedParser
import org.makerc.tinyantlr.ParserGenerator
import org.makerc.tinyantlr.TinyAntlr

import groovy.transform.CompileStatic

@CompileStatic
class TestParserGenerator {

	static TinyAntlr ag = new TinyAntlr();
	
	@Test
	public void testGenerate() {
		String gram = "A : ('a' A) | B ;    B : 'b' ;"
		String sent = "aaab"
		String start = 'A'
		ParseNode pn = generateAndParse(gram, sent, start) 
		println pn
		assertTrue(pn.matched)
		
	}

	@Test
	public void testEOF() {
		String gram = "A : 'a' EOF ;"
		String sent = "a"
		String start = 'A'
		ParseNode pn = generateAndParse(gram, sent, start)
		println pn
		assertTrue(pn.matched)
		
	}
	
	static void main(String[] args) {
		String gram = "A : ('a' A) | B ;    B : 'b' ;"
		String sent = "aaab"
		String start = 'A'
		ParseNode pn = generateAndParse(gram, sent, start)
		println pn
	}
	
	
	static private ParseNode generateAndParse(String defn, String sentence, String ruleName) {
		Rule.DEBUG = true
		
		StringDocument doc = new StringDocument(defn);
		ParseNode pn = ag.parse(doc)
		ParseTreeView.show(ParseTreeNode.construct(pn))
		
		ParserGenerator.DEBUG = true
//		ParserGenerator gg = new ParserGenerator(ag)
//		StringDocument sent = new StringDocument(sentence)
		Parser gp = ag.createParser(defn)
		Rule start = gp.getRule(ruleName)
		start.setDescriptions()
		Rule.DEBUG = true
		return start.apply(new StringDocument(sentence), 0)
		
	}
}
