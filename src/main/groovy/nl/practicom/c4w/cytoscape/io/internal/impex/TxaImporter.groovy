package nl.practicom.c4w.cytoscape.io.internal.impex

import nl.practicom.c4w.multidll.ProcedureDependencyScanner
import nl.practicom.c4w.txa.transform.StreamingTxaReader
import org.cytoscape.model.CyNetwork
import org.cytoscape.model.CyNode

final class TxaImporter {

  def importTxa(CyNetwork net, InputStream txaInputStream) {

    def scanner = new ProcedureDependencyScanner()

    new StreamingTxaReader()
      .withHandler(scanner)
      .parse(txaInputStream)

    def nodeSuids = []

    scanner.dependencies.eachWithIndex { procedureName, _, idx ->
      CyNode node = net.addNode();
      net.getDefaultNodeTable().getRow(node.getSUID()).set("name", procedureName as String);
      nodeSuids[idx] = node.getSUID()
    }

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
