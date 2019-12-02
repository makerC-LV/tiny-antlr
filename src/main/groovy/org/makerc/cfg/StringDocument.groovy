package org.makerc.cfg

import java.nio.CharBuffer

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class StringDocument implements Document{

	StringBuilder buf = new StringBuilder();

	public StringDocument(String s) {
		buf.append(s);
	}

	@Override
	public CharSequence subseq(int start, int end) {
		return buf.subSequence(start, end);
	}

	@Override
	public int length() {
		return buf.length();
	}

	@Override
	public boolean startsWith(String prefix, int offset) {
		if (buf.length() < offset + prefix.length()) {
			return false
		}
		for (int i = 0; i < prefix.length(); i++) {
			if (prefix.charAt(i) != buf.charAt(offset + i)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public char charAt(int index) {
		return buf.charAt(index);
	}

	@Override
	public int[] translateOffset(int offset) {
		int line, col
		int lastLine
		for (int i = 0; i < offset; i++) {
			if (buf.charAt(i) == '\n') {
				line++
				lastLine = i;
			}
		}
		col = offset - lastLine
		return [line, col] as int[]
	}

	@Override
	public CharSequence getDiagnostic(int offset, int maxLen) {
		int remaining = buf.length() - offset;
		if (remaining <= 0) {
			return "<EOF>"
		}
		return (buf.subSequence(offset, offset + Math.min(maxLen, remaining)))
	}

	static void main(String[] args) {
		new StringDocument("a");
	}

	
}
