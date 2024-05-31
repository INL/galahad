<template>
  <div>
    <h2>Overview</h2>
    <p>
      Welcome to GaLAHaD: Generating Linguistic Annotations for Historical Dutch. This page provides an overview of the
      GaLAHaD application.
    </p>
    <p>
      GaLAHaD is a platform that enables (non-technical) users to add linguistic annotation to (primarily) historical
      Dutch documents, which can then in turn be inspected, evaluated and exported. Here follows a short introduction of
      what GaLAHaD entails and what users can expect from this platform. Thereafter follows a short step-by-step guide
      that takes you through the platform.
    </p>
    <h4>Context of the application</h4>
    <p>
      Linguistic annotation of (historical) texts is invaluable for various kinds of research and, of course, a
      consistent and apt annotation with the least amount of errors is necessary if one wants to research (words in)
      large amounts of text. However, lack of a central place where researchers were able to look at the possibilities
      of linguistically enriching historical Dutch texts made it hard to find a good fitting model without substantial
      effort and technical knowledge. So instead, researchers might use (or develop) a model for their own documents
      without looking at other models.
    </p>
    <p>
      Although researchers use their own models, there was a wish to be able to look at all the possibilities of
      linguistically enriching historical texts and also to compare the different models used and to be able to use the
      one(s) most suited for their specific data. Therefore, in the context of
      <GlossaryLink gloss="clariah">CLARIAH</GlossaryLink> (Common Lab Research Infrastructure for the Arts and
      Humanities), GaLAHaD has been created at <GlossaryLink gloss="int">INT</GlossaryLink> to fulfil in that need.
    </p>
    <h3>Intended use</h3>
    <h4><i>For linguistic users</i></h4>
    <p>
      GaLAHaD aims to be a central platform that allows users to do various things. The main thing for researchers is to
      linguistically enrich (historical) data (corpora). GaLAHad enables a user to upload documents (in several formats)
      to a corpus. This corpus can then be enriched with one or more taggers; this is also possible if a corpus has
      already been enriched. Each of these taggers (and also enrichment that was added prior to uploading the documents)
      can then be compared and evaluated.
    </p>
    <p>
      With several (visually helpful) criteria a user should be able to decide which tagger is the best for their data
      in a reasonable timespan. It is also possible to download all evaluation criteria in separate Excel-files.
      Furthermore, a user is then able to export their corpus to
      <GlossaryLink gloss="blacklab">BlackLab</GlossaryLink>, to add some manual corrections (<GlossaryLink
        gloss="cobalt"
        >CoBaLT</GlossaryLink
      >) or to download the enriched data (either as new documents or appended to the old documents). It should be noted
      that individual steps take longer the more data are uploaded.
    </p>
    <p>It also possible to make your corpus/corpora publicly available. TBD</p>
    <h4><i>For technical users</i></h4>
    <p>
      For technical users it is also possible to look at the performance of all taggers that exist in GaLAHaD.
      Furthermore, it is also possible to add your own tagger to see how it compares to the other taggers. TBD
    </p>
    <h4>What can you expect of the Galahad?</h4>
    <p>
      GaLAHaD as a platform thus serves as a central hub for enriching historical Dutch. For instance, it allows users
      to compare different taggers, to annotate data and to add your own material. Users can expect all of this to be
      done rather easily and fast, though it should be noted that each operation takes more time, the bigger the
      corpus/corpora (or documents) is/are. Besides ease of access, you can also expect good quality of linguistically
      enriched data, though it of course depends how suited a tagger is for the specific data. Some taggers are more
      suited (more trained) for older texts and some more for newer texts.
    </p>
    <p>
      Even with the current state of the art, GaLAHaD can at best help to minimize the amount of manual corrections
      needed. Depending on your use case, you should still expect that a significant post-processing step is required.
    </p>
  </div>
</template>
<script setup lang="ts">
// Components
import { GlossaryLink } from "@/components" 
</script>
