package nl.practicom.c4w.cytoscape.io.internal.writer

import org.cytoscape.io.CyFileFilter
import org.cytoscape.io.write.CyNetworkViewWriterFactory
import org.cytoscape.io.write.CyWriter
import org.cytoscape.model.CyNetwork
import org.cytoscape.view.model.CyNetworkView

class TxaNetworkWriterFactory implements CyNetworkViewWriterFactory {

  CyFileFilter fileFilter

  TxaNetworkWriterFactory(CyFileFilter filter) {
    this.fileFilter = filter
  }

  @Override
  CyWriter createWriter(OutputStream os, CyNetworkView view) {
    return new TxaNetworkWriter(os, view.model)
  }

  @Override
  CyWriter createWriter(OutputStream os, CyNetwork network) {
    return new TxaNetworkWriter(os, network)
  }

  @Override
  CyFileFilter getFileFilter() {
    return this.fileFilter
  }
}
