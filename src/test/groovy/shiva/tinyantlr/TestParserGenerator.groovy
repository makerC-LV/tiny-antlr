package shiva.tinyantlr

import org.junit.Test

import groovy.transform.CompileStatic
import shiva.cfg.ParseNode
import shiva.cfg.Rule
import shiva.cfg.StringDocument
import shiva.swingcfg.ParseTreeNode
import shiva.swingcfg.ParseTreeView
import shiva.tinyantlr.GeneratedParser
import shiva.tinyantlr.ParserGenerator
import shiva.tinyantlr.TinyAntlr

@CompileStatic
class TestParserGenerator {

	
	public void testGenerate() {
		String gram = "A : ('a' A) | B ;    B : 'b' ;"
		String sent = "aaab"
		String start = 'A'
		ParseNode pn = generateAndParse(gram, sent, start) 
		println pn
		
	}

	static void main(String[] args) {
		String gram = "A : ('a' A) | B ;    B : 'b' ;"
		String sent = "aaab"
		String start = 'A'
		ParseNode pn = generateAndParse(gram, sent, start)
		println pn
	}
	static private ParseNode generateAndParse(String defn, String sentence, String ruleName) {
		Rule.DEBUG = false
		TinyAntlr ag = new TinyAntlr();
		StringDocument doc = new StringDocument(defn);
		ParseNode pn = ag.parse(doc)
		ParseTreeView.show(ParseTreeNode.construct(pn))
		
		ParserGenerator.DEBUG = true
		ParserGenerator gg = new ParserGenerator(ag)
		StringDocument sent = new StringDocument(sentence)
		GeneratedParser gp = gg.generate(pn, doc)
		Rule start = gp.getRule(ruleName)
		start.setDescriptions()
		Rule.DEBUG = true
		return start.apply(sent, 0)
		
	}
}
