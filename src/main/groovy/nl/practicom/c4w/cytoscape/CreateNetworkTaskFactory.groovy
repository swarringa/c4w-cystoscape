package nl.practicom.c4w.cytoscape

import org.cytoscape.model.CyNetworkFactory
import org.cytoscape.model.CyNetworkManager
import org.cytoscape.session.CyNetworkNaming
import org.cytoscape.view.model.CyNetworkViewFactory
import org.cytoscape.view.model.CyNetworkViewManager
import org.cytoscape.work.AbstractTaskFactory
import org.cytoscape.work.TaskIterator

class CreateNetworkTaskFactory extends AbstractTaskFactory {
  private final CyNetworkManager netMgr
  private final CyNetworkFactory cnf
  private final CyNetworkNaming namingUtil
  private final CyNetworkViewFactory cnvf
  private final CyNetworkViewManager networkViewManager

  CreateNetworkTaskFactory(
    final CyNetworkManager netMgr,
    final CyNetworkNaming namingUtil,
    final CyNetworkFactory cnf,
    final CyNetworkViewFactory cnvf,
    final CyNetworkViewManager networkViewManager
  ){
    this.netMgr = netMgr
    this.namingUtil = namingUtil
    this.cnf = cnf
    this.cnvf = cnvf
    this.networkViewManager = networkViewManager
  }

  TaskIterator createTaskIterator(){
    return new TaskIterator(new CreateNetworkTask(netMgr, namingUtil, cnf, cnvf, networkViewManager))
  }
}
