package shiva.cfg

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
public class ParseNode {
	
	Rule rule;
	boolean matched;
	int start;
	int matchLength = -1;
	List<ParseNode> children = [];
	
	public ErrorInfo errorInfo(Document d) {
		if (matched) {
			return null;
		}
		if (children.isEmpty()) {
			int[] lo = d.translateOffset(start);
			return new ErrorInfo(rule, rule.getDescription(), start, lo[0], lo[1])
		} else {
			ParseNode c = children.find({ !it.matched} )
			assert c != null : "$rule failed, but has no failing children"
			return c.errorInfo(d)
		}
	}
	
	@Override
	public String toString() {
		return "ParseNode [rule=" + rule.getDescription() + ", matched=" + matched + ", start=" + start + ", matchLength=" + matchLength
				 "]";
	}

	@TupleConstructor
	public static class ErrorInfo {
		Rule rule
		String expect
		int docOffset
		int line;
		int lineOffset;
	}
	
}