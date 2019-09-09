package nl.practicom.c4w.cytoscape.view.internal


import org.cytoscape.application.swing.CyMenuItem
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory
import org.cytoscape.model.CyNode
import org.cytoscape.view.model.CyNetworkView
import org.cytoscape.view.model.View

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class TxaNodeViewContextMenuFactory implements CyNodeViewContextMenuFactory, ActionListener {
  @Override
  CyMenuItem createMenuItem(CyNetworkView netView, View<CyNode> nodeView) {
    JMenuItem menuItem = new JMenuItem("Select procedure tree")
    menuItem.addActionListener(this)
    CyMenuItem cyMenuItem = new CyMenuItem(menuItem, 0)
    return cyMenuItem
  }

  void actionPerformed(ActionEvent e) {
    // Write your own function here.
    JOptionPane.showMessageDialog(null, "Select procedure tree action worked.")
  }
}
