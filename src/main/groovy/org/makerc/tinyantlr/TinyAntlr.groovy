package org.makerc.tinyantlr

import java.lang.reflect.Field
import java.lang.reflect.Type

import org.makerc.cfg.DefaultParser
import org.makerc.cfg.Document
import org.makerc.cfg.ParseNode
import org.makerc.cfg.Parser
import org.makerc.cfg.Rule
import org.makerc.cfg.StringDocument
import org.makerc.swingcfg.ParseTreeNode
import org.makerc.swingcfg.ParseTreeView

import static org.makerc.cfg.Rules.*

import groovy.transform.CompileStatic

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
	Rule Single = alt(Literal, Regex, Eof, Ident, Group)
	Rule Unit = seq(Single, opt(PostfixOp))

	Rule Seq = plus(Unit)
	Rule AltTail = seq(Bar, Unit)
	Rule Alt = seq(Unit, plus(AltTail))

	Rule RuleDef = seq(Ident, lit(":"), Exp, lit(";"))
	Rule BodyPart = alt(LineComment, BlockComment, RuleDef)
	Rule Body = plus(BodyPart)
	Rule Program = seq(Body, eof())


	ParserGenerator pg = new ParserGenerator(this);

	public TinyAntlr() {
		createRules()
		assignNamesToRules()
		Program.skipWsRecursive()
	}

	private void createRules() {
		Exp.setRule(alt(Alt, Seq))
	}

	public Parser createParser(String grammarString) {
		return createParser(new StringDocument(grammarString))
	}
	
	public Parser createParser(Document grammarDoc) {
		ParseNode pn = parse(grammarDoc)
		return pg.generate(pn, grammarDoc)
	}
	
	public ParseNode parse(Document doc) {
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

}
