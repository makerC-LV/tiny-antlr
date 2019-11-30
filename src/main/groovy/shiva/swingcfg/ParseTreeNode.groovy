package shiva.swingcfg

import javax.swing.tree.DefaultMutableTreeNode

import groovy.transform.TupleConstructor
import shiva.cfg.ParseNode

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
