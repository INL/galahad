<template>
    <p>
        On the <router-link to="/overview/taggers">taggers page</router-link> you can find an overview of all taggers
        (annotation tools) in GaLAHaD. The overview gives a short <i>description</i> of the tool, the
        <i>language</i> and <i>period</i> the training data covered, the <i>annotation types</i>, and any
        <i>attributions</i>.
    </p>
    <p>
        By clicking on the <i>annotations</i>, you can see which annotation principles (e.g. tagsets) are used by a
        tool. For each principle, a link is provided to the reference document. Version, software and dataset
        information can be found by clicking on the <i>attributions</i>.
    </p>
    <p>
        Would you like to contribute your own tool? Please take a look at the
        <router-link to="/application/contribute">contribution guidelines</router-link>.
    </p>
</template>
