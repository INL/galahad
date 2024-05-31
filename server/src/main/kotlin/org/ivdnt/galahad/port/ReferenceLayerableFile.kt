package org.ivdnt.galahad.port

import org.ivdnt.galahad.data.layer.Layer

interface SourceLayerableFile {

    fun sourceLayer(): Layer

}