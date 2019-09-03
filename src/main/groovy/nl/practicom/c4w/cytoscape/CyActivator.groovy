package nl.practicom.c4w.cytoscape

import org.cytoscape.model.CyNetworkFactory
import org.cytoscape.model.CyNetworkManager
import org.cytoscape.service.util.AbstractCyActivator
import org.cytoscape.session.CyNetworkNaming
import org.cytoscape.view.model.CyNetworkViewFactory
import org.cytoscape.view.model.CyNetworkViewManager
import org.cytoscape.work.TaskFactory
import org.osgi.framework.BundleContext

class CyActivator extends AbstractCyActivator {
    CyActivator() {
        super();
    }


    void start(BundleContext bc) {

        CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class)
        CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class)
        CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class)
        CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class)
        CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class)

        CreateNetworkTaskFactory createNetworkTaskFactory = new CreateNetworkTaskFactory(
          cyNetworkManagerServiceRef,
          cyNetworkNamingServiceRef,
          cyNetworkFactoryServiceRef,
          cyNetworkViewFactoryServiceRef,
          cyNetworkViewManagerServiceRef
        )

        Properties props = new Properties()
        props.setProperty("preferredMenu","Apps.Practicom")
        props.setProperty("title","Import TXA")
        registerService(bc,createNetworkTaskFactory,TaskFactory.class, props)
    }
}
