package nl.practicom.c4w.cytoscape

import java.awt.Color
import nl.practicom.c4w.multidll.ProcedureDependencyScanner
import nl.practicom.c4w.txa.transform.StreamingTxaReader
import org.cytoscape.application.CyApplicationManager
import org.cytoscape.event.CyEventHelper
import org.cytoscape.model.CyNetwork
import org.cytoscape.model.CyNetworkFactory
import org.cytoscape.model.CyNetworkManager
import org.cytoscape.model.CyNode
import org.cytoscape.session.CyNetworkNaming
import org.cytoscape.view.model.CyNetworkView
import org.cytoscape.view.model.CyNetworkViewFactory
import org.cytoscape.view.model.CyNetworkViewManager
import org.cytoscape.view.model.View
import org.cytoscape.view.presentation.property.BasicVisualLexicon
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty
import org.cytoscape.view.vizmap.VisualMappingManager
import org.cytoscape.work.AbstractTask
import org.cytoscape.work.TaskMonitor

import java.nio.file.Paths

class CreateNetworkTask extends AbstractTask {

  private final CyNetworkManager netMgr
  private final CyNetworkFactory cnf
  private final CyNetworkNaming namingUtil
  private final CyNetworkViewFactory cnvf
  private final CyNetworkViewManager networkViewManager
  private final CyApplicationManager cam
  private final VisualMappingManager vmm
  private final CyEventHelper eh

  // Just for testing
  final txaFilePath = '/Volumes/SSD2/Workspaces/clients/Practicom/data/historie_all/historie10.txa'

  CreateNetworkTask(
    final CyNetworkManager netMgr,
    final CyNetworkNaming namingUtil,
    final CyNetworkFactory cnf,
    final CyNetworkViewFactory cnvf,
    final CyNetworkViewManager networkViewManager,
    final CyApplicationManager cam,
    final VisualMappingManager vmm,
    final CyEventHelper eh
  ){
    this.netMgr = netMgr;
    this.cnf = cnf;
    this.namingUtil = namingUtil;
    this.cnvf = cnvf
    this.networkViewManager = networkViewManager
    this.cam = cam
    this.vmm = vmm
    this.eh = eh
  }

  void run(final TaskMonitor monitor) {
    def fileName = Paths.get(txaFilePath).fileName.toString()
    monitor.setTitle("Import " + fileName);

    CyNetwork myNet = cnf.createNetwork();
    myNet.getRow(myNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(fileName));
    importTxa(myNet, new File(txaFilePath))
    netMgr.addNetwork(myNet);

    createDefaultNetworkView(myNet)
  }

  private void createDefaultNetworkView(CyNetwork net) {
    CyNetworkView cnv = cnvf.createNetworkView(net);
    networkViewManager.addNetworkView(cnv);

    // Make sure the newly added nodes get a view
    eh.flushPayloadEvents()

    def pk = net.getDefaultNodeTable().getPrimaryKey().getName()

    net.getDefaultNodeTable().getAllRows().each { row ->
      def suid = row.get(pk, Long.class)
      def node = net.getNode(suid)
      def procedureName = row.get("name", String.class)
      View<CyNode> view = cnv.getNodeView(node)
      view.setVisualProperty(BasicVisualLexicon.NODE_LABEL, procedureName)
      view.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, procedureName.size() * 10.0d)
      view.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE)
      view.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, Color.CYAN)
    }
  }

  def importTxa(CyNetwork net, File txaFile) {

    def scanner = new ProcedureDependencyScanner()
    new StreamingTxaReader()
      .withHandler(scanner)
      .parse(txaFile)

    def nodeSuids = []

    scanner.dependencies.eachWithIndex { procedureName, _, idx ->
      CyNode node = net.addNode();
      net.getDefaultNodeTable().getRow(node.getSUID()).set("name", procedureName as String);
      nodeSuids[idx] = node.getSUID()
    }


    def nv = cam.getCurrentNetworkView()

    scanner.dependencies.eachWithIndex { _, dependentProcedures, sourceIdx ->
      def sourceSUID = nodeSuids[sourceIdx]
      def sourceNode = net.getNode(sourceSUID)
      def sourceName = net.getDefaultNodeTable().getRow(sourceNode.getSUID()).get("name", String.class);

      dependentProcedures.each { dependentProcedure ->
        def targetIdx = scanner.dependencies.findIndexOf { it.key == dependentProcedure }
        def targetSUID = nodeSuids[targetIdx]
        def targetNode = net.getNode(targetSUID)
        def edge = net.addEdge(sourceNode, targetNode, true)
        net.getDefaultEdgeTable().getRow(edge.getSUID()).set("name","${sourceName}::${dependentProcedure}" as String)
      }
    }
  }

}