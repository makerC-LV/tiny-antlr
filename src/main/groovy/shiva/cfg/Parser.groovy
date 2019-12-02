package shiva.cfg

interface Parser {
	
	ParseNode parse(Rule r, Document doc, int offset) throws ParseException
	
	List<Rule> getRules()
	
	Rule getRule(String name)
	
}
