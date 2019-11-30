package shiva.antlr

import static shiva.cfg.Rules.*

import java.lang.reflect.Field
import java.lang.reflect.Type

import groovy.transform.CompileStatic
import shiva.cfg.Grammar
import shiva.cfg.ParseNode
import shiva.cfg.Rule
import shiva.cfg.StringDocument
import shiva.cfg.Rules.Sequence
import shiva.swingcfg.ParseTreeNode
import shiva.swingcfg.ParseTreeView

@CompileStatic
class SmallAntlrGrammar extends Grammar {

	// lhs : exp ;
	// exp : literal | ident | pExp | qmarkExp | starExp | plusExp | altExp | seqExp
	// qmarkExp : exp ?
	// altExp : ( exp ( | exp )* )
	
	Rule SQuote = lit("'")
	Rule Star = lit("*")
	Rule Plus = lit("+")
	Rule LPar = lit("(")
	Rule RPar = lit(")")
	Rule Qmark = lit("?")
	Rule Bar = lit("?")
	Rule Ident = identifier()
	Rule Literal = reg("'.*?'")
	Rule Regex = seq(lit("/.*?/"))
	
	Lazy Exp = lazy()
	Rule OptExp = alt(seq(Ident, Qmark), seq(Exp, Qmark))
	
	Rule IdentStar = seq(Ident, Star)
	Rule ExpStar = seq(LPar, Exp, RPar, Star)
	Rule StarExp = alt(IdentStar,  ExpStar)
	
	Rule IdentPlus = seq(Ident, Plus)
	Rule ExpPlus = seq(LPar, Exp, RPar, Plus)
	
	Rule PlusExp = alt(IdentPlus,  ExpPlus)
	
	Rule OptTail = seq(Bar, Exp)
	Rule OptTailStar = star(OptTail)
	
	Rule AltExp = seq(LPar, Exp, OptTailStar, RPar)
	Rule SeqExp = seq(Exp)
	
	Rule LineComment = lineComment()
	Rule BlockComment = blockComment()
	
	
	Rule  ruleDef = seq(Ident, lit(":"), Exp, lit(";")).skipWs()
	Rule filePart = alt(LineComment, BlockComment, ruleDef).skipWs()
	Rule body = plus(filePart);
	Sequence start = seq(body, eof())
	
	public SmallAntlrGrammar() {
		Exp.setRule(alt(Literal, Regex, Ident, OptExp, StarExp, PlusExp, AltExp, SeqExp))
		
		assignNamesToRules()
	}
	
	public ParseNode parse() {

		String defn = this.getClass().getResource('/sargam.bnf').text
		
		StringDocument doc = new StringDocument(defn);
		
		return parse(start, doc);
		
	}
	
	// Set the names of the instance rules
	protected assignNamesToRules() {
		for (Field f in this.class.declaredFields) {
			try {
				Type t = f.type
//				println("Field $f.name class ${t.name}")
				if (Rule.class.isAssignableFrom((Class) t)) {
					Rule r = f.get(this) as Rule
//					println("Setting $f.name")
					r.setName(f.name)
				} else {
//					println "Not assignable"
				}
			} catch (IllegalAccessException) {
//				println "Exc for $f.name"
			}
		}
	}
	public static void main(String[] args) {
		Rule.DEBUG = true
		SmallAntlrGrammar ag = new SmallAntlrGrammar()
		ParseNode pn = ag.parse()
		ParseTreeView.show(ParseTreeNode.construct(pn))
		println("done");
//		println "/a\\/b".matches("^/.*?/\$")
	}
}
