package nl.practicom.c4w.cytoscape

import java.awt.event.ActionEvent
import java.util.List

import javax.swing.JOptionPane

import org.cytoscape.application.CyApplicationManager
import org.cytoscape.application.swing.AbstractCyAction
import org.cytoscape.model.CyNode
import org.cytoscape.model.CyTableUtil;

class GetSelectedNodesAction extends AbstractCyAction {

    private static final long serialVersionUID = 1L
    private CyApplicationManager cyApplicationManager

   def GetSelectedNodesAction(CyApplicationManager cyApplicationManager){
        super("Practicom :: Import TXA file as network")
        setPreferredMenu("Apps.Practicom")
        this.cyApplicationManager = cyApplicationManager
    }

    @Override
    void actionPerformed(ActionEvent e) {
        if (cyApplicationManager.getCurrentNetwork() == null){
            return
        }

        //Get the selected nodes
        List<CyNode> nodes = CyTableUtil.getNodesInState(cyApplicationManager.getCurrentNetwork(),"selected",true)

        JOptionPane.showMessageDialog(null, "Number of selected nodes are ${nodes.size()}")
    }
}
