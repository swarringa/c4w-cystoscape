package nl.practicom.c4w.cytoscape.io.internal.writer

import nl.practicom.c4w.multidll.transforms.procedure.ProcedureExtractor
import nl.practicom.c4w.multidll.transforms.procedure.ProcedureListTransformFactory
import nl.practicom.c4w.multidll.io.SingleTxaProcedureWriter
import nl.practicom.c4w.txa.transform.StreamingTxaReader
import org.cytoscape.io.write.CyWriter
import org.cytoscape.model.CyNetwork
import org.cytoscape.model.subnetwork.CySubNetwork
import org.cytoscape.work.ProvidesTitle
import org.cytoscape.work.TaskMonitor
import org.cytoscape.work.Tunable

import static nl.practicom.c4w.cytoscape.io.internal.Constants.NetworkTableColumn.SOURCETXA
import static nl.practicom.c4w.cytoscape.io.internal.Constants.NodeType.PROCEDURE
import static nl.practicom.c4w.cytoscape.io.internal.Constants.ProcedureTableColumn.NODETYPE

class TxaNetworkWriter implements CyWriter {

  OutputStream outputStream
  CyNetwork network

  @Tunable(description = "Aantal procedures per module")
  public Integer numProceduresPerModule = 20

  @ProvidesTitle
  String getTitle() { "Export network as Clarion TXA file" }

  TxaNetworkWriter(
    OutputStream os,
    CyNetwork network
  ) {
    this.outputStream = os
    this.network = network
  }

  @Override
  void run(TaskMonitor taskMonitor) throws Exception {

    // The source TXA from which the network was build should be on the root network
    def rootNetwork = this.network
    if ( this.network instanceof CySubNetwork ){
      rootNetwork = (this.network as CySubNetwork).rootNetwork
    }

    def localAttrs = rootNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS)
    def row = localAttrs.getRow(rootNetwork.SUID)
    String sourceTxa = row.get(SOURCETXA.fqn, SOURCETXA.columnType)
    taskMonitor.setTitle(sourceTxa)

    def publicProcedures =
        this.network.getDefaultNodeTable()
        .getMatchingRows(NODETYPE.fqn, PROCEDURE.value)
        .inject([]){ procs, r -> procs << r.get(CyNetwork.NAME,String.class) }

    def procedureWriter = new SingleTxaProcedureWriter(outputStream, numProceduresPerModule)

    def transformFactory = new ProcedureListTransformFactory()
    transformFactory.publicProcedures = publicProcedures

    def procedureExtractor = new ProcedureExtractor(transformFactory,procedureWriter)

    new StreamingTxaReader()
      .withHandler(procedureExtractor)
      .parse(new File(sourceTxa))
  }

  @Override
  void cancel() {

  }
}
