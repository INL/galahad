<template>
  <div class="flex" style="padding: 0 !important;">
    <GCard headless noHelp class="left">
      <h2>Contents</h2>
      <ul>

        <!-- H1 -->
        <li>
          <a href="#introduction">Introduction</a>
          <ul>
            <!-- H2 -->
            <li><a href="#background">Background</a></li>
            <!-- H2 -->
            <li><a href="#galahad">Introducing GaLAHaD</a></li>
          </ul>
        </li>

        <!-- H1 -->
        <li>
          <a href="#manual">Application User Manual</a>
          <ul>

            <!-- H2 -->
            <li>
              <a href="#annotate-evaluate">Annotate & Evaluate</a>
              <ul>
                <!-- H3 -->
                <li><a href="#own-corpus">Your own corpus</a></li>
                <!-- H3 -->
                <li><a href="#explore-benchmark">Benchmark corpora</a></li>
              </ul>
            </li>

            <!-- H2 -->
            <li>
              <a href="#evaluation">On Evaluation</a>
              <ul>
                <!-- H3 -->
                <li><a href="#evaluation-terminology">Evaluation terminology</a></li>
                <!-- H3 -->
                <li>
                  <a href="#how-to-evaluate">How to evaluate</a>
                  <ul>
                    <!-- H4 -->
                    <li><a href="#distribution">Distribution</a></li>
                    <!-- H4 -->
                    <li><a href="#global-metrics">Global Metrics</a></li>
                    <!-- H4 -->
                    <li><a href="#grouped-metrics">Grouped Metrics</a></li>
                    <!-- H4 -->
                    <li><a href="#pos-confusion">Part of speech confusion</a></li>
                  </ul>
                </li>
              </ul>
            </li>

            <!-- H2 -->
            <li>
              <a href="#taggers-datasets">Taggers & Datasets</a>
              <ul>
                <!-- H3 -->
                <li><a href="#taggers">Taggers</a></li>
                <!-- H3 -->
                <li><a href="#tagsets">Tagsets</a></li>
                <!-- H3 -->
                <li><a href="#datasets">Datasets</a></li>
                <!-- H3 -->
                <li><a href="#benchmarks">Benchmarks</a></li>
              </ul>
            </li>

            <!-- H2 -->
            <li><a href="#questions">Questions</a></li>


          </ul>
        </li>

      </ul>
    </GCard>
    <div class="right">
      <GCard headless noHelp>

        <!-- H1: Introduction -->
        <h1 id="introduction">Introduction</h1>

        <!-- H2: Background -->
        <h2 id="background">Background</h2>
        <p>
          Historical texts are essential source material for both linguistic and digital humanities research. Adding
          linguistic annotation (part of speech and modern Dutch lemma) to historical text helps to make the data more
          accessible. Users need not be concerned with historical spelling variation when querying and analysing the
          data.
        </p>
        <p>
          Unfortunately, automatic linguistic annotation of historical Dutch in all its diversity still remains a
          challenge. Work has been done in several projects, both national and international, but results are
          fragmented, mutually incompatible, and far from providing a completely satisfactory solution.
        </p>
        <p>
          We have addressed this problem in the CLARIAH+ task <i>Infrastructure for historical Dutch</i>, by 1.
          defining a <a href="https://ivdnt.org/wp-content/uploads/2021/05/TDN_INT_WP_1.pdf">tagset</a> applicable to
          all phases of historical Dutch (TDN), with mappings to the
          tagsets used in existing historical and modern corpora, 2. by harmonising and extending <a
            href="https://github.com/INL/galahad-corpus-data/">training and evaluation
            material</a>, and 3. by developing an online infrastructure for historical corpus building and deployment,
          consisting of the <a href="https://autosearch.ivdnt.org/">Autosearch</a> corpus exploration environment, the
          <a href="https://portal.clarin.ivdnt.org/lancelot/">LAnCeLoT</a> tool for manual correction of linguistic
          annotation and the GaLAHaD application for the deployment and evaluation of various approaches to automatic
          linguistic annotation.
        </p>


        <!-- H2: Introducing GaLAHaD -->
        <h2 id="galahad">Introducing GaLAHaD</h2>
        <p>
          GaLAHaD serves two purposes. One is to make annotation and tool evaluation easily accessible to researchers,
          the other to make it easy for developers to contribute their tools and models to the platform, and thus
          compare them to other tools with gold standard material.
        </p>
        <p>
          <i>GaLAHaD</i> is designed to enable end users to choose the optimal path for their material. Apart from the
          basic task of uploading and annotating corpus material, <i>GaLAHaD</i> provides options to inspect and
          evaluate the result of the annotation process, in order to raise the awareness of typical errors and biases in
          the tools. The functionality of comparing annotation layers enables users to assess the accuracy of different
          tools on their data. It can be used both to evaluate a layer added by an automatic tagger with respect to a
          gold standard reference layer, or to compare layers added by different taggers. Disagreement between layers is
          not only represented by global statistics, but also illustrated by examples which are immediately visible in
          the tool. The annotated material can be uploaded to the Autosearch corpus exploration
          environment and to the <i>LAnCeLoT</i> tool for manual correction of linguistic annotation.
        </p>
        <p>
          For tool developers, the docker-based application architecture ensures easy contribution of tools to the
          platform. The application and taggers are hosted by the INT and accessible with any CLARIN-account. There is
          also the option to self-host an instance using the publicly available docker images from the
          <a href="https://hub.docker.com/u/instituutnederlandsetaal">INT Docker Hub</a> or the open source code
          available on <a href="https://github.com/INL/galahad">GitHub</a>.
        </p>

        <!-- H2: Release 1.0.0 -->
        <h2 id="release">Release 1.0.0</h2>
        <p>
          The tools for annotation with part of speech and lemma have all been trained on Gold Standard Data enriched
          with the TDN core tagset.
        </p>

        <!-- H1: Application User Manual -->
        <h1 id="manual">Application User Manual</h1>
        <p>
          In the GaLAHaD platform you can annotate corpus data with part of speech and lemma as well as evaluate the
          linguistic annotation. There are also benchmark corpora already available in the platform for which you can
          inspect the evaluation results.
        </p>

        <!-- H2: Annotate & Evaluate -->
        <h2 id="annotate-evaluate">Annotate & Evaluate</h2>
        <p>
          Click on the <code>Annotate & Evaluate</code> to start annotating and/or evaluating.
        </p>

        <!-- H3: Scenario 1 -->
        <h3 id="own-corpus">Scenario 1: Annotate and evaluate your own corpus</h3>
        <p>
          You can create a corpus within the platform that you would like to annotate. Corpora you have uploaded will be
          listed in <code>Corpora</code> under "your corpora". When you have uploaded a corpus, you can always
          modify the metadata. You can also remove an uploaded corpus from the platform.
        </p>

        <h4>Define your corpus</h4>
        <p>
          Click on <code class="green-marker">New</code> to create a corpus and fill in the metadata.
        </p>
        <p>
          The corpus name is required. When the corpus is already tagged and lemmatised, please add the name of the
          tagset. You can add additional metadata on the time period covered by your corpus and the source of your
          corpus. You can add other users with a CLARIN log-in (email) either as collaborators or as viewers.
        </p>

        <h4>Upload the corpus files</h4>
        <p>
          The created corpus is listed under "your corpora" but does not yet contain data. Go to
          <code>Documents</code> to upload the corpus files. The following formats are allowed: Plain text, TSV,
          CoNLL-U, TEI P5, TEI P4, NAF and FoLiA; see <ExternalLink href="/galahad/help/formats">Formats</ExternalLink>
          for more information.
        </p>

        <h4 id="jobs">Annotate your corpus</h4>
        <p>
          After you have uploaded your file(s), please go to <code>Jobs</code> to choose one or more taggers to annotate
          your corpus. There are filters to help you choose a tagger, but it is also possible to look at the benchmark
          results (<code>Benchmarks</code> in <code>Taggers & Datasets</code>) of the available taggers to get more
          insight into the performance of a tagger.
        </p>
        <p>
          To start a tagging job, click on <code>View & Tag</code> in the list of tools and then on <code
            class="green-marker">Start</code> to start a job. This may take a while, depending on the corpus size. The
          progress of your job or jobs is listed per tool in the tagger overview.
        </p>
        <p>
          The tagger status (pending, busy, error, finished) will be displayed in the status bar. Tagging is carried out
          in the background. You do not need to keep the application open. The total number of documents that is being
          processed indicates how busy the server is.
        </p>
        <p>
          You can also stop and delete existing jobs. A preview of the resulting annotation layer is shown as well.
        </p>

        <h4>Evaluate the linguistic annotation</h4>
        <p>
          In GaLAHaD, it is possible to do an elaborate evaluation of the linguistic annotation. In the ideal scenario,
          you select part of your corpus, do a first automatic part of speech tagging and lemmatisation with a tagger in
          the platform you think will produce the best possible automatic tagging and then manually correct the
          annotations. <a href="https://portal.clarin.ivdnt.org/lancelot/">LAnCeLoT</a> provides an environment to do
          this manual verification.
        </p>
        <p>
          You then upload your gold standard as a separate corpus to use for extensive evaluation so as to be able to
          select the best possible tagger for your data.
        </p>
        <p>
          If you have no time to produce a gold standard dataset, you can also tag your corpus with different taggers
          and inspect the differences. Choose an annotation layer of one tagger as hypothesis and an annotation layer of
          another tagger as reference.
        </p>
        <p>
          See the section below on <a href="#evaluation">Evaluation</a> for more information.
        </p>

        <h4 id="export">Export your corpus</h4>
        <p>
          To export your corpus, you first have to select the annotation layer you wish to export. You then have to
          choose the desired export format. The export will contain information on the tagger you used in the metadata.
        </p>
        <p>
          Different export formats are possible. You can either choose the same export format as your import format, or
          choose a different one. If your chosen export format is the same as the original file format, you can choose
          to merge the linguistic annotation with the uploaded source files. If you do not choose to merge, your export
          will not take the original encoding of your corpus into account.
        </p>
        <p>
          When you are only interested in main part of speech and lemma, you can choose not to export the PoS features,
          for instance <i>NOU-C</i> instead of <i>NOU-C(number=sg)</i>.
        </p>
        <p>
          When choosing TEI P5 as export format, the LAnCeLoT annotation tool is available to manually correct the
          PoS-tagging and lemmatisation of your corpus.
        </p>

        <!-- H3: Scenario 2 -->
        <h3 id="explore-benchmark">Scenario 2: explore the evaluation results of the available benchmark corpora</h3>
        <ul>
          <li>Select a corpus by clicking on a row.</li>
          <li>Click on <code>Documents</code> to view the content of the uploaded corpus.</li>
          <li>Click on <code>Jobs</code> to get an overview of the taggers used on the benchmark corpus.</li>
          <li>Click on <code>Evaluate</code> to evaluate the linguistic annotation of the selected corpus.</li>
        </ul>
        <p>
          For further information on the evaluation, see the section below.
        </p>

        <!-- H2: Evaluation -->
        <h2 id="evaluation">On Evaluation</h2>
        <p>
          It is possible to do an extensive evaluation of the linguistic annotation in the GaLAHaD platform. Before we
          explain how to evaluate, we begin with an explanation of the terminology used in <code>Evaluate</code>.
        </p>

        <!-- H3: Evaluation terminology -->
        <h3 id="evaluation-terminology">Evaluation terminology</h3>

        <h4>F1</h4>
        <p>
          A combined score of precision and recall, using micro-averaging.
        </p>
        <div>
          <code>
              F1 = 2 * (precision * recall) / (precision + recall)
            </code>
        </div>

        <h4>False negative (FN)</h4>
        <p>
          In the context of automatic linguistic annotation a false negative for label X is an instance that should have
          been annotated with label X, but is not. (The reference layer has X, the hypothesis has another label.)
        </p>

        <h4>False positive (FP)</h4>
        <p>
          In the context of automatic linguistic annotation a false positive for label X is an instance that has been
          annotated with label X, but should not have been. (The hypothesis layer has X, the reference layer has another
          label.)
        </p>

        <h4>Hypothesis layer</h4>
        <p>
          The hypothesis layer is a candidate annotation layer for your corpus. This candidate layer has been added to
          your documents by one of the taggers you have selected. You can compare this annotation layer to the reference
          layer to evaluate its quality.
        </p>

        <h4>Reference layer</h4>
        <p>
          The reference layer is the annotation layer you want to compare the hypothesis layer to. It can either be the
          source layer, i.e. the annotation that was already in your corpus when uploading it to the platform, or an
          annotation layer coming from another tagger of the platform.
        </p>

        <h4>True positive (TP)</h4>
        <p>
          In the context of automatic linguistic annotation a true positive for label X is an instance that has been
          correctly annotated with label X. (Both hypothesis and reference layer have label X.)
        </p>

        <h4>Macro-averaging versus micro-averaging</h4>
        <p>
          Macro-averaging shows average performance across classes, treating each class as equally important.
          Micro-averaging gives equal weight to every instance and shows average performance across all predictions.
        </p>
        <p>
          In corpus linguistics, where words have a Zipfian distribution, micro-averaging tends to obscure poor
          performance on less frequent words.
        </p>

        <h4>Macro F1</h4>
        <p>
          A combined score of macro precision and macro recall.
        </p>
        <div>
          <code>
            Macro F1 = 2 * (macro precision * macro recall) / (macro precision + macro recall)
          </code>
        </div>

        <h4>Macro precision</h4>
        <p>
          Precision measured using macro-averaging.
        </p>

        <h4>Macro recall</h4>
        <p>
          Recall measured using macro-averaging.
        </p>

        <h4>Micro accuracy</h4>
        <p>
          Accuracy using micro-averaging.
        </p>

        <h4>Multiple PoS</h4>
        <p>
          For tokens that in fact consist of more than one word, a multiple analysis is given. This means that one token
          is not only assigned more than one lemma but also more than one part of speech. An example: <code>int</code>
          analysed as <code>IN (ADP) + HET (PD)</code>. The evaluation results for part of speech tagging are taking the
          assignment of multiple part of speech tags into account.
        </p>

        <h4>No match</h4>
        <p>
          A <i>no match</i> measures the amount of instances for which there was an issue with the alignment of
          hypothesis and reference layer.
        </p>

        <h4>Precision</h4>
        <p>
          Precision indicates the degree of correctness. It measures how many of the instances that have been assigned a
          label in the hypothesis layer have the correct label.
        </p>
        <div>
          <code>
            Precision = TP / (TP+FP)
          </code>
        </div>
        <p>
          For instance, if 100 tokens have been assigned the tag VRB, and only 80 of these labels are correct, precision
          is 80%.
        </p>

        <h4>Recall</h4>
        <p>
          Recall gives information on what has been missed. It measures how many of the annotations that have a label in
          the reference layer have the same label in the hypothesis layer.
        </p>
        <div>
          <code>Recall = TP / (TP+FN)</code>
        </div>
        <p>
          For instance, if 100 tokens should be labelled VRB, and only 90 of these (the true positives) are also
          labelled VRB in the hypothesis layer, recall is 90%.
        </p>

        <!-- H3: How to evaluate -->
        <h3 id="how-to-evaluate">How to evaluate and what are the evaluation metrics</h3>
        <p>
          To evaluate the linguistic annotation of a corpus, choose a hypothesis layer and a reference layer. The
          available layers in your corpus will be shown in the drop-down list. Once you have selected a reference layer
          and a hypothesis layer, the evaluation metrics will be computed. The result is information on
          <code>Distribution</code>, <code>Global Metrics</code>, <code>Grouped Metrics</code> and
          <code>Pos Confusion</code>. The evaluation results are downloadable.
        </p>

        <h4 id="distribution">Distribution</h4>
        <p>
          In <code>Distribution</code> an insight is given into the annotation of your corpus by showing what lemma,
          part of speech pairs have been assigned to which types.
        </p>
        <p>
          Numbers are given for <i>lemma/single PoS</i>, <i>lemma/multiple PoS</i> or <i>lemma/both single and multiple
            PoS</i>. You can refine each overview by searching on lemma or type or by clicking on the part of speech
          checkboxes.
        </p>
        <p>
          The result is presented in a table; an example:
        </p>
        <img src="@/assets/help/distribution.png" />
        <p>
          When there are more than five types you can click on the inspect symbol to view all types of a lemma-PoS
          combination. You can order the information by clicking on the small arrows.
        </p>

        <h4 id="global-metrics">Global Metrics</h4>
        <p>
          In <code>Global Metrics</code> an overall overview is given of the (dis)agreement between the two layers that
          have been selected for lemma and PoS comparison.
        </p>
        <p>
          The <code>Basic Global Metrics</code> table gives information on lemma, PoS or the combination of Lemma and
          PoS. The <code>Extended Global Metrics</code> table gives more detailed information on the accuracy of tokens
          with a single analysis versus tokens with a multiple analysis.
        </p>
        <p>
          The <b>grouped by</b> column defines the classes over which macro-averaging is computed. For example,
          <i>annotation=Lemma</i> and <i>grouped by=Lemma</i> computes the macro precision, recall and F1 for lemma
          assignment with equal weight for each lemma, thus counterbalancing the frequent word bias.
        </p>
        <p>
          Grouped by and Annotation may also have different values. For instance, <i>annotation=Lemma</i> and grouped
          <i>by=PoS</i> (in <code>Extended Global Metrics</code>) means that macro precision for lemma assignment is
          computed by assigning equal weight to the precisions measured within each PoS class separately.
        </p>
        <p>
          The result is presented in two different tables; an example:
        </p>
        <img src="@/assets/help/basic-global-metrics.png" />
        <img src="@/assets/help/extended-global-metrics.png" />
        <p>
          In both tables, it is possible to see a data sample of true positives or true negatives, by clicking on the
          percentage. Samples are downloadable.
        </p>
        <p>
          A data sample of Annotation PoS (single), grouped by PoS, True Positive:
        </p>
        <img src="@/assets/help/metrics-sample-pos.png" />
        <p>
          Another example, for which the hypothesis layer had a different tagset than the reference layer:
        </p>
        <img src="@/assets/help/basic-global-metrics-bad.png" />
        <img src="@/assets/help/extended-global-metrics-bad.png" />
        <p>
          A data sample of Annotation Lemma (single), grouped by Lemma, True Positive:
        </p>
        <img src="@/assets/help/metrics-sample-lemma.png" />

        <h4 id="grouped-metrics">Grouped Metrics</h4>
        <p>
          In <code>Grouped Metrics</code> an overview is given of the (dis)agreement between the two layers per part of
          speech.
        </p>
        <p>
          By default the metrics are given for the annotation part of speech, grouped by part of speech for both single
          and multiple analysis. By changing the value of the <code>Annotation</code>, <code>Group by</code> and
          <code>Single/multiple analysis</code>, the other available metrics per PoS can be displayed. See <a
            href="#global-metrics">Global Metrics</a> for a further explanation of what <b>grouped by</b> means.
        </p>
        <p>
          The result is presented in a table, for example:
        </p>
        <img src="@/assets/help/grouped-metrics.png" />
        <p>
          For each table it is possible to see a data sample of true positives, false positives and false negatives, by
          clicking on the percentage. Samples are downloadable.
        </p>
        <p>
          A data sample of Group NOU-C, False Negative (Annotation PoS; Group by PoS; Both single/multiple analysis):
        </p>
        <img src="@/assets/help/grouped-metrics-sample.png" />
        <p class="red">
          Note that token mismatches can occur when the tagger treats punctuation for abbreviations as a separate token
          (in abbreviations like "e.g." or "i.e.", or in "mr." in the example above).
        </p>

        <h4 id="pos-confusion">Part of speech confusion</h4>
        <p>
          In <code>Pos Confusion</code>, an overview is given of the matches (in green) and mismatches per PoS when
          comparing the tagging of the hypothesis layer with the reference layer. If the hypothesis layer and reference
          layer have been tagged using a different tagset, the confusion table can give more detailed insight into the
          part of speech tagging.
        </p>
        <p>
          The confusion table contains information on each part of speech and on the amount of no-matches. The category
          "MULTIPLE" contains combined tags like "ADP+NOU-C" or "VRB+PD+PD". These are shown in one cell, but this does
          not mean that the taggers agree on the exact tags. Click on the cell or look at the
          <code>Global Metrics</code> for more details. NO_POS refers to missing part of speech tagging, PC refers to
          the encoding of punctuation and <i>Missing Match</i> refers to tokens that could not be aligned (for example,
          due to how a tagger treats punctuation in abbreviations).

        </p>
        <p>
          An example of a confusion matrix:
        </p>
        <img src="@/assets/help/pos-confusion.png" />
        <p>
          To see a data sample, click on a cell with frequency > 0. Samples are downloadable.
        </p>
        <p>
          A data sample of the confusion between VRB and NOU-C:
        </p>
        <img src="@/assets/help/pos-confusion-samples.png" />

        <!-- H2: Taggers & Datasets -->
        <h2 id="taggers-datasets">Taggers & Datasets</h2>

        <!-- H3: Taggers -->
        <h3 id="taggers">Taggers</h3>
        <p>
          Here you can find an overview of all taggers (for part of speech and lemma) the GaLAHaD platform offers. The
          overview gives a short description of the tool, the tagset, the period the training data covered and the
          annotation types. You can also find a link to the training data, the software and the model.
        </p>

        <!-- H3: Tagsets -->
        <h3 id="tagsets">Tagsets</h3>
        <p>
          Here you can find an overview of the possible tagsets used for annotation of Dutch. For now, GaLAHaD offers
          taggers that have been trained on Gold Standard Data using the TDN core tagset.
        </p>

        <!-- H3: Datasets -->
        <h3 id="datasets">Datasets</h3>
        <p>
          Here you can find an overview of the text sets that have been used to benchmark the taggers. The complete
          benchmark sets, including training and development sets can be found on Github:
          <a href="https://github.com/INL/galahad-corpus-data">galahad-corpus-data</a>.
        </p>

        <!-- H3: Benchmarks -->
        <h3 id="benchmarks">Benchmarks</h3>
        <p>
          Here you can see how well the different available taggers do on the different benchmark datasets. Choose a
          dataset to get an overview.
        </p>
        <p>
          Per tagger, macro precision, macro recall, macro F1 and micro accuracy are given; by default for the
          annotation part of speech, grouped by part of speech for both single and multiple analysis. By changing the
          value of the <code>Annotation</code>, <code>Group by</code> and <code>Single/multiple analysis</code>, the
          other available metrics per tagger can be displayed. See <a href="#global-metrics">Global Metrics</a> for a
          further explanation of what grouped by means. For more detailed information on the evaluation of a tagger,
          click on <code>Details</code>.
        </p>
        <p>
          An example:
        </p>
        <img src="@/assets/help/benchmarks.png" />

        <!-- H2: Questions -->
        <h2 id="questions">Questions</h2>
        <p>
          If you have questions, please contact
          <MailAddress />.
        </p>

      </GCard>
    </div>
  </div>
</template>

<script setup>
import { GCard } from '@/components'
</script>

<style scoped>
img {
  display: block;
  width: 100%;
  margin: 0 auto;
}

p.red {
  color: red;
}

h3,
h4 {
  font-weight: bold;
}

.right :deep(.content-wrapper)>.content {
  flex: 0 1 800px !important;
}

code {
  font-size: 16px;

  &.green-marker {
    background-color: var(--int-green);
    padding: 0.2rem 0.3rem;
  }
}

a,
a:hover,
a:active,
a:visited {
  color: #000;
}

.flex {
  display: flex !important;
  flex-direction: row !important;
  overflow: hidden !important;
}

.right {
  overflow-y: auto;
}

.left {
  flex: 1;
  border-right: 1px solid var(--int-light-grey);
  padding-bottom: 0;
  overflow-y: auto;


  h2 {
    margin-bottom: -0.3rem;
  }

  ul {
    padding-left: 2rem;
    margin-bottom: 0;
  }

  ul ul {
    padding-left: 1rem;
  }

  li {
    margin: 0.3rem 0;
  }
}

@media (max-width: 800px) {
  .flex {
    flex-direction: column !important;
  }

  .left {
    border-right: none;
    display: block;

    ul ul {
      padding-left: 2rem;
    }
  }

  :deep(.left) .content-wrapper {
    justify-content: initial;
  }
}
</style>