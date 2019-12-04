package org.makerc.swingcfg

import javax.swing.tree.DefaultMutableTreeNode

import org.makerc.cfg.Document
import org.makerc.cfg.ParseNode

import groovy.transform.TupleConstructor

@TupleConstructor
class ParseTreeNode extends DefaultMutableTreeNode {
	
//	private ParseNode node
	private ParseTreeNode(ParseNode node) {
		setUserObject(node);
	}
	
	public static ParseTreeNode construct(ParseNode node, Document doc) {
		ParseTreeNode tnode = new ParseTreeNode(node)
		node.children.each { n -> tnode.add(construct(n, doc)) }
		node.markup(doc)
		return tnode;
	}
}
