package org.makerc.cfg

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
public class ParseNode {
	
	Rule rule;
	boolean matched;
	int start;
	int matchLength = -1;
	String diagString
	List<ParseNode> children = [];
	
	
	
	public ParseNode(Rule rule, boolean matched, int start, int matchLength = -1) {
		super();
		this.rule = rule;
		this.matched = matched;
		this.start = start;
		this.matchLength = matchLength;
	}

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
	
	void markup(Document doc) {
		if (matched) {
			int end = start + matchLength
			if (matchLength < 80) {
				diagString = '[' + doc.subseq(start, end) + ']'
			} else {
				diagString = '[' + doc.subseq(start, start + 40) + ' ... ' +
				doc.subseq(end - 40, end) + ']'
			}
		} else {
			int end = Math.min(start + 40, doc.length())
			diagString = '[' + doc.subseq(start, end) + ' ...'
		}
		children.each { c -> c.markup(doc) }
	}
	
	@Override
	public String toString() {
		String prefix = "ParseNode Rule: ${rule.getDescription()} "
		return matched ? "$prefix matched [$start - ${start + matchLength} $diagString]" : "$prefix failed $start $diagString"
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