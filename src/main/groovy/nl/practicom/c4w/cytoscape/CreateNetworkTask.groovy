package nl.practicom.c4w.cytoscape

import nl.practicom.c4w.multidll.ProcedureDependencyScanner
import nl.practicom.c4w.txa.transform.StreamingTxaReader
import org.cytoscape.model.CyNetwork
import org.cytoscape.model.CyNetworkFactory
import org.cytoscape.model.CyNetworkManager
import org.cytoscape.model.CyNode
import org.cytoscape.session.CyNetworkNaming
import org.cytoscape.view.model.CyNetworkView
import org.cytoscape.view.model.CyNetworkViewFactory
import org.cytoscape.view.model.CyNetworkViewManager
import org.cytoscape.work.AbstractTask
import org.cytoscape.work.TaskMonitor

import java.nio.file.Paths

class CreateNetworkTask extends AbstractTask {

  private final CyNetworkManager netMgr
  private final CyNetworkFactory cnf
  private final CyNetworkNaming namingUtil
  private final CyNetworkViewFactory cnvf
  private final CyNetworkViewManager networkViewManager

  // Just for testing
  final txaFilePath = '/Volumes/SSD2/Workspaces/clients/Practicom/data/historie_all/historie10.txa'

  CreateNetworkTask(
    final CyNetworkManager netMgr,
    final CyNetworkNaming namingUtil,
    final CyNetworkFactory cnf,
    final CyNetworkViewFactory cnvf,
    final CyNetworkViewManager networkViewManager){

    this.netMgr = netMgr;
    this.cnf = cnf;
    this.namingUtil = namingUtil;
    this.cnvf = cnvf
    this.networkViewManager = networkViewManager
  }

  void run(final TaskMonitor monitor) {
    def fileName = Paths.get(txaFilePath).fileName.toString()
    monitor.setTitle("Import " + fileName);

    CyNetwork myNet = cnf.createNetwork();
    myNet.getRow(myNet).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle(fileName));

    importTxa(myNet, new File(txaFilePath))
    netMgr.addNetwork(myNet);

    CyNetworkView myView = cnvf.createNetworkView(myNet);
    networkViewManager.addNetworkView(myView);
  }

  def importTxa(CyNetwork net, File txaFile) {
    def scanner = new ProcedureDependencyScanner()
    new StreamingTxaReader()
      .withHandler(scanner)
      .parse(txaFile)

    def nodeSuids = []

    scanner.dependencies.eachWithIndex { procedureName, _, idx ->
      CyNode node = net.addNode();
      net.getDefaultNodeTable().getRow(node.getSUID()).set("name", procedureName);
      nodeSuids[idx] = node.getSUID()
    }

    scanner.dependencies.eachWithIndex { _, dependentProcedures, sourceIdx ->
      def sourceSUID = nodeSuids[sourceIdx]
      def sourceNode = net.getNode(sourceSUID)

      dependentProcedures.each { dependentProcedure ->
        def targetIdx = scanner.dependencies.findIndexOf { it.key == dependentProcedure }
        def targetSUID = nodeSuids[targetIdx]
        def targetNode = net.getNode(targetSUID)
        def edge = net.addEdge(sourceNode, targetNode, true)
      }
    }
  }

}