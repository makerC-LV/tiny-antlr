package org.makerc.cfg

import groovy.transform.CompileStatic

@CompileStatic
public interface Document {
	
		/**
		 * Returns this document's text for the specified range.
		 */
		public CharSequence subseq(int start, int end);
	
//		/**
//		 * Returns this document's text starting at the given position to the end of the document.
//		 */
//		public String substring(int start);
	
		public int length();
	
		public char charAt(int index);
//	
//		public int indexOf(int ch, int fromIndex);
	
		public boolean startsWith(String prefix, int offset);
	
//		public int indexOf(String str, int fromIndex);
//		
		/**
		 * translates an offset within the document to the line/column position
		 */
		public int[] translateOffset(int offset);
		
		public CharSequence getDiagnostic(int offset, int maxLen);
	
	}
