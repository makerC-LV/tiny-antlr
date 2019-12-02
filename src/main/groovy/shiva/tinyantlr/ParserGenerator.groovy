package shiva.tinyantlr

import shiva.cfg.ParseNode
import shiva.cfg.Rule
import shiva.cfg.Document
import static shiva.cfg.Rules.*

import groovy.transform.CompileStatic

@CompileStatic
class ParserGenerator {

	public static boolean DEBUG 
	TinyAntlr grammar


	public ParserGenerator(TinyAntlr grammar) {
		super();
		this.grammar = grammar;
	}

	public GeneratedParser generate(ParseNode pn, Document grammarDoc) throws UndefinedSymbolException {
		Map<String, ParseNode> ruleDefs = new LinkedHashMap<>()
		findRuleDefs(pn, grammarDoc, ruleDefs)
		// Create priority q of dependencies
		PriorityQueue<Dependencies> pq = new PriorityQueue()
		ruleDefs.each { s, n ->  
			Dependencies d = new Dependencies(s)
			fillDependencies(n, grammarDoc, d)
			pq.add(d)
		}
		
		checkForUndefinedSymbols(pq, ruleDefs.keySet())
		
		//Process rules in order of increasing dependency size
		Map<String, Rule> generated =[:]
		while (!pq.isEmpty()) {
			Dependencies d = pq.remove()
			String ident = d.name
			if (DEBUG) {
				println "Processing rule $ident:  deps: $d"
			}
			// Create a lazy rule for each dependency of ident
			d.each { s -> 
				if (s.equals('EOF')) {
					generated[s] = eof()
				} else {
					generated[s] = lazy().setName(s)
					if (DEBUG) {
						println "Creating lazy rule $s"
					}
				}
			 }
			// Define the rule for ident
			Rule defn = defineRule(ruleDefs[ident], grammarDoc, generated)
			Rule predef = generated[ident]
			if (predef != null) {
				assert predef instanceof Lazy : "Predefined rule $ident must be a Lazy, not ${predef.class.name}"
				Lazy l = predef as Lazy
				l.setRule(defn)
				defn.setName(l.name)
				if (DEBUG) {
					println "Setting lazy rule [$ident]'s definition"
				}
			} else {
				generated[ident] = defn
				defn.setName(ident)
				if (DEBUG) {
					println "Defining new rule $ident"
				}
			}
			// Update the priority queue, removing all the dependencies for
			// which rules have been defined (lazy or otherwise )
			List<Dependencies> deps = []
			deps.addAll(pq)
			pq.clear()
			deps.each { dep ->
				dep.removeAll(d)
				dep.remove(ident)
				pq.add(dep)
			}
			
		}
		
		List<Rule> orderedRules = []
		ruleDefs.keySet().each { name -> orderedRules << generated[name]  }
		return new GeneratedParser(orderedRules, generated)

	}
	
	private void checkForUndefinedSymbols(PriorityQueue<Dependencies> pq, Set<String> symbols) 
	throws UndefinedSymbolException {
		Set<String> allSymbols = new HashSet<String>()
		pq.each { d -> allSymbols.addAll(d) }
		StringBuilder sb = new StringBuilder()
		allSymbols.each { s -> if (!symbols.contains(s)) {sb.append(s); sb.append(" ") }}
		if (sb.length() > 0) {
			throw new UndefinedSymbolException("Undefined symbols: " + sb.toString())
		}
	}
	
	
	private void fillDependencies(ParseNode pn, Document doc, Dependencies d) {
		if (!pn.matched) {
			return
		}
		if (pn.rule == grammar.Ident) {
			String s = doc.subseq(pn.start, pn.start + pn.matchLength)
			d.add(s)
		} else {
			pn.children.each { cn -> fillDependencies(cn, doc, d) }
		}
	}


	private void findRuleDefs(ParseNode pn, Document doc, Map<String, ParseNode> ruleDefs) {
		if (!pn.matched) {
			println "Failed $pn"
			return
		}
		if (pn.rule == grammar.RuleDef) {
			ParseNode nameNode = pn.children[0];
			ParseNode defNode = pn.children[2];
			String name = doc.subseq(nameNode.start, nameNode.start + nameNode.matchLength)
			if (DEBUG) {
				println "Rule def for $name: $pn"
			}
			ruleDefs[name] = defNode
			return
		} else {
			pn.children.each { c -> findRuleDefs(c, doc, ruleDefs) }
		}
	}



	Rule defineRule(ParseNode pn, Document doc, Map<String, Rule> rules) {
		Rule r = pn.rule
		if (r instanceof Lazy) {
			Lazy l = r as Lazy
			r = l.getRule()
		}
		if (r == grammar.Unit) {
			return defineUnit(pn, doc, rules)
		}
		switch (r) {

			case grammar.Seq:
				List<Rule> childRules = []
				collectUnitRules(pn, doc, rules, childRules)
				return childRules.size() == 1 ? childRules[0] : seq(childRules as Rule[])
			
			case grammar.Alt:
				List<Rule> childRules = []
				collectUnitRules(pn, doc, rules, childRules)
				return childRules.size() == 1 ? childRules[0] : alt(childRules as Rule[])			
				
			case grammar.Literal:
				String s = doc.subseq(pn.start+1, pn.start + pn.matchLength-1)
				return lit(s)
			case grammar.Regex:
				String s = doc.subseq(pn.start+1, pn.start + pn.matchLength-1)
				return reg(s)
			case grammar.Eof:
				return eof()
			case grammar.Ident:
				String s = doc.subseq(pn.start, pn.start + pn.matchLength)
				assert rules[s] != null : " Expecting a predefined rule for $s"
				return rules[s]
			default:
				List<Rule> childRules = []
				pn.children.each { cn ->
					Rule cr = defineRule(cn, doc, rules)
					if (cr != null) {
						childRules << cr
					}
				}
				assert childRules.size() <= 1 : "${r.getDescription()}  $pn"
				return childRules.size() == 0 ? null : childRules[0]
		}
		return null
	}

	
	void collectUnitRules(ParseNode pn, Document doc, Map<String, Rule> rules, List<Rule> result) {
		if (pn.rule != grammar.Unit) {
			pn.children.each { cn -> collectUnitRules(cn, doc, rules, result) }
		} else { // Unit
			result << defineUnit(pn, doc, rules)
		}
	}
	
	
	Rule defineUnit(ParseNode pn, Document doc, Map<String, Rule> rules) {
		assert pn.children.size() == 2 : "Unit must have 2 children, not ${pn.children.size()} children"
		Rule operandRule = defineRule(pn.children[0], doc, rules)
		ParseNode opNode = pn.children[1]
		if (opNode.matchLength == 0) {
			return operandRule
		}
		opNode = opNode.children[0]
		assert opNode.rule == grammar.PostfixOp : "Expected PostfixOp rule: $opNode"
		switch (opNode.children[0].rule) {
			case grammar.LitStar:
				return star(operandRule)
			case grammar.LitPlus:
				return plus(operandRule)
			case grammar.Qmark:
				return opt(operandRule)
			default:
				assert false : "PostfixOp matched with an unknown rule: $opNode"

		}
	
	}
	
	private class Dependencies extends HashSet<String> implements Comparable<Dependencies> {
		
		String name
		
		public Dependencies(String name) {
			super();
			this.name = name;
		}


		@Override
		public int compareTo(Dependencies o) {
			return size() - o.size();
		}
		
		
	}
}
