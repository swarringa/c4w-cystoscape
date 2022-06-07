package nl.practicom.c4w.cytoscape.view.internal

import org.cytoscape.application.swing.CyMenuItem
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory
import org.cytoscape.model.CyNetwork
import org.cytoscape.model.CyNode
import org.cytoscape.view.model.CyNetworkView
import org.cytoscape.view.model.View

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

/**
 * Provides Select > Subtree context menu item for nodes which
 * highlights all descendant nodes reachable from the selected
 * node
 */
class SelectSubtreeViewContextMenuFactory implements CyNodeViewContextMenuFactory, ActionListener {

  private CyNetworkView netView
  private View<CyNode> nodeView

  @Override
  CyMenuItem createMenuItem(CyNetworkView netView, View<CyNode> nodeView) {
      this.netView = netView
      this.nodeView = nodeView
      JMenuItem menuItem = new JMenuItem("Descendants")
      menuItem.addActionListener(this)
      CyMenuItem cyMenuItem = new CyMenuItem(menuItem, 0)
      return cyMenuItem
  }

  void actionPerformed(ActionEvent e) {

    def net = this.netView.model
    def selectedNode = nodeView.model

    def descendantNodes = descendants([] as Set,net,selectedNode)
    def descendantRows = descendantNodes.collect {node ->  net.defaultNodeTable.getRow(node.SUID)}
    descendantRows.each { row -> row.set(CyNetwork.SELECTED, true) }
  }

  Set<CyNode> descendants(Set<CyNode> visitedNodes, CyNetwork net, CyNode node){
    def children = childNodes(net,node)
    // children - visitedNodes should eventually become [] and prevent non-termination due to cycles
    visitedNodes + children + (children - visitedNodes).collect {
      descendants(visitedNodes+children as Set, net, it)
    }.flatten() as Set<CyNode>
  }

  Set<CyNode> childNodes(CyNetwork net, CyNode node) {
    net.edgeList
      .findAll { e1 -> e1.source.SUID == node.SUID }
      .collect { e2 -> net.getNode(e2.target.SUID) }
  }
}
