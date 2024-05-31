<!-- create or update a corpus -->
<template>
    <GModal :title="title" :show="show" @hide="$emit('hide')" :showHelp="showHelp">
        <template #help>
            <slot name="help"></slot>
        </template>
        <table>
            <template v-if="userStore.hasWriteAccess || !item">

                <tr>
                    <td>
                        <label>Name:</label> <span class="warning"><small>(Required)</small></span>
                    </td>
                    <td>
                        <GInput v-model="name" refName="corpusName" placeholder="corpus name"
                            :validator="validateCorpusName" validityDescriptor="3-100 characters" @enter="doAction" />
                    </td>
                </tr>
                <tr>
                    <td><label>Year from:</label></td>
                    <td>
                        <GInput v-model.number="eraFrom" validityDescriptor="Must be before end year"
                            :validator="(v) => { return v <= eraTo }" :min="-10000" :max="10000" placeholder="YYYY"
                            :step="100" @enter="doAction" />
                    </td>
                </tr>

                <tr>
                    <td><label>Year to:</label></td>
                    <td>
                        <GInput v-model.number="eraTo" validityDescriptor="Must be after start year" placeholder="YYYY"
                            :validator="(v) => { return v >= eraFrom }" :min="-10000" :max="10000" @enter="doAction" />
                    </td>
                </tr>

                <tr>
                    <td>
                        <label>
                            <ExternalLink href="/galahad/overview/tagsets">Tagset</ExternalLink>:
                        </label>
                    </td>
                    <td>
                        <GInput v-model="tagset" list="tagsets" @enter="doAction" placeholder="tagset name" />
                        <datalist id="tagsets">
                            <option v-for="(tagset, _) in tagsetsStore.tagsets" :value="tagset.shortName"></option>
                        </datalist>
                    </td>
                </tr>

                <template v-if="userStore.user.admin">
                    <tr>
                        <td colspan="2">
                            <hr>
                        </td>
                    </tr>

                    <tr>
                        <td>Benchmark set:</td>
                        <td>
                            <GInput type="checkbox" v-model="dataset">Benchmark</GInput>
                        </td>
                    </tr>
                </template>

                <tr>
                    <td colspan="2">
                        <hr>
                    </td>
                </tr>

                <tr>
                    <td><label>Source name:</label></td>
                    <td>
                        <GInput v-model="sourceName" placeholder="source name" @enter="doAction" />
                    </td>
                </tr>

                <tr>
                    <td><label>Source url:</label></td>
                    <td>
                        <GInput v-model="sourceURL" type="url" placeholder="source url" @enter="doAction" />
                    </td>
                </tr>

                <UserList :users="collaborators" listName="Collaborators" :showAddDialog="showAddDialog" />
            </template>
            <UserList :users="viewers" listName="Viewers" :showAddDialog="showAddDialog" />

        </table>

        <template #buttons>
            <GButton green @click="doAction" :disabled="disabled">{{ update ? 'Update' : 'Create' }}</GButton>
        </template>
    </GModal>
</template>

<script lang='ts'>
import { defineComponent } from 'vue';
import stores from '@/stores'

import { MutableCorpusMetadata } from '@/types/corpora'

import { GInput, GButton, GlossaryLink, ExternalLink } from '@/components'
// Component dependencies.
import UserList from '@/components/modals/corpus/UserList.vue'

export default defineComponent({
    name: "CorpusForm",
    components: {
        GlossaryLink, UserList
    },
    props: {
        action: { type: Function },
        cancel: { type: Function },
        item: { default: null },
        update: { type: Boolean, default: false },
        showHelp: { type: Boolean, default: false },
        title: { type: String, default: "" },
        show: { type: Boolean, default: true }
    },
    setup() {
        const userStore = stores.useUser()
        const tagsetsStore = stores.useTagsets()
        return { userStore: userStore, tagsetsStore: tagsetsStore }
    },
    data() {
        return {
            public: false,
            dataset: false,
            name: "",
            eraFrom: null,
            eraTo: null,
            tagset: "",
            sourceName: "",
            sourceURL: "",
            collaborators: [],
            viewers: [],
            newUser: "",
        }
    },
    computed: {
        // Only used for the viewers list for when you yourself are a viewer:
        // You may see the name list, but not add to it.
        showAddDialog() {
            // Only show it when you're not editing (i.e. you pressed 'new')
            // Or if you did press edit, only show it when you have access
            return !this.update || this.userStore.hasDeleteAccess
        },
        disabled() {
            if (!this.item && this.update) return true
            const item = this.item as MutableCorpusMetadata
            return !this.isValid || (this.update &&
                this.name === item.name &&
                this.eraFrom === item.eraFrom &&
                this.eraTo === item.eraTo &&
                this.tagset === item.tagset &&
                this.collaborators.join('\n') === item.collaborators.join('\n') &&
                this.viewers.join('\n') === item.viewers.join('\n') &&
                this.sourceName === item.sourceName &&
                this.sourceURL === item.sourceURL &&
                this.public === item.public &&
                this.dataset === item.dataset
            )
        },
        isValid() {
            if (!this.validateCorpusName(this.name)) return false
            // check if eras are integer values
            if (this.eraFrom && !Number.isInteger(this.eraFrom)) return false
            if (this.eraTo && !Number.isInteger(this.eraTo)) return false
            if (this.eraFrom > this.eraTo) return false
            return true
        }
    },
    methods: {
        doAction() {
            if (!this.validateCorpusName(this.name)) return
            const value: MutableCorpusMetadata = {
                owner: "", // this is set by the server for security reasons
                name: this.name,
                eraFrom: this.eraFrom,
                eraTo: this.eraTo,
                tagset: this.tagset,
                dataset: this.dataset,
                public: this.public,
                collaborators: this.collaborators,
                viewers: this.viewers,
                sourceName: this.sourceName,
                sourceURL: this.validateSourceURL(this.sourceURL),
            }
            this.action(value)
            this.resetFormFields()
        },
        doCancel() {
            this.resetFormFields()
            this.cancel()
        },
        resetFormFields() {
            this.collaborators = []
            this.viewers = []
            this.name = ""
            this.eraFrom = null
            this.eraTo = null
            this.tagset = ""
            this.sourceName = ""
            this.sourceURL = ""
            this.public = false
            this.dataset = false
        },
        validateCorpusName(name: string) {
            return name.toString().match(RegExp("^.{3,100}$"))
        },
        validateSourceURL(url: string): string {
            if (!url) return url
            try {
                new URL(url)
            } catch (error) {
                // try to fix it by adding a protocol
                url = "http://" + url
            }
            return url
        }
    },
    watch: {
        item: {
            handler(newValue: MutableCorpusMetadata) {
                if (!(newValue as MutableCorpusMetadata)) return
                this.name = newValue.name
                this.collaborators = [...newValue.collaborators]
                this.viewers = [...newValue.viewers]
                this.eraFrom = newValue.eraFrom
                this.eraTo = newValue.eraTo
                this.tagset = newValue.tagset
                this.sourceName = newValue.sourceName
                this.sourceURL = newValue.sourceURL
                this.public = newValue.public
                this.dataset = newValue.dataset
            }, immediate: true,
            deep: true
        },
        dataset() {
            // datasets are always public
            if (this.dataset) this.public = true
        },
        public() {
            // datasets can't be private
            if (!this.public) this.dataset = false
        }
    }
});
</script>

<style scoped lang="scss">
.warning {
    color: var(--int-red);
}

.borderRow {
    border-top: 1px solid var(--int-grey);
}

:deep(table) {
    border-collapse: collapse;

    td {
        padding: 3px 10px;
    }
}

:deep(hr) {
    margin: 10px 0;
    border: 1px dotted var(--int-grey);
}

:deep(.checkbox-container) {
    margin-bottom: 0;
}
</style>