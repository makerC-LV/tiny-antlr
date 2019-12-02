package org.makerc.swingcfg

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Graphics

import javax.swing.Icon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel

import org.makerc.cfg.ParseNode

class ParseTreeView {
	
	static void show(ParseTreeNode root) {
		
		JFrame fr = new JFrame("Parse tree")
		fr.setLayout(new BorderLayout())
		JTree tree = new JTree(new DefaultTreeModel(root))
		tree.setCellRenderer(new ParseTreeRenderer())
		fr.add(new JScrollPane(tree), BorderLayout.CENTER)
		fr.setBounds(100, 100, 300, 500)
		fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
		SwingUtilities.invokeLater({ -> fr.pack(); fr.setVisible(true)})
	}
	
	static class ParseTreeRenderer extends DefaultTreeCellRenderer {
		
		Icon errorOpen
		Icon errorClosed
		Icon errorLeaf
		Icon open
		Icon closed
		Icon leafIcon
		
		ParseTreeRenderer() {
			open = super.getOpenIcon()
			closed = super.getClosedIcon()
			leafIcon = super.getDefaultLeafIcon()
			errorOpen = create(open)
			errorClosed = create(closed)
			errorLeaf = create(leafIcon)
		}
		
		
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			JLabel lab = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			boolean hasError = hasError(value)
			Icon icon = leaf ? (hasError ? errorLeaf : leafIcon) :
				(expanded ? (hasError ? errorOpen : open) :
					(hasError ? errorClosed : closed))
			lab.setIcon(icon)		
			return lab
		}

		private boolean hasError(ParseTreeNode ptn) {
			ParseNode pn = ptn.getUserObject()
			return !pn.matched
		}


		private Icon create(Icon base) {
			if (base == null) {
				throw new RuntimeException("No icon")
			}
			Icon icon = new Icon() {

				@Override
				public void paintIcon(Component c, Graphics g, int x, int y) {
					base.paintIcon(c, g, x, y);
					int w = getIconWidth()
					int h = getIconHeight()
					Color col = g.getColor()
					g.setColor(Color.red)
					g.fillRect(w-5, h-5, 5, 5)
					g.setColor(col)

					
				}

				@Override
				public int getIconWidth() {
					return base.getIconWidth();
				}

				@Override
				public int getIconHeight() {
					return base.getIconHeight();
				}
				
			}
		

			return icon
		}
	}
	
}
