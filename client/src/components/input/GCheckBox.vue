<template>
    <label class="checkbox-container" tabindex="0" title="Check" @keypress.space.prevent="check" @keyup.enter="check">
        <slot></slot>
        <input class="checkbox" v-model="model" type="checkbox" />
        <span class="checkmark"></span>
    </label>
</template>

<script setup lang="ts">
const model = defineModel<boolean>()

function check(): void {
    model.value = !model.value
}
</script>

<style scoped lang="scss">
.checkbox-container {
    display: block;
    position: relative;
    padding: 0px 10px 0 30px;
    margin-bottom: 12px;
    cursor: pointer;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    width: fit-content;
    white-space: nowrap;

    /* Hide the browser's default checkbox */
    .checkbox {
        display: none;
    }

    /* Create a custom checkbox */
    .checkmark {
        position: absolute;
        top: 0;
        left: 0;
        height: 25px;
        width: 25px;
        background-color: var(--int-very-light-grey);
    }
}

/* On mouse-over, add a grey background color */
.checkbox-container:hover input ~ .checkmark {
    background-color: var(--int-very-light-grey-hover);
}

.checkbox-container:active input ~ .checkmark {
    background-color: var(--int-light-grey-hover);
}

/* When the checkbox is checked, add a INT background */
.checkbox-container input:checked ~ .checkmark {
    background-color: var(--int-theme);
}

.checkbox-container:hover input:checked ~ .checkmark {
    background-color: var(--int-theme-hover);
}

.checkbox-container:active input:checked ~ .checkmark {
    background-color: var(--int-theme-active);
}

/* Create the checkmark/indicator (hidden when not checked) */
.checkmark:after {
    content: "";
    position: absolute;
    display: none;
}

/* Show the checkmark when checked */
.checkbox-container input:checked ~ .checkmark:after {
    display: block;
}

/* Style the checkmark/indicator */
.checkbox-container .checkmark:after {
    left: 7px;
    top: 3px;
    width: 10px;
    height: 15px;
    border: solid black;
    border-width: 0 3px 3px 0;
    -webkit-transform: rotate(45deg);
    -ms-transform: rotate(45deg);
    transform: rotate(45deg);
}
</style>
