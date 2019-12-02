package org.makerc.tinyantlr

import org.makerc.cfg.Document
import org.makerc.cfg.ParseException
import org.makerc.cfg.ParseNode
import org.makerc.cfg.Parser
import org.makerc.cfg.Rule

class GeneratedParser implements Parser {

	List<Rule> rules
	Map<String, Rule> namedRules
	
	public GeneratedParser(List<Rule> rules, Map<String, Rule> namedRules) {
		super();
		this.rules = rules;
		this.namedRules = namedRules;
	}

	@Override
	public ParseNode parse(Rule r, Document doc, int offset) throws ParseException {
		return r.apply(doc, offset)
	}

	@Override
	public List<Rule> getRules() {
		return rules;
	}

	@Override
	public Rule getRule(String name) {
		return namedRules[name];
	}
}
