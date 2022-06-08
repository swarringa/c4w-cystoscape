package nl.intreq.c4w.cytoscape.io.internal.txareader

import org.cytoscape.io.BasicCyFileFilter
import org.cytoscape.io.DataCategory
import org.cytoscape.io.util.StreamUtil

class CytoscapeTxaFileFilter extends BasicCyFileFilter {

  CytoscapeTxaFileFilter(final String[] extensions,
                         final String[] contentTypes,
                         final String description,
                         final DataCategory category,
                         final StreamUtil streamUtil) {
    super(extensions, contentTypes, description, category, streamUtil)
  }

  CytoscapeTxaFileFilter(final Set<String> extensions,
                         final Set<String> contentTypes,
                         final String description,
                         final DataCategory category,
                         final StreamUtil streamUtil) {
    super(extensions, contentTypes, description, category, streamUtil)
  }

  @Override
  boolean accepts(final InputStream stream, final DataCategory category) {
    category == DataCategory.NETWORK
  }

  @Override
  boolean accepts(final URI uri, final DataCategory category) {
    category == DataCategory.NETWORK
  }
}
