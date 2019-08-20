package nl.practicom.c4w.cytoscape

import org.cytoscape.application.CyApplicationManager
import org.cytoscape.application.swing.CyAction
import org.cytoscape.service.util.AbstractCyActivator
import org.osgi.framework.BundleContext

class CyActivator extends AbstractCyActivator {
    CyActivator() {
        super()
    }

    void start(BundleContext bc) {
        CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class)
        GetSelectedNodesAction getSelectedNodesAction = new GetSelectedNodesAction(cyApplicationManager)
        registerService(bc,getSelectedNodesAction,CyAction.class,new Properties())
    }
}
