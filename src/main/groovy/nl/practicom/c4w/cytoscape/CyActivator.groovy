package nl.practicom.c4w.cytoscape

import org.cytoscape.application.CyApplicationManager
import org.cytoscape.event.CyEventHelper
import org.cytoscape.model.CyNetworkFactory
import org.cytoscape.model.CyNetworkManager
import org.cytoscape.service.util.AbstractCyActivator
import org.cytoscape.session.CyNetworkNaming
import org.cytoscape.view.model.CyNetworkViewFactory
import org.cytoscape.view.model.CyNetworkViewManager
import org.cytoscape.view.vizmap.VisualMappingManager
import org.cytoscape.work.TaskFactory
import org.osgi.framework.BundleContext

class CyActivator extends AbstractCyActivator {
    CyActivator() {
        super();
    }


    void start(BundleContext bc) {

        final CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class)
        final CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class)
        final CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class)
        final CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class)
        final CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class)

        final CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
        final VisualMappingManager vmmServiceRef = getService(bc,VisualMappingManager.class)
        final CyEventHelper cyEventHelper = getService(bc, CyEventHelper.class)


        CreateNetworkTaskFactory createNetworkTaskFactory = new CreateNetworkTaskFactory(
          cyNetworkManagerServiceRef,
          cyNetworkNamingServiceRef,
          cyNetworkFactoryServiceRef,
          cyNetworkViewFactoryServiceRef,
          cyNetworkViewManagerServiceRef,
          cyApplicationManagerServiceRef,
          vmmServiceRef,
          cyEventHelper
        )

        Properties props = new Properties()
        props.setProperty("preferredMenu","Apps.Practicom")
        props.setProperty("title","Import TXA")
        registerService(bc,createNetworkTaskFactory,TaskFactory.class, props)
    }
}
