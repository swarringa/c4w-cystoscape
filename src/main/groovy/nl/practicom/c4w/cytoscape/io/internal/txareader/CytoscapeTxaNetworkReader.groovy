package nl.practicom.c4w.cytoscape.io.internal.txareader


import nl.practicom.c4w.cytoscape.io.internal.impex.TxaNetworkBuilder
import nl.practicom.c4w.multidll.EntryProcedureScanner
import nl.practicom.c4w.multidll.ProcedureDependencyScanner
import nl.practicom.c4w.multidll.ProcedureInfoScanner
import nl.practicom.c4w.txa.transform.StreamingTxaReader
import org.cytoscape.application.CyApplicationManager
import org.cytoscape.group.CyGroupFactory
import org.cytoscape.io.read.AbstractCyNetworkReader
import org.cytoscape.model.CyNetwork
import org.cytoscape.model.CyNetworkFactory
import org.cytoscape.model.CyNetworkManager
import org.cytoscape.model.CyNode
import org.cytoscape.model.subnetwork.CyRootNetworkManager
import org.cytoscape.model.subnetwork.CySubNetwork
import org.cytoscape.view.layout.CyLayoutAlgorithmManager
import org.cytoscape.view.model.CyNetworkView
import org.cytoscape.view.model.CyNetworkViewFactory
import org.cytoscape.view.model.View
import org.cytoscape.view.presentation.RenderingEngineManager
import org.cytoscape.view.presentation.property.BasicVisualLexicon
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory
import org.cytoscape.view.vizmap.VisualMappingManager
import org.cytoscape.view.vizmap.VisualStyleFactory
import org.cytoscape.work.ProvidesTitle
import org.cytoscape.work.TaskMonitor
import org.cytoscape.work.Tunable

import java.awt.*
import java.util.List

import static nl.practicom.c4w.cytoscape.io.internal.Constants.NAMESPACE
import static nl.practicom.c4w.cytoscape.io.internal.Constants.NetworkTableColumn.SOURCETXA

class CytoscapeTxaNetworkReader extends AbstractCyNetworkReader {

  private final CyApplicationManager         _application_manager
  private final List<CyNetwork>              _networks
  private String                             _network_collection_name
  private final InputStream                  _in
  private final VisualMappingManager         _visual_mapping_manager
  private final RenderingEngineManager       _rendering_engine_manager
  private final CyNetworkViewFactory         _networkview_factory
  private final VisualStyleFactory           _visual_style_factory
  private final VisualMappingFunctionFactory _vmf_factory_c
  private final VisualMappingFunctionFactory _vmf_factory_d
  private final VisualMappingFunctionFactory _vmf_factory_p
  private final CyGroupFactory               _group_factory
  private final CyLayoutAlgorithmManager    layoutManager
  private TaskMonitor                       parentTaskMonitor

  private final String txaFilePath

  @Tunable(description = "Import menu")
  public Boolean importMenu = false

  @ProvidesTitle
  String getTitle() { "Import Clarion TXA file as network" }

  CytoscapeTxaNetworkReader(
          final String network_collection_name,
          final InputStream input_stream,
          final CyApplicationManager application_manager,
          final CyNetworkFactory network_factory,
          final CyNetworkManager network_manager,
          final CyRootNetworkManager root_network_manager,
          final CyGroupFactory group_factory,
          final VisualMappingManager visual_mapping_manager,
          final VisualStyleFactory visual_style_factory,
          final RenderingEngineManager rendering_engine_manager,
          final CyNetworkViewFactory networkview_factory,
          final VisualMappingFunctionFactory vmf_factory_c,
          final VisualMappingFunctionFactory vmf_factory_d,
          final VisualMappingFunctionFactory vmf_factory_p,
          final CyLayoutAlgorithmManager layoutManager) throws IOException {

    super(input_stream, networkview_factory, network_factory, network_manager, root_network_manager)

    if (inputStream == null) {
      throw new IllegalArgumentException("input stream must not be null")
    }
    _in = inputStream
    this.txaFilePath = (_in.in as FileInputStream).path

    _network_collection_name = network_collection_name
    _visual_mapping_manager = visual_mapping_manager
    _rendering_engine_manager = rendering_engine_manager
    _networkview_factory = networkview_factory
    _group_factory = group_factory
    _networks = new ArrayList<CyNetwork>()
    _visual_style_factory = visual_style_factory
    _vmf_factory_c = vmf_factory_c
    _vmf_factory_d = vmf_factory_d
    _vmf_factory_p = vmf_factory_p
    this.layoutManager = layoutManager
  }

  @Override
  CyNetworkView buildCyNetworkView(CyNetwork net) {
    CyNetworkView cnv = _networkview_factory.createNetworkView(net)

    def pk = net.getDefaultNodeTable().getPrimaryKey().getName()

    net.getDefaultNodeTable().getAllRows().each { row ->
      def suid = row.get(pk, Long.class)
      def node = net.getNode(suid)
      def nodeName = row.get("name", String.class) ?: "?"
      View<CyNode> view = cnv.getNodeView(node)
      view.setLockedValue(BasicVisualLexicon.NODE_LABEL, nodeName)
      view.setLockedValue(BasicVisualLexicon.NODE_WIDTH, nodeName.size() * 10.0d)
      view.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE)
      view.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.CYAN)
    }

    return cnv
  }

  @Override
  void run(TaskMonitor taskMonitor) throws Exception {
    taskMonitor.setTitle("Import Clarion TXA")
    taskMonitor.setStatusMessage("Importing ${txaFilePath} as network")

    def procedureScanner = new ProcedureInfoScanner()
    def dependencyScanner = new ProcedureDependencyScanner()
    def entryScanner = new EntryProcedureScanner()

    new StreamingTxaReader()
      .withHandler(procedureScanner)
      .withHandler(dependencyScanner)
      .withHandler(entryScanner)
      .parse(_in)

    CyNetwork net = super.cyNetworkFactory.createNetwork()
    net.getRow(net)?.set(CyNetwork.NAME, _network_collection_name)
    new TxaNetworkBuilder(procedureScanner,dependencyScanner,entryScanner).importTxa(net, importMenu)

    // We need to attach the source txa to the network so we can retrieve it when generating target txa's
    def rootNetwork = (net as CySubNetwork).rootNetwork
    def localAttrs = rootNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS)
    localAttrs.createColumn(NAMESPACE, SOURCETXA.columnName,SOURCETXA.columnType,SOURCETXA.immutable)
    localAttrs.getRow(rootNetwork.SUID).set(SOURCETXA.fqn,this.txaFilePath)

    _networks.push(net)
  }

  @Override
  CyNetwork[] getNetworks() {
    final CyNetwork[] results = new CyNetwork[_networks.size()]
    for (int i = 0; i < results.length; ++i) {
      results[i] = _networks.get(i)
    }
    return results
  }

}
