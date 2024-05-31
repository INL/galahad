<template>
    <div class="infobox" v-if="eval.generated">
        <table>
            <!-- In common -->
            <tr>
                <td><i>Generated:</i></td>
                <td>{{ new Date(eval.generated).toLocaleString() }}</td>
            </tr>

            <!-- Distribution only -->
            <tr v-if="eval.lastModified">
                <td><i>Layer last modified (at time of generation):</i></td>
                <td>{{ new Date(eval.lastModified).toLocaleString() }}</td>
            </tr>

            <!-- Confusion & metrics only -->
            <tr v-if="eval.hypothesisLastModified">
                <td><i>Hypothesis layer last modified (at time of generation):</i></td>
                <td>{{ new Date(eval.hypothesisLastModified).toLocaleString() }}</td>
            </tr>
            <tr v-if="eval.referenceLastModified">
                <td><i>Reference layer last modified (at time of generation):</i></td>
                <td>{{ new Date(eval.referenceLastModified).toLocaleString() }}</td>
            </tr>

            <!-- Distribution only -->
            <tr v-if="eval.totalChars > 0">
                <td><i>Covered characters:</i></td>
                <td>{{ (eval.coveredChars * 100 / eval.totalChars).toFixed(0) }}%</td>
            </tr>
            <tr v-if="eval.totalAlphabeticChars > 0">
                <td><i>Covered alphabetic characters:</i></td>
                <td>{{ (eval.coveredAlphabeticChars * 100 / eval.totalAlphabeticChars).toFixed(0) }}%</td>
            </tr>
        </table>
    </div>
</template>

<script setup lang='ts'>
defineProps(['eval'])
</script>

<style scoped lang='scss'>
.infobox {
    table {
        margin: 0 0 0 auto; // Align right

        tr td:first-child {
            text-align: right;
        }
    }

    margin: 10px;
    background-color: var(--int-very-light-grey);
    padding: 5px;

    p {
        padding: 0;
        margin: 0;
    }
}
</style>
