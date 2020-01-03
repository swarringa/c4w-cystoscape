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
 * Provides Select > Ancestors context menu item for nodes which
 * highlights all nodes from which the selected node can be reached
 */
class SelectAncestorTreeViewContextMenuFactory implements CyNodeViewContextMenuFactory, ActionListener {

  private CyNetworkView netView
  private View<CyNode> nodeView

  @Override
  CyMenuItem createMenuItem(CyNetworkView netView, View<CyNode> nodeView) {
      this.netView = netView
      this.nodeView = nodeView
      JMenuItem menuItem = new JMenuItem("Ancestors")
      menuItem.addActionListener(this)
      CyMenuItem cyMenuItem = new CyMenuItem(menuItem, 0)
      return cyMenuItem
  }

  void actionPerformed(ActionEvent e) {

    def net = this.netView.model
    def selectedNode = nodeView.model

    def ancestorNodes = ancestors([] as Set,net,selectedNode)
    def ancestorRows = ancestorNodes.collect {node ->  net.defaultNodeTable.getRow(node.SUID)}
    ancestorRows.each { row -> row.set(CyNetwork.SELECTED, true) }
  }

  Set<CyNode> ancestors(Set<CyNode> visitedNodes, CyNetwork net, CyNode node){
    def parents = parentNodes(net,node)
    // children - visitedNodes should eventually become [] and prevent non-termination due to cycles
    visitedNodes + parents + (parents - visitedNodes).collect {
      ancestors(visitedNodes+parents as Set, net, it)
    }.flatten() as Set<CyNode>
  }

  Set<CyNode> parentNodes(CyNetwork net, CyNode node) {
    net.edgeList
      .findAll { e1 -> e1.target.SUID == node.SUID }
      .collect { e2 -> net.getNode(e2.source.SUID) }
  }
}
