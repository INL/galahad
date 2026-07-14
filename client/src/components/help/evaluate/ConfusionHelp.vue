<template>
    <p>
        In part-of-speech confusion, an overview is given of the matches (in green) and mismatches per PoS when
        comparing the tagging of the hypothesis layer with the reference layer. Click on any frequency below to show a
        data sample.
    </p>
    <p>
        The category "MULTIPLE" contains combined tags like "ADP+NOU" or "VRB+PD+PD". These are shown in one cell, but
        this does not mean that the taggers agree on the exact tags. Click on the cell or look at the Global Metrics for
        more details.
    </p>
    <DifferentTagsetsHelp />
    <HelpLink topic="confusion" />
</template>
