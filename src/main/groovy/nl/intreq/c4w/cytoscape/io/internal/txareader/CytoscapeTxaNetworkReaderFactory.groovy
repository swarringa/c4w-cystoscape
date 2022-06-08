package nl.intreq.c4w.cytoscape.io.internal.txareader

import org.cytoscape.application.CyApplicationManager
import org.cytoscape.group.CyGroupFactory
import org.cytoscape.io.CyFileFilter
import org.cytoscape.io.read.AbstractInputStreamTaskFactory
import org.cytoscape.model.CyNetworkFactory
import org.cytoscape.model.CyNetworkManager
import org.cytoscape.model.subnetwork.CyRootNetworkManager
import org.cytoscape.view.layout.CyLayoutAlgorithmManager
import org.cytoscape.view.model.CyNetworkViewFactory
import org.cytoscape.view.presentation.RenderingEngineManager
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory
import org.cytoscape.view.vizmap.VisualMappingManager
import org.cytoscape.view.vizmap.VisualStyleFactory
import org.cytoscape.work.TaskIterator

class CytoscapeTxaNetworkReaderFactory extends AbstractInputStreamTaskFactory {

  private  CyApplicationManager _application_manager
  protected  CyNetworkFactory _network_factory
  private  CyNetworkManager _network_manager
  private  CyRootNetworkManager _root_network_manager
  private  VisualMappingManager _visual_mapping_manager
  private  RenderingEngineManager _rendering_engine_manager
  private  CyNetworkViewFactory _networkview_factory
  private  CyGroupFactory _group_factory
  private  VisualStyleFactory _visual_style_factory
  private  VisualMappingFunctionFactory _vmf_factory_c
  private  VisualMappingFunctionFactory _vmf_factory_d
  private  VisualMappingFunctionFactory _vmf_factory_p
  private  CyLayoutAlgorithmManager layoutManager

   CytoscapeTxaNetworkReaderFactory(
          final CyFileFilter filter,
          final CyApplicationManager application_manager,
          final CyNetworkFactory network_factory,
          final CyNetworkManager network_manager,
          final CyRootNetworkManager root_network_manager,
          final VisualMappingManager visual_mapping_manager,
          final VisualStyleFactory visual_style_factory,
          final CyGroupFactory group_factory,
          final RenderingEngineManager rendering_engine_manager,
          final CyNetworkViewFactory networkview_factory,
          final VisualMappingFunctionFactory vmf_factory_c,
          final VisualMappingFunctionFactory vmf_factory_d,
          final VisualMappingFunctionFactory vmf_factory_p,
          final CyLayoutAlgorithmManager layoutManager) {

    super(filter)
    _application_manager = application_manager
    _network_factory = network_factory
    _network_manager = network_manager
    _root_network_manager = root_network_manager
    _visual_mapping_manager = visual_mapping_manager
    _visual_style_factory = visual_style_factory
    _group_factory = group_factory
    _rendering_engine_manager = rendering_engine_manager
    _networkview_factory = networkview_factory
    _vmf_factory_c = vmf_factory_c
    _vmf_factory_d = vmf_factory_d
    _vmf_factory_p = vmf_factory_p
    this.layoutManager = layoutManager
  }


  @Override
  TaskIterator createTaskIterator(InputStream is, String inputName) {
    try {
      return new TaskIterator(
        new CytoscapeTxaNetworkReader(
                inputName,
                is,
                _application_manager,
                _network_factory,
                _network_manager,
                _root_network_manager,
                _group_factory,
                _visual_mapping_manager,
                _visual_style_factory,
                _rendering_engine_manager,
                _networkview_factory,
                _vmf_factory_c,
                _vmf_factory_d,
                _vmf_factory_p,
                layoutManager
        )
      )
    }
    catch (final IOException e) {

      e.printStackTrace()
      return null
    }
  }
}
