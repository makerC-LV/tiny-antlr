package org.makerc.swingcfg

import javax.swing.tree.DefaultMutableTreeNode

import org.makerc.cfg.ParseNode

import groovy.transform.TupleConstructor

@TupleConstructor
class ParseTreeNode extends DefaultMutableTreeNode {
	
//	private ParseNode node
	private ParseTreeNode(ParseNode node) {
		setUserObject(node);
	}
	
	public static ParseTreeNode construct(ParseNode node) {
		ParseTreeNode tnode = new ParseTreeNode(node)
		node.children.each { n -> tnode.add(construct(n)) }
		return tnode;
	}
}
