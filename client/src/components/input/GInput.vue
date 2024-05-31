<template>
    <div :class="inline ? 'inline' : ''">
        <!-- select -->
        <template v-if="type === 'select'">
            <!-- disabled does not work well with a dynamic property, therefore we have to bifurcate on the 'disabled' property -->
            <select v-if="disabled" disabled v-model="private_value">
                <option hidden disabled selected value> -- select an option -- </option>
                <option v-for="option in options" :key="option.key" :value="option.value" :disabled="option.disabled">
                    {{ option.text }}
                </option>
            </select>
            <select v-else v-model="private_value">
                <option hidden disabled selected value="null"> -- select an option -- </option>
                <option v-for="option in options" :key="option.key" :value="option.value" :disabled="option.disabled">
                    {{ option.text }}
                </option>
            </select>
        </template>
        <!-- select-group -->
        <template v-else-if="type === 'select-group'">
            <select v-model="private_value">
                <optgroup v-for="optgroup in options" :key="optgroup.label" :label="optgroup.label">
                    <option v-for="option in optgroup.options" :key="option.key" :value="option.value"
                        :disabled="option.disabled">
                        {{ option.text }}
                    </option>
                </optgroup>
            </select>
        </template>
        <!-- number -->
        <template v-else-if="type === 'number'">
            <input v-model.number="private_value" :type="type" :placeholder="placeholder" @keyup.enter="$emit('enter')"
                :min="min" :max="max" :step="step">
        </template>
        <!-- checkbox -->
        <template v-else-if="type === 'checkbox'">
            <label class="clickable checkbox-container">
                <slot></slot>
                <input v-model="private_value" :type="type" :placeholder="placeholder">
                <span class="checkmark" tabindex="0" @keydown="check"></span>
            </label>
        </template>
        <!-- other: text -->
        <template v-else>
            <!-- text with clear button-->
            <div v-if="clearBtn" class="clear">
                <input v-model="private_value" :type="type" :placeholder="placeholder" :disabled="disabled" :list="list"
                    :ref="refName" @keyup.enter="$emit('enter')">
                <input type="reset" value="&#10006;" :disabled="private_value === null || private_value.length == 0"
                    title="Clear" @click="private_value = ''" />
            </div>
            <!-- text without clear button-->
            <input v-else v-model="private_value" :type="type" :placeholder="placeholder" :disabled="disabled"
                :list="list" :ref="refName" @keyup.enter="$emit('enter')">


        </template>

        <template v-if="validator">
            <span id="invalidFeedback" v-if="!isValid">
                <i class="fa fa-times"></i> <i>{{ validityDescriptor }}</i>
            </span>
            <span v-else id="validFeedback">
                <i class="fa fa-check"></i> <i>{{ validityDescriptor }}</i>
            </span>
        </template>
    </div>
</template>

<script lang='ts'>
import { defineComponent, nextTick } from 'vue'

export default defineComponent({
    name: "GInput",
    emits: ['update:modelValue'],
    props: {
        checked: { type: Boolean, default: true },
        default: { default: "" },
        disabled: { type: Boolean, default: false },
        inline: { type: Boolean, default: false },
        validityDescriptor: { type: String },
        list: { type: String },
        min: { type: Number },
        max: { type: Number },
        options: { type: Array, default() { return [] } }, // for select
        placeholder: {},
        step: { type: Number },
        type: { type: String, default: "text" },
        validator: { type: Function },
        modelValue: { default: "" },
        clearBtn: Boolean,
        refName: { type: String, default: "" },
    },
    methods: {
        check(e) {
            // enter and space.
            if (e.keyCode == 13 || e.keyCode == 32) {
                this.private_value = !this.private_value
            }
        }
    },
    data() {
        return {
            private_value: ""
        }
    },
    mounted() {
        this.private_value = this.modelValue
        if (this.refName) {
            nextTick(() => {
                this.$refs.corpusName.focus()
            })
        }
    },
    computed: {
        isValid(): boolean { // TODO: bubble up the validity
            return this.validator(this.modelValue)
        }
    },
    watch: {
        private_value(newVal) {
            this.$emit('update:modelValue', newVal)
        },
        modelValue(newVal) {
            this.private_value = newVal
        }
    }
});
</script>

<style scoped lang="scss">
select,
input {
    font-style: inherit;
    font-variant-ligatures: inherit;
    font-variant-caps: inherit;
    font-variant-numeric: inherit;
    font-variant-east-asian: inherit;
    font-weight: inherit;
    font-stretch: inherit;
    font-size: inherit;
    font-family: inherit;
}

div.inline {
    display: inline-block;
}

#invalidFeedback {
    border-bottom: 2px solid var(--int-red);
    margin-left: 10px;
    padding: 2px;
}

#validFeedback {
    border-bottom: 2px solid var(--gold);
    margin-left: 10px;
    padding: 2px;
}

label {
    margin-bottom: 0px;
}

.clickable {
    cursor: pointer;
}

/* checkbox */
.checkbox-container {
    display: block;
    position: relative;
    padding: 0px 10px 0 35px;
    margin-bottom: 12px;
    cursor: pointer;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    width: fit-content;
}

/* Hide the browser's default checkbox */
.checkbox-container input {
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

/* On mouse-over, add a grey background color */
.checkbox-container:hover input~.checkmark {
    background-color: var(--int-very-light-grey-hover);
}

.checkbox-container:active input~.checkmark {
    background-color: var(--int-light-grey-hover);
}

/* When the checkbox is checked, add a INT background */
.checkbox-container input:checked~.checkmark {
    background-color: var(--int-theme);
}

.checkbox-container:hover input:checked~.checkmark {
    background-color: var(--int-theme-hover);
}

.checkbox-container:active input:checked~.checkmark {
    background-color: var(--int-theme-active);
}

/* Create the checkmark/indicator (hidden when not checked) */
.checkmark:after {
    content: "";
    position: absolute;
    display: none;
}

/* Show the checkmark when checked */
.checkbox-container input:checked~.checkmark:after {
    display: block;
}

/* Style the checkmark/indicator */
.checkbox-container .checkmark:after {
    left: 9px;
    top: 5px;
    width: 5px;
    height: 10px;
    border: solid black;
    border-width: 0 3px 3px 0;
    -webkit-transform: rotate(45deg);
    -ms-transform: rotate(45deg);
    transform: rotate(45deg);
}

/* Inputs */
input[type=text],
input[type=url],
input[type=number],
input[type=reset] {
    height: 35px;
    font-size: 1em;
    padding-left: 5px;
    border: 1px solid #ccc;
    width: 200px;
    background-color: white;

    &:focus {
        outline: var(--int-theme-active) solid 2px;
    }
}

div.clear {
    width: 208px;
    height: 39px;
    padding: 0;
    margin: 0;
    box-sizing: border-box;

    &:focus-within {
        outline: var(--int-theme-active) solid 2px;
    }

    input {
        box-sizing: border-box;
        vertical-align: bottom;
        height: 39px;
        margin: 0;

        &:focus {
            outline: none;
        }

        &[type=text] {
            width: 169px;
            border-right: 0;
        }

        &[type=reset] {
            background-color: var(--int-very-light-grey-hover);
            width: 39px;
            border-left: 0;
            font-size: 1.2em;
            cursor: pointer;

            &:disabled {
                background-color: var(--int-very-light-grey);
                color: var(--int-very-light-grey-hover);
                cursor: initial;
            }

            &:hover:not(:disabled) {
                background-color: var(--int-light-grey);
            }

            &:active:not(:disabled) {
                background-color: var(--int-light-grey-hover);
            }
        }
    }
}

select {
    border: 0px solid black;
    height: 34px;
    max-width: 300px;
    -webkit-appearance: none;
    -moz-appearance: none;
    appearance: none;
    -moz-border-radius: 0px;
    -webkit-border-radius: 0px;
    border-radius: 0px;
    padding: .375rem 1.75rem .375rem .75rem;
    outline-width: 0;
    // Background-color is taken from the firefox default.
    background: rgb(233, 233, 237) url("data:image/svg+xml;charset=utf-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='4' height='5'%3E%3Cpath fill='%23343a40' d='M2 0L0 2h4zm0 5L0 3h4z'/%3E%3C/svg%3E") no-repeat right .75rem center/8px 10px;
    font-size: 1em;
    cursor: pointer;

    &:hover {
        background-color: var(--int-very-light-grey-hover);
    }

    &:active {
        background-color: var(--int-light-grey);
    }
}

option:hover,
option:checked,
select:focus option:checked {
    background-color: var(--int-theme-hover) !important;
    box-shadow: 0 0 10px 100px var(--int-theme-hover) inset;
}
</style>