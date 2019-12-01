package shiva.antlr

import org.junit.Test

import groovy.transform.CompileStatic
import shiva.cfg.ParseNode
import shiva.cfg.Rule
import shiva.cfg.StringDocument
import shiva.swingcfg.ParseTreeNode
import shiva.swingcfg.ParseTreeView

@CompileStatic
class TestGrammarGenerator {

	
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
		SmallAntlrGrammar ag = new SmallAntlrGrammar();
		StringDocument doc = new StringDocument(defn);
		ParseNode pn = ag.parse(doc)
		ParseTreeView.show(ParseTreeNode.construct(pn))
		
		GrammarGenerator.DEBUG = true
		GrammarGenerator gg = new GrammarGenerator(ag)
		StringDocument sent = new StringDocument(sentence)
		Rule start = gg.generate(pn, doc)[ruleName]
		start.setDescriptions()
		Rule.DEBUG = true
		return start.apply(sent, 0)
		
	}
}
