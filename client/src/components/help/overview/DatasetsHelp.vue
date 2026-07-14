<template>
    <p>
        On the <router-link to="/overview/datasets">datasets page</router-link> and corpora page you will find the test
        sets that can be used to benchmark taggers. The <i>language</i> and <i>period</i> of a dataset are given, as
        well as its size in <i>files</i> and <i>tokens</i>.
    </p>
    <p>
        You can evaluate the datasets in further detail on the
        <router-link to="/overview/benchmarks">benchmarks page</router-link> or the
        <router-link to="/annotate/evaluate">evaluation page</router-link>.
    </p>
    <p>
        For the complete datasets, including training and development sets, as well as TEI-files and statistics, please
        visit
        <ExternalLink href="https://github.com/instituutnederlandsetaal/galahad-corpus-data">
            galahad-corpus-data on GitHub </ExternalLink
        >.
    </p>
    <p>
        Would you like to contribute your own dataset? Please take a look at the
        <router-link to="/application/contribute">contribution guidelines</router-link>.
    </p>
</template>
