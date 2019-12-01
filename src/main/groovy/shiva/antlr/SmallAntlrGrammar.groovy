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

	/**
	 * Program -> Body EOF
	 * Body -> BodyPart*
	 * BodyPart -> LC | BC | RuleDef
	 * RuleDef -> Ident ':' Exp ';'
	 * Exp -> Seq | Alt | Opt | Star | Plus
	 * Seq -> Unit+
	 * Unit -> Lit | Reg | Ident | Group
	 * Group -> '(' Exp ')'
	 * Alt -> Unit ('|' Unit)+
	 * Opt -> Unit '?'
	 * Star -> Unit '*'
	 * Plus -> Unit '+'
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
	Rule Regex = seq(lit("/.*?/"))

	Rule LineComment = lineComment()
	Rule BlockComment = blockComment()

	Lazy Group = lazy()
	Rule Unit = alt(Literal, Regex, Ident, Group)

	Rule Seq = plus(Unit)
	Rule Alt = seq(Unit, plus(seq(Bar, Unit)))



	Rule Opt = seq(Unit, Qmark)
	Rule Star = seq(Unit, LitStar)
	Rule Plus = seq(Unit, LitPlus)

	Lazy Exp = lazy()
	Rule RuleDef = seq(Ident, lit(":"), Exp, lit(";"))
	Rule BodyPart = alt(LineComment, BlockComment, RuleDef)
	Rule Body = plus(BodyPart)
	Rule Program = seq(Body, eof())



	public SmallAntlrGrammar() {

		createRules()

		//		Exp.setRule(alt(Literal, Regex, Ident, OptExp, StarExp, PlusExp, AltExp, SeqExp))
		assignNamesToRules()
		Program.skipWsRecursive()
	}

	private void createRules() {

		Group.setRule(seq(LPar, Exp, RPar))
		Exp.setRule(alt(Alt, Opt, Star, Plus, Seq))
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
		String defn = SmallAntlrGrammar.class.getResource('/sargam.bnf').text

		StringDocument doc = new StringDocument(defn);
		Rule.DEBUG = true
		SmallAntlrGrammar ag = new SmallAntlrGrammar()
		ParseNode pn = ag.parse(doc)
		ParseTreeView.show(ParseTreeNode.construct(pn))

		GrammarGenerator gg = new GrammarGenerator(ag)
		gg.generate(pn, doc)

		println("done");
		//		println "/a\\/b".matches("^/.*?/\$")
	}
}
