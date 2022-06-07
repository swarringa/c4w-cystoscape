package nl.practicom.c4w.cytoscape

import nl.practicom.c4w.cytoscape.io.internal.txareader.CytoscapeTxaFileFilter
import nl.practicom.c4w.cytoscape.io.internal.txareader.CytoscapeTxaNetworkReaderFactory
import nl.practicom.c4w.cytoscape.io.internal.writer.TxaNetworkWriterFactory
import nl.practicom.c4w.cytoscape.view.internal.SelectAncestorTreeViewContextMenuFactory
import nl.practicom.c4w.cytoscape.view.internal.SelectSubtreeViewContextMenuFactory
import org.cytoscape.application.CyApplicationManager
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory
import org.cytoscape.group.CyGroupFactory
import org.cytoscape.io.DataCategory
import org.cytoscape.io.read.InputStreamTaskFactory
import org.cytoscape.io.util.StreamUtil
import org.cytoscape.model.CyNetworkFactory
import org.cytoscape.model.CyNetworkManager
import org.cytoscape.model.subnetwork.CyRootNetworkManager
import org.cytoscape.service.util.AbstractCyActivator
import org.cytoscape.view.layout.CyLayoutAlgorithmManager
import org.cytoscape.view.model.CyNetworkViewFactory
import org.cytoscape.view.presentation.RenderingEngineManager
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory
import org.cytoscape.view.vizmap.VisualMappingManager
import org.cytoscape.view.vizmap.VisualStyleFactory
import org.osgi.framework.BundleContext

import static org.cytoscape.work.ServiceProperties.ID

class CyActivator extends AbstractCyActivator {
    CyActivator() {
        super()
    }

    void start(BundleContext bc) {
        /** Imported services */

        final CyApplicationManager applicationManager = getService(bc,CyApplicationManager.class)
        final CyRootNetworkManager rootNetworkManager = getService(bc, CyRootNetworkManager.class)
        final CyNetworkManager networkManager = getService(bc,CyNetworkManager.class)
        final CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class)
        final CyNetworkViewFactory networkViewFactory = getService(bc,CyNetworkViewFactory.class)
        final VisualMappingManager visualMappingManager = getService(bc,VisualMappingManager.class)
        final RenderingEngineManager renderingEngineManager = getService(bc, RenderingEngineManager.class)
        final VisualStyleFactory visualStyleFactory = getService(bc, VisualStyleFactory.class)
        final CyGroupFactory groupFactory = getService(bc, CyGroupFactory.class)
        final CyLayoutAlgorithmManager layoutManager = getService(bc, CyLayoutAlgorithmManager.class)

        final VisualMappingFunctionFactory vmfFactoryC =
            getService(bc, VisualMappingFunctionFactory.class,"(mapping.type=continuous)")

        final VisualMappingFunctionFactory vmfFactoryD =
            getService(bc, VisualMappingFunctionFactory.class,"(mapping.type=discrete)")

        final VisualMappingFunctionFactory vmfFactoryP =
            getService(bc, VisualMappingFunctionFactory.class,"(mapping.type=passthrough)")

        final StreamUtil streamUtil = getService(bc, StreamUtil.class)

        /** Provided services */

        final CytoscapeTxaFileFilter txaFilter = new CytoscapeTxaFileFilter(
          ['txa','TXA'] as Set<String>,
          ['text/plain'] as Set<String>,
          "Clarion TXA file",
          DataCategory.NETWORK,
          streamUtil
        )

        final CytoscapeTxaNetworkReaderFactory txaReadeFactory = new CytoscapeTxaNetworkReaderFactory(
          txaFilter,
          applicationManager,
          networkFactory,
          networkManager,
          rootNetworkManager,
          visualMappingManager,
          visualStyleFactory,
          groupFactory,
          renderingEngineManager,
          networkViewFactory,
          vmfFactoryC,
          vmfFactoryD,
          vmfFactoryP,
          layoutManager
        )

        final txaWriterFactory = new TxaNetworkWriterFactory(txaFilter)
        final Properties writer_factory_properties = new Properties()
        writer_factory_properties.put(ID, "txaNetworkWriterFactory")
        registerAllServices(bc, txaWriterFactory, writer_factory_properties)

        final Properties reader_factory_properties = new Properties()
        reader_factory_properties.put(ID, "cytoscapeTxaNetworkReaderFactory")
        registerService(bc, txaReadeFactory, InputStreamTaskFactory.class, reader_factory_properties)

        registerAllServices(bc, new SelectSubtreeViewContextMenuFactory(), new Properties(["preferredMenu": "Select"]))
        registerAllServices(bc, new SelectAncestorTreeViewContextMenuFactory(), new Properties(["preferredMenu": "Select"]))
    }
}
