package nl.practicom.c4w.cytoscape

import org.cytoscape.application.CyApplicationManager
import org.cytoscape.event.CyEventHelper
import org.cytoscape.model.CyNetworkFactory
import org.cytoscape.model.CyNetworkManager
import org.cytoscape.session.CyNetworkNaming
import org.cytoscape.view.model.CyNetworkViewFactory
import org.cytoscape.view.model.CyNetworkViewManager
import org.cytoscape.view.vizmap.VisualMappingManager
import org.cytoscape.work.AbstractTaskFactory
import org.cytoscape.work.TaskIterator

class CreateNetworkTaskFactory extends AbstractTaskFactory {
  private final CyNetworkManager netMgr
  private final CyNetworkFactory cnf
  private final CyNetworkNaming namingUtil
  private final CyNetworkViewFactory cnvf
  private final CyNetworkViewManager networkViewManager
  private final CyApplicationManager cam
  private final VisualMappingManager vmm
  private final CyEventHelper eh

  CreateNetworkTaskFactory(
    final CyNetworkManager cnm,
    final CyNetworkNaming cnn,
    final CyNetworkFactory cnf,
    final CyNetworkViewFactory cnvf,
    final CyNetworkViewManager nvm,
    final CyApplicationManager cam,
    final VisualMappingManager vmm,
    final CyEventHelper eh
  ){
    this.netMgr = cnm
    this.namingUtil = cnn
    this.cnf = cnf
    this.cnvf = cnvf
    this.networkViewManager = nvm
    this.cam = cam
    this.vmm = vmm
    this.eh = eh
  }

  TaskIterator createTaskIterator(){
    return new TaskIterator(
        new CreateNetworkTask(
          netMgr, namingUtil, cnf, cnvf, networkViewManager, cam,vmm,eh
        )
    )
  }
}
