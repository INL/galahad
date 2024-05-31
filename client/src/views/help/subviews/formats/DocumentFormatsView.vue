<template>
  <div>
    <GCard headless>
      <h1>Document Formats</h1>

      <p>
        The following formats are supported by GaLAHaD. At the very least, a file of the respective format can be
        uploaded. Most formats can also be used as an export format. This includes creating new files of that format to
        download irrespective of the original document,
        but also merging files of that format with the original document to preserve metadata and structure (only if it
        is of
        the same format of course).
      </p>

      <h2>Text-like formats</h2>

      <section>
        <h3>Plain text</h3>
        <FormatCapabilities uploadable />
        <p>
          Plain text files only contain text, no annotations.
        </p>
      </section>

      <section>
        <h3>Tab Separated Values (TSV)</h3>
        <FormatCapabilities uploadable exportable mergeable />
        <FormatModeExplanation>
          <template #import>
            TSV files may contain any number of columns in any order. However, a header is required.
            GaLAHaD looks
            for three columns:
            <ul>
              <li>A token column, called any of: <b>word, token, literal, term, form</b></li>
              <li>A lemma column called: <b>lemma</b></li>
              <li>A part of speech column called: <b>pos</b></li>
            </ul>
          </template>
          <template #export>
            <p>
              GaLAHaD exports TSV files with the columns <b>word</b>, <b>lemma</b> and <b>pos</b>; in that order.
            </p>
            <code><pre>
word    lemma    pos
hello   hello    INT
world   world    NOU
</pre></code>
          </template>
          <template #merge>
            When merging, the original columns are preserved.
          </template>
        </FormatModeExplanation>
      </section>

      <section>
        <h3>CoNLL-U</h3>
        <FormatCapabilities uploadable exportable mergeable />
        <FormatReference href="https://universaldependencies.org/format.html">
          CoNLL-U (Universal Dependencies)
        </FormatReference>
        <p>
          CoNLL-U files are tab separated text files with a fixed column structure. They can also contain comments. See
          the reference for details.
        </p>
        <FormatModeExplanation>
          <template #import>
            When importing, GaLAHaD reads the part of speech out of the <b>XPOS</b> column.
          </template>
          <template #export>
            When exporting, GaLAHaD fills in the columns <b>ID</b>, <b>FORM</b>, <b>LEMMA</b>, and <b>XPOS</b>.
          </template>
          <template #merge>
            When merging, the original columns are preserved.
          </template>
        </FormatModeExplanation>
      </section>

      <h2>XML formats</h2>

      <section>
        <h3>TEI P5</h3>
        <FormatCapabilities uploadable exportable mergeable />
        <FormatReference href="https://tei-c.org/guidelines/p5">P5 Guidelines</FormatReference>
        <p>
          TEI P5 is the current version of the <i>TEI Guidelines for Electronic Text Encoding and Interchange</i>, which
          define and document a markup language for representing the structural, renditional, and conceptual features of
          texts.
          Tokens are encoded as element &lt;w&gt;, punctuation as &lt;pc&gt;.
        </p>
        <FormatModeExplanation>
          <template #import>
            The lemma and part of speech are attributes
            of &lt;w&gt;, namely <i>@lemma</i> and <b>@pos</b>. Note how <b>@pos</b> differs from the <b>@type</b>
            attribute
            in other TEI versions.
            <code><pre>
&lt;w lemma="word" <b>pos="NOU"</b>&gt;
  word
&lt;/w&gt;
</pre></code>
            When importing, intertwined styling tags like &lt;hi&gt; are supported. Unannotated and untokenized text is
            also
            supported.
          </template>
        </FormatModeExplanation>
      </section>

      <section>
        <h3>TEI P5 Legacy</h3>
        <FormatCapabilities uploadable mergeable />

        <FormatModeExplanation>
          <template #import>
            This format is similar to TEI P5, but with the part of speech encoded as in TEI P4, i.e. <b>@type</b>
            instead
            of <b>@pos</b>.
            <code><pre>
&lt;w lemma="word" <b>type="VRB"</b>&gt;
  word
&lt;/w&gt;</pre></code>
          </template>
          <template #merge>
            <TeiP5LegacyWarning />
          </template>

        </FormatModeExplanation>
      </section>

      <section>
        <h3>TEI P4</h3>
        <FormatCapabilities uploadable />
        <FormatReference href="https://tei-c.org/Vault/P4">P4 Guidelines</FormatReference>
        <p>
          Previous version of the TEI guidelines. Lemma is in the attribute <i>@lemma</i>, part of speech in attribute
          <b>@type</b>.
        </p>
      </section>

      <section>
        <h3>NLP Annotation Format (NAF)</h3>
        <FormatCapabilities uploadable exportable mergeable />
        <FormatReference href="https://github.com/newsreader/NAF">github.com/newsreader/NAF</FormatReference>
        <p>
          NAF is a stand-off, multilayered annotation schema for representing linguistic annotations. Only the terms
          layer
          and
          the token layer are taken into account.
        </p>
      </section>

      <section>
        <h3>Format for Linguistic Annotation (FoLiA)</h3>
        <FormatCapabilities uploadable exportable mergeable />
        <FormatReference href="https://proycon.github.io/folia">FoLiA</FormatReference>
        <p>
          FoLiA is an XML-based annotation format, suitable for the representation of linguistically annotated language
          resources.
        </p>
        <FormatModeExplanation>
          <template #import>
            If a word (&lt;w&gt;) has multiple lemmas or part of speech annotations, only the last one is imported.
            <code><pre>
&lt;w&gt;
  &lt;t&gt;word&lt;/t&gt;
  <s>&lt;lemma class="word" set="example"/&gt;</s>
  <b>&lt;lemma class="word" set="other_example"/&gt;</b>
  <s>&lt;pos class="VRB" set="example"/&gt;</s>
  <b>&lt;pos class="NOU" set="other_example"/&gt;</b>
&lt;/w&gt;</pre></code>
            In &lt;correction&gt; tags, the &lt;new&gt; tag is imported.
            <code><pre>
&lt;w&gt;
  &lt;t&gt;word&lt;/t&gt;
  &lt;correction&gt;
    &lt;new&gt;
      <b>&lt;pos class="NOU" set="other_example"/&gt;</b>
    &lt;/new&gt;
    &lt;original&gt;
      <s>&lt;pos class="VRB" set="example"/&gt;</s>
    &lt;/original&gt;
  &lt;/correction&gt;
&lt;/w&gt;</pre></code>
            Intertwined &lt;t-style&gt; tags are supported.
            Lastly, we also support unannotated and untokenized text.

          </template>
          <template #merge>
            When merging, the lemma and part of speech annotations generated by GaLAHaD are added as the last annotations.
            <code><pre>
&lt;w&gt;
  &lt;t&gt;word&lt;/t&gt;
  &lt;lemma class="word" set="example"/&gt;
  &lt;lemma class="word" set="other_example"/&gt;
  &lt;pos class="VRB" set="example"/&gt;
  &lt;pos class="NOU" set="other_example"/&gt;
  <b>&lt;lemma class="word" set="GaLAHaD_example"/&gt;
  &lt;pos class="NOU(num=sg)" set="GaLAHaD_example"/&gt;</b>
&lt;/w&gt;</pre></code>
          </template>
        </FormatModeExplanation>
      </section>
    </GCard>
  </div>
</template>

<script setup lang='ts'>
import FormatCapabilities from '@/views/help/subviews/formats/FormatCapabilities.vue'
import FormatReference from '@/views/help/subviews/formats/FormatReference.vue'
import FormatModeExplanation from '@/views/help/subviews/formats/FormatModeExplanation.vue'
import TeiP5LegacyWarning from '@/views/help/subviews/formats/TeiP5LegacyWarning.vue'
</script>


<style scoped>
:deep(.content-wrapper)>.content {
  flex: 0 1 800px !important;
}

h1,
h2,
:deep(h3),
:deep(h4),
ul {
  margin-bottom: 0.3rem;
  margin-top: 0.3rem;
}

section {
  margin-bottom: 2rem;
}

:deep(p) {
  margin: 0.5rem 0;
}

:deep(pre) {
  margin: 0.5rem 0;
  font-size: 14px;
}
</style>
