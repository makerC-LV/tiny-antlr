package shiva.cfg

import java.util.regex.Matcher
import java.util.regex.Pattern

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
class Rules {


	@TupleConstructor
	static class Lazy extends Rule {
		Rule rule

		@Override
		public ParseNode apply(Document doc, int start) {
			return rule.apply(doc, start);
		}

		@Override
		protected ParseNode applyInternal(Document doc, int start) {
			return null; // Should never happen
		}

		@Override
		protected Rule[] dependencies() {
			return [rule] as Rule[]
		}

		@Override
		protected String descriptionFromDependencies() {
			return rule.getDescription();
		}
	}

	@TupleConstructor
	static class Alternatives extends Rule {
		Rule[] alt;

		@Override
		protected ParseNode applyInternal(Document doc, int start) {
			
			for (Rule app in alt) {
				ParseNode r = app.apply(doc, start);
				if (r.matched) {
					ParseNode res =  passNode(start, r.matchLength, r);
					return res;
				}
			}
			return failNode(start);
		}

		@Override
		protected String descriptionFromDependencies() {
			StringBuilder sb = new StringBuilder()
			boolean first = true
			for (Rule app in alt) {
				if (!first) {
					sb.append("|")
				}
				sb.append(app.getDescription())
				first = false
			}
			return sb.toString();
		}

		@Override
		protected Rule[] dependencies() {
			return alt;
		}
		
	}

	@TupleConstructor
	static class Star extends Rule {
		Rule rule;

		@Override
		protected ParseNode applyInternal(Document doc, int start) {
			//Result myRes = new Result(true, this, start, 0, null);
			List<ParseNode> children = [];
			ParseNode res
			int wslen
			int len
			int index = start
			while ((res = rule.apply(doc, index)).matched)  {
				index += res.matchLength
				len += res.matchLength
				children << res
				if (ignoreWhitespace) {
					wslen = skipWhitespace(doc, index)
					index += wslen
					len += wslen
				}
			}
			if (ignoreWhitespace) {
				len -= wslen
			}
			return passNode(start, len, children as ParseNode[])
		}

		@Override
		protected String descriptionFromDependencies() {
			return rule.getDescription() + "*";
		}

		@Override
		protected Rule[] dependencies() {
			return [rule] as Rule[];
		}
		
	}

	@TupleConstructor
	static class Opt extends Rule {
		Rule rule;

		@Override
		protected ParseNode applyInternal(Document doc, int start) {
			//Result myRes = new Result(true, this, start, 0, null)
			ParseNode res = rule.apply(doc, start)
			if (res.matched)  {
				return passNode(start, res.matchLength, res)
			} else {
				return passNode(start, 0)
			}
		}

		@Override
		protected String descriptionFromDependencies() {
			return rule.getDescription() + "?";
		}

		@Override
		protected Rule[] dependencies() {
			return [rule] as Rule[];
		}

		
	}

	@TupleConstructor
	public static class Sequence extends Rule {
		Rule[] elements;

		@Override
		protected ParseNode applyInternal(Document doc, int start) {
			//Result myRes = new Result(true, this, start, 0, null);
			List<ParseNode> children = []
			int wslen
			int len
			int index = start
			for (app in elements) {
				ParseNode res = app.apply(doc, index);
				children << res
				if (!res.matched) {
					return failNode(start, children as ParseNode[])
				} else {
					index += res.matchLength
					len += res.matchLength
					if (ignoreWhitespace) {
						wslen = skipWhitespace(doc, index)
						index += wslen
						len += wslen
					}
				}
			}
			if (ignoreWhitespace) {
				len -= wslen
			}
			return passNode(start, len, children as ParseNode[])
		}

		Sequence separatedBy(Rule a) {
			int newLen = elements.length * 2 - 1;
			Rule[] ne = new Rule[newLen]
			int i = 0
			while (i < newLen) {
				if (i > 0) {
					ne[i++] = a;
				}
				ne[i++] = elements[(int) ((i+1)/2)];
			}
			elements = ne
			return this
		}

//		Sequence spaceSep() {
//			return separatedBy(opt(ws()));
//		}

		@Override
		protected String descriptionFromDependencies() {
			StringBuilder s = new StringBuilder();
			s.append("Seq[")
			boolean first = true
			for (Rule r in elements) {
				if (!first) {
					s.append ", "
				}
				s.append  r.getDescription()
				first = false
			}
			s.append("]")
			return s.toString()
		}

		@Override
		protected Rule[] dependencies() {
			return elements;
		}

		

	}

	
	static class Plus extends Sequence {
		Plus(Rule a) {
			elements = [a, new Star(a)]
		}
		@Override
		protected String descriptionFromDependencies() {
			return elements[0].getDescription() + "+";
		}
	}

	@TupleConstructor
	static class Literal extends Rule {

		String literal;

		@Override
		protected String descriptionFromDependencies() {
			return "'" + literal + "'";
		}

		@Override
		protected ParseNode applyInternal(Document doc, int start) {
			ParseNode res
			boolean matched = doc.startsWith(literal, start)
			if (matched) {
				res = passNode(start, literal.length());
			} else {
				res = failNode(start);
			}
			return res
		}

		@Override
		protected Rule[] dependencies() {
			return [] as Rule[];
		}

	}

	
	static class Regex extends Rule {
		String regex
		Pattern pat

		public Regex(String reg, int patternFlag = -1) {
			this.regex = reg;
			pat = patternFlag == -1 ? Pattern.compile(regex) : Pattern.compile(regex, patternFlag)
		}

		@Override
		protected String descriptionFromDependencies() {
			return "re(" + regex + ")"
		}

		@Override
		protected ParseNode applyInternal(Document doc, int start) {
			CharSequence s = doc.subseq(start, doc.length())
			Matcher m = pat.matcher(s)
			if (m.find() && m.start() == 0) {
				int len = m.group(0).length();
				return passNode(start, len);
			} else {
				return failNode(start);
			}
		}

		@Override
		protected Rule[] dependencies() {
			return [] as Rule[];
		}

	}


	
	static class EOF extends Rule {

		@Override
		protected String descriptionFromDependencies() {
			return "<EOF>";
		}

		@Override
		protected ParseNode applyInternal(Document doc, int start) {
			if (start == doc.length()) {
				return passNode(start, 0)
			} else {
				return failNode(start);
			}
		}

		@Override
		protected Rule[] dependencies() {
			return [] as Rule[];
		}
	}

	
	static class EPS extends Rule {

		@Override
		protected String descriptionFromDependencies() {
			return "*Eps";
		}

		@Override
		public ParseNode apply(Document doc, int start) {
			return passNode(start, 0)
		}
		@Override
		protected ParseNode applyInternal(Document doc, int start) {
			assert false : "Should never happen"
		}
		
		@Override
		protected Rule[] dependencies() {
			return [] as Rule[];
		}

	}

	public static Rule blockComment() {
		return new Regex("/\\*.*?\\*/", Pattern.DOTALL).setName("BLOCK_COMMENT")
	}

	public static Rule lineComment() {
		return new Regex("//[^\r\n]*").setName("LINE_COMMENT")
	}

	public static Rule identifier() {
		return new Regex("[a-zA-Z_][a-zA-Z0-9_]*").setName("IDENTIFIER")
	}

	public static Rule ws() {
		return new Regex("\\s+").setName("WS");
	}

	public static Rule reg(String s) {
		return new Regex(s);
	}

	public static  Rule eof() {
		return new EOF();
	}

	public static  Rule opt(Rule a) {
		return new Opt(a);
	}

	public static  Rule lit(String s) {
		return new Literal(s).setName("[$s]");
	}

	public static  Rule star(Rule app) {
		return new Star(app)
	}

	public static  Rule plus(Rule app) {
		return new Plus(app)
	}

	public static  Rule alt(Rule... app) {
		return new Alternatives(app);
	}

	public static  Sequence seq(Rule... app) {
		return new Sequence(app);
	}

	public static  Lazy lazy() {
		return new Lazy(null);
	}

	public static  Rule eps() {
		return new EPS();
	}


}
