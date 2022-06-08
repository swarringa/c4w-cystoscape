package nl.intreq.c4w.cytoscape.io.internal.impex

import nl.intreq.c4w.cytoscape.io.internal.Constants
import nl.intreq.c4w.multidll.scanners.EntryProcedureScanner
import nl.intreq.c4w.multidll.scanners.ProcedureDependencyScanner
import nl.intreq.c4w.multidll.scanners.ProcedureInfoScanner
import org.cytoscape.model.CyNetwork
import org.cytoscape.model.CyNode
import org.cytoscape.model.CyTable

import static nl.intreq.c4w.cytoscape.io.internal.Constants.NAMESPACE
import static nl.intreq.c4w.cytoscape.io.internal.Constants.NodeType.MENU
import static nl.intreq.c4w.cytoscape.io.internal.Constants.NodeType.PROCEDURE
import static nl.intreq.c4w.cytoscape.io.internal.Constants.ProcedureTableColumn.*

final class TxaNetworkBuilder {

  def procedureNodeSuids = [:]
  def menuNodeSuids = [:]
  def menuRootSuid = null
  def mainProcedure = null
  def mainMenuProcedure = null
  def importMenu = false

  ProcedureInfoScanner procedureScanner
  ProcedureDependencyScanner dependencyScanner
  EntryProcedureScanner entryScanner

  TxaNetworkBuilder(
        ProcedureInfoScanner procedureScanner,
        ProcedureDependencyScanner dependencyScanner,
        EntryProcedureScanner entryScanner ) {
    this.procedureScanner = procedureScanner
    this.dependencyScanner = dependencyScanner
    this.entryScanner = entryScanner
  }

  def importTxa(CyNetwork net, boolean importMenu) {

    mainProcedure = procedureScanner.getMainProcedure()?.name
    mainMenuProcedure = entryScanner.procedureName

    addProcedureNodes(procedureScanner, dependencyScanner, net)

    if ( importMenu ) {
      addMenuNodes(entryScanner, net)
      connectMenuToProcedures(entryScanner, net, dependencyScanner)
    }

    addNodeAttributes(net, procedureScanner)
  }

  def addProcedureNodes(ProcedureInfoScanner procedureScanner, ProcedureDependencyScanner dependencyScanner, net) {

    dependencyScanner.dependencies.eachWithIndex { procedureName, _, idx ->
      CyNode node = net.addNode();
      procedureNodeSuids[procedureName] = node.getSUID()
    }

    // Connect procedure nodes
    dependencyScanner.dependencies.eachWithIndex { procedureName, dependentProcedures, sourceIdx ->
      // When menutree is imported the procedure containing the menu tree wll be connected to the menubar only
      if (!importMenu || procedureName != mainMenuProcedure ) {
        def sourceSUID = procedureNodeSuids[procedureName]
        def sourceNode = net.getNode(sourceSUID)
        def sourceName = net.getDefaultNodeTable().getRow(sourceNode.getSUID()).get(CyNetwork.NAME, String.class);

        dependentProcedures.each { dependentProcedure ->
          //def targetIdx = dependencyScanner.dependencies.findIndexOf { it.key == dependentProcedure }
          def targetSUID = procedureNodeSuids[dependentProcedure]
          def targetNode = net.getNode(targetSUID)
          def edge = net.addEdge(sourceNode, targetNode, true)
          net.getDefaultEdgeTable().getRow(edge.getSUID()).set(CyNetwork.NAME, "${sourceName}::${dependentProcedure}" as String)
        }
      }
    }

    // Create link from application to the main procedure
    if ( procedureScanner.getMainProcedure() ) {
      def applicationNode = net.getNode(procedureNodeSuids['APPLICATION'])
      def mainProcedureNode = net.getNode(procedureNodeSuids[procedureScanner.mainProcedure.name])
      def mainEdge = net.addEdge(applicationNode, mainProcedureNode, true)
      net.getDefaultEdgeTable().getRow(mainEdge.getSUID()).set(CyNetwork.NAME, "main_procedure" as String)
    }
  }

  def addMenuNodes(EntryProcedureScanner entryScanner, net) {
    // First add all menu parent nodes
    entryScanner.menuTree.eachWithIndex { menuEntry, _, idx ->
      CyNode menuNode = net.addNode();
      menuNodeSuids[menuEntry] = menuNode.getSUID()
      if ( entryScanner.menuRoot == menuEntry){
        menuRootSuid = menuNode.getSUID()
      }
    }

    // Link the children to their parents
    entryScanner.menuTree.eachWithIndex { menuEntry, children, _ ->
      def menuNodeSuid = menuNodeSuids[menuEntry]
      def menuNode = net.getNode(menuNodeSuid)
      if (menuNode) {
        children.each { child ->
          def childNode = null
          if (menuNodeSuids.containsKey(child)) {
            childNode = net.getNode(menuNodeSuids[child])
          } else {
            childNode = net.addNode()
            menuNodeSuids[child] = childNode.SUID
          }

          if (childNode) {
            def edge = net.addEdge(menuNode, childNode, true)
            net.getDefaultEdgeTable().getRow(edge.getSUID()).set(CyNetwork.NAME, "${menuEntry}::${child}" as String)
          }
        }
      }
    }

    //Link the procedure containing the menubar to the menubar root
    if ( mainMenuProcedure ) {
      def mainMenuProcedureNode = net.getNode(procedureNodeSuids[mainMenuProcedure])
      def menubarNode = net.getNode(menuRootSuid)
      if (mainMenuProcedureNode && menubarNode) {
        def edge = net.addEdge(mainMenuProcedureNode, menubarNode, true)
        net.getDefaultEdgeTable().getRow(edge.getSUID()).set(CyNetwork.NAME, "menubar" as String)
      }
    }
  }

  def connectMenuToProcedures(EntryProcedureScanner entryScanner, net, dependencyScanner) {
    entryScanner.menuTree.each { _, children ->
      children.each { child ->
        if (!entryScanner.menuTree.containsKey(child)) {
          def menuNode = net.getNode(menuNodeSuids[child])
          def entryProcedures = entryScanner.entryProceduresFor(child)
          entryProcedures.each { procedure ->
            def procedureNode = net.getNode(procedureNodeSuids[procedure])
            if (menuNode && procedureNode) {
              net.addEdge(menuNode, procedureNode, true)
            }
          }
        }
      }
    }
  }

  def addNodeAttributes(CyNetwork net, ProcedureInfoScanner procedureScanner){
    CyTable table = net.getDefaultNodeTable()

    table.with {
      Constants.ProcedureTableColumn.values().each { column ->
        createColumn(NAMESPACE,column.columnName, column.columnType,column.immutable)
      }
    }

    procedureScanner.procedures.each { procInfo ->
      try {
        def row = table.getRow(procedureNodeSuids[procInfo.name])
        row.set(CyNetwork.NAME, procInfo.name)
        row.set(NODETYPE.fqn, PROCEDURE.value)
        row.set(TEMPLATE.fqn, procInfo.template)
        row.set(NOEXPORT.fqn, !procInfo.isExported)
        row.set(MAIN_PROCEDURE.fqn, procInfo.isMainProcedure)
      } catch ( Exception e){
        print("Error adding row for $procInfo.name")
        print(procInfo)
      }
    }

    menuNodeSuids.each { menuName, suid ->
      def row = table.getRow(suid)
      row.set(CyNetwork.NAME, menuName)
      row.set(NODETYPE.fqn, MENU.value)
    }
  }
}
