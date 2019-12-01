package shiva.antlr

import shiva.cfg.ParseNode
import shiva.cfg.Rule
import shiva.cfg.Document
import static shiva.cfg.Rules.*

import groovy.transform.CompileStatic

@CompileStatic
class GrammarGenerator {

	public static boolean DEBUG
	SmallAntlrGrammar grammar


	public GrammarGenerator(SmallAntlrGrammar grammar) {
		super();
		this.grammar = grammar;
	}

	public Map<String, Rule> generate(ParseNode pn, Document doc) {
		Map<String, ParseNode> ruleDefs = [:]
		findRuleDefs(pn, doc, ruleDefs)
		// Create priority q of dependencies
		PriorityQueue<Dependencies> pq = new PriorityQueue()
		ruleDefs.each { s, n ->  
			Dependencies d = new Dependencies(s)
			fillDependencies(n, doc, d)
			pq.add(d)
		}

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
				generated[s] = lazy().setName(s)
				if (DEBUG) {
					println "Creating lazy rule $s"
				}
			 }
			// Define the rule for ident
			Rule defn = defineRule(ruleDefs[ident], doc, generated)
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
			Dependencies[] deps = []
			deps += pq
			pq.clear()
			deps.each { dep ->
				dep.removeAll(d)
				dep.remove(ident)
				pq.add(dep)
			}
			
		}
		return generated

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
			r = r.rule
		}
		if (r == grammar.Alt) {
			return defineAlt(pn, doc, rules)
		}
		Rule[] childRules = []
		pn.children.each { cn ->
			Rule cr = defineRule(cn, doc, rules)
			if (cr != null) {
				childRules += cr
			}
		}
		switch (r) {

			case grammar.Seq:
				assert childRules.size() >= 1 : "${r.getDescription()}  $pn  $childRules"
				return seq(childRules)
			case grammar.Star:
				assert childRules.size() == 1 : "${r.getDescription()}  $pn"
				return star(childRules[0])
			case grammar.Plus:
				assert childRules.size() == 1 : "${r.getDescription()}  $pn"
				return plus(childRules[0])
			case grammar.Opt:
				assert childRules.size() == 1 : "${r.getDescription()}  $pn"
				return opt(childRules[0])
			case grammar.Literal:
				String s = doc.subseq(pn.start+1, pn.start + pn.matchLength-1)
				return lit(s)
			case grammar.Regex:
				String s = doc.subseq(pn.start+1, pn.start + pn.matchLength-1)
				return reg(s)
			case grammar.Ident:
				String s = doc.subseq(pn.start, pn.start + pn.matchLength)
				assert rules[s] != null : " Expecting a predefined rule for $s"
				return rules[s]
			default:
				assert childRules.size() <= 1 : "${r.getDescription()}  $pn"
				return childRules.length == 0 ? null : childRules[0]
		}
		return null
	}

	Rule defineAlt(ParseNode pn, Document doc, Map<String, Rule> rules) {
		assert pn.children.size() == 2 : "Alt must not have ${pn.children.size()} children"
		Rule[] childRules = []
		childRules += defineRule(pn.children[0], doc, rules) as Rule
		ParseNode plusChild = pn.children[1]
		plusChild.children.each { cn ->
			Rule cr = defineRule(cn, doc, rules) as Rule
			if (cr != null) {
				childRules += cr
			}
		}
		return alt(childRules)

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
