package shiva.tinyantlr

import static shiva.cfg.Rules.*

import java.lang.reflect.Field
import java.lang.reflect.Type

import groovy.transform.CompileStatic
import shiva.cfg.DefaultParser
import shiva.cfg.ParseNode
import shiva.cfg.Rule
import shiva.cfg.StringDocument
import shiva.cfg.Rules.Sequence
import shiva.swingcfg.ParseTreeNode
import shiva.swingcfg.ParseTreeView

@CompileStatic
class TinyAntlr extends DefaultParser {

	/**
	 * Program -> Body EOF
	 * Body -> BodyPart*
	 * BodyPart -> LC | BC | RuleDef
	 * RuleDef -> Ident ':' Exp ';'
	 * Exp -> Seq | Alt 
	 * Seq -> Unit+
	 * Alt -> Unit ('|' Unit)+
	 * 
	 *  // Avoid left recursion
	 *  Unit -> Single PostfixOp ?
	 *  Single -> Lit | Reg | Ident | Group
	 *  PostfixOp -> '*' | '+' | '?'
	 *  
	 * Group -> '(' Exp ')'
	 *
	 */


	Rule LitStar = lit("*")
	Rule LitPlus = lit("+")
	Rule LPar = lit("(")
	Rule RPar = lit(")")
	Rule Qmark = lit("?")
	Rule Bar = lit("|")
	Rule Ident = identifier()
	Rule Literal = reg("'.*?'")
	Rule Regex = seq(reg("/.*?/"))
	Rule Eof = lit('EOF')
	
	Rule LineComment = lineComment()
	Rule BlockComment = blockComment()

	Lazy Exp = lazy()  // Exp.setRule(alt(Seq, Alt))
	Rule Group = seq(LPar, Exp, RPar)
	Rule PostfixOp = alt(LitStar, LitPlus, Qmark)
	Rule Single = alt(Literal, Regex, Ident, Eof, Group)
	Rule Unit = seq(Single, opt(PostfixOp))

	Rule Seq = plus(Unit)
	Rule AltTail = seq(Bar, Unit)
	Rule Alt = seq(Unit, plus(AltTail))

	Rule RuleDef = seq(Ident, lit(":"), Exp, lit(";"))
	Rule BodyPart = alt(LineComment, BlockComment, RuleDef)
	Rule Body = plus(BodyPart)
	Rule Program = seq(Body, eof())



	public TinyAntlr() {

		createRules()

		assignNamesToRules()
		Program.skipWsRecursive()
	}

	private void createRules() {

		Exp.setRule(alt(Alt, Seq))
	}



	public ParseNode parse(StringDocument doc) {

		return parse(Program, doc);
	}


	// Set the names of the instance rules
	protected assignNamesToRules() {
		for (Field f in this.class.declaredFields) {
			try {
				Type t = f.type
				if (Rule.class.isAssignableFrom((Class) t)) {
					Rule r = f.get(this) as Rule
					r.setName(f.name)
				} else {
				}
			} catch (IllegalAccessException) {
			}
		}
	}

	public static void main(String[] args) {
		String defn = TinyAntlr.class.getResource('/sargam.bnf').text

		StringDocument doc = new StringDocument(defn);
		Rule.DEBUG = true
		TinyAntlr ag = new TinyAntlr()
		ParseNode pn = ag.parse(doc)
		ParseTreeView.show(ParseTreeNode.construct(pn))

		ParserGenerator gg = new ParserGenerator(ag)
		gg.generate(pn, doc)

		println("done");
		//		println "/a\\/b".matches("^/.*?/\$")
	}
}
