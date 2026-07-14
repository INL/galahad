<template>
    <h2>Step 4: Evaluate</h2>
    <p>
        Inspect the tagged layers and evaluate the performance of the taggers by selecting a
        <ExternalLink href="/galahad/help/general#evaluation">hypothesis layer and a reference layer</ExternalLink>. As
        a reference layer, you can choose the source layer (uploaded annotations) or a version of your corpus tagged
        with a different tagger.
    </p>
    <p>
        GaLAHaD provides various evaluation methods. The <i>distribution</i> shows the assignment of annotations to
        tokens. The <i>metrics</i>* give a general performance indication. The <i>confusion</i> table highlights
        misclassifications by class. You can also download these evaluations as CSV files.
    </p>
    <DifferentTagsetsHelp />
    <HelpLink topic="how-to-evaluate" displayName="evaluation" />
</template>
