import GCard from "./GCard.vue"
import GInfo from "./GInfo.vue"
import GSpinner from "./GSpinner.vue"
import GTabs from "./GTabs.vue"
import KeyValues from "./KeyValues.vue"
import AnnotateTab from "./AnnotateTab.vue"
import EvaluationInfoBox from "./EvaluationInfoBox.vue"
// Tables
import GTable from "./tables/GTable.vue"
import CorpusTable from "./tables/CorpusTable.vue"
import DocumentsTable from "./tables/DocumentsTable.vue"
import RightFloatCell from "./tables/RightFloatCell.vue"
// Links
import ContributeTaggerLink from "./links/ContributeTaggerLink.vue"
import ExternalLink from "./links/ExternalLink.vue"
import MailAddress from "./links/MailAddress.vue"
import GNav from "./links/GNav.vue"
import HelpLink from "./links/HelpLink.vue"
import GlossaryLink from "./links/GlossaryLink.vue"
// Inputs
import JobSelect from "./input/JobSelect.vue"
import FileFormatInput from "./input/FileFormatInput.vue"
import GInput from "./input/GInput.vue"
import GButton from "./input/GButton.vue"
import DownloadButton from './input/DownloadButton.vue'
import InspectButton from './input/InspectButton.vue'
// Modals
import GModal from "./modals/GModal.vue"
import VariantsModal from "./modals/VariantsModal.vue"
import DeleteModal from "./modals/DeleteModal.vue"
import ComparisonModal from "./modals/ComparisonModal.vue"
import CorpusForm from "./modals/corpus/CorpusForm.vue"
import JobModal from "./modals/jobs/JobModal.vue"

export {
    GCard,
    GInfo,
    GSpinner,
    GTabs,
    KeyValues,
    AnnotateTab,
    EvaluationInfoBox,
    // Tables
    GTable,
    DocumentsTable,
    CorpusTable,
    RightFloatCell,
    // Links
    ExternalLink,
    GNav,
    GlossaryLink,
    HelpLink,
    MailAddress,
    ContributeTaggerLink,
    // Inputs
    GInput,
    FileFormatInput,
    GButton,
    DownloadButton,
    InspectButton,
    JobSelect,
    // Modals
    GModal,
    VariantsModal,
    DeleteModal,
    ComparisonModal,
    CorpusForm,
    JobModal,
}

// I do not recommend using global components like below, since there is no type completion
// better to explicitly import components from the object above
// However, nested components can be tricky
// If you want type completion, just make sure you explicitly import the components

export default {
    install: (app, options) => {
        //     /* declare global component */
        app.component("GCard", GCard)
        app.component("GInfo", GInfo)
        app.component("GSpinner", GSpinner)
        app.component("GTabs", GTabs)
        app.component("KeyValues", KeyValues)
        app.component("AnnotateTab", AnnotateTab)
        app.component("EvaluationInfoBox", EvaluationInfoBox)
        // Tables
        app.component("CorpusTable", CorpusTable)
        app.component("DocumentsTable", DocumentsTable)
        app.component("GTable", GTable)
        app.component("RightFloatCell", RightFloatCell)
        // Links
        app.component("ContributeTaggerLink", ContributeTaggerLink)
        app.component("ExternalLink", ExternalLink)
        app.component("GlossaryLink", GlossaryLink)
        app.component("GNav", GNav)
        app.component("MailAddress", MailAddress)
        app.component("HelpLink", HelpLink)
        // Inputs
        app.component("FileFormatInput", FileFormatInput)
        app.component("GInput", GInput)
        app.component("GButton", GButton)
        app.component("DownloadButton", DownloadButton)
        app.component("InspectButton", InspectButton)
        app.component("JobSelect", JobSelect)
        // Modals
        app.component("GModal", GModal)
        app.component("VariantsModal", VariantsModal)
        app.component("DeleteModal", DeleteModal)
        app.component("ComparisonModal", ComparisonModal)
        app.component("CorpusForm", CorpusForm)
        app.component("JobModal", JobModal)
    }
}