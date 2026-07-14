import { createRouter, createWebHistory, type RouterScrollBehavior } from "vue-router"

import HomeView from "@/views/HomeView.vue"
import AnnotateView from "@/views/annotate/AnnotateView.vue"
import CorporaView from "@/views/annotate/subviews/CorporaView.vue"
import DocumentsView from "@/views/annotate/subviews/DocumentsView.vue"
import JobsView from "@/views/annotate/subviews/JobsView.vue"
import EvaluateView from "@/views/annotate/subviews/evaluate/EvaluateView.vue"
import ConfusionView from "@/views/annotate/subviews/evaluate/subviews/ConfusionView.vue"
import DistributionView from "@/views/annotate/subviews/evaluate/subviews/DistributionView.vue"
import DocumentLayerComparisonView from "@/views/annotate/subviews/evaluate/subviews/DocumentLayerComparisonView.vue"
import EntitiesView from "@/views/annotate/subviews/evaluate/subviews/EntitiesView.vue"
import GlobalMetricsView from "@/views/annotate/subviews/evaluate/subviews/GlobalMetricsView.vue"
import GroupedMetricsView from "@/views/annotate/subviews/evaluate/subviews/GroupedMetricsView.vue"
import ExportView from "@/views/annotate/subviews/ExportView.vue"
import ApplicationView from "@/views/application/ApplicationView.vue"
import AboutView from "@/views/application/subviews/AboutView.vue"
import ContributeView from "@/views/application/subviews/ContributeView.vue"
import OverviewView from "@/views/overview/OverviewView.vue"
import BenchmarksView from "@/views/overview/subviews/BenchmarksView.vue"
import DatasetsView from "@/views/overview/subviews/DatasetsView.vue"
import TaggersView from "@/views/overview/subviews/TaggersView.vue"
import PrinciplesView from "@/views/overview/subviews/PrinciplesView.vue"
import HelpView from "@/views/help/HelpView.vue"
import EvaluationView from "@/views/help/subviews/EvaluationView.vue"
import GeneralView from "@/views/help/subviews/GeneralView.vue"
import DocumentFormatsView from "@/views/help/subviews/formats/DocumentFormatsView.vue"
import PageNotFound from "@/views/PageNotFound.vue"
import UserView from "@/views/UserView.vue"

export type RouterQuery = { corpus?: string; referenceLayer?: string; hypothesisLayer?: string }

const routes = [
    { path: "/:pathMatch(.*)*", component: PageNotFound },
    { path: "/", component: HomeView },
    {
        path: "/annotate",
        redirect: "/annotate/corpora",
        component: AnnotateView,
        children: [
            { meta: { title: "Corpora" }, path: "corpora", component: CorporaView },
            { meta: { title: "Documents" }, path: "documents", component: DocumentsView },
            { meta: { title: "Jobs" }, path: "jobs", component: JobsView },
            {
                path: "evaluate",
                component: EvaluateView,
                redirect: "/annotate/evaluate/distribution",
                children: [
                    {
                        meta: { title: "Evaluate" },
                        title: "Distribution",
                        path: "distribution",
                        component: DistributionView,
                    },
                    {
                        meta: { title: "Evaluate" },
                        title: "Distribution",
                        path: "global_metrics",
                        component: GlobalMetricsView,
                    },
                    { meta: { title: "Evaluate" }, path: "grouped_metrics", component: GroupedMetricsView },
                    { meta: { title: "Evaluate" }, path: "confusion", component: ConfusionView },
                    { meta: { title: "Evaluate" }, path: "document", component: DocumentLayerComparisonView },
                    { meta: { title: "Evaluate" }, path: "entities", component: EntitiesView },
                ],
            },
            { meta: { title: "Export" }, path: "export", component: ExportView },
        ],
    },
    {
        path: "/application",
        name: "Application",
        redirect: "/application/about",
        component: ApplicationView,
        children: [
            { meta: { title: "About" }, path: "about", component: AboutView },
            { meta: { title: "Contribute" }, path: "contribute", component: ContributeView },
        ],
    },
    {
        path: "/overview",
        name: "Overview",
        redirect: "/overview/taggers",
        component: OverviewView,
        children: [
            { meta: { title: "Taggers" }, path: "taggers", component: TaggersView },
            { meta: { title: "Principles" }, path: "principles", component: PrinciplesView },
            { meta: { title: "Datasets" }, path: "datasets", component: DatasetsView },
            { meta: { title: "Benchmarks" }, path: "benchmarks", component: BenchmarksView },
        ],
    },
    {
        path: "/help",
        name: "Help",
        redirect: "/help/general",
        component: HelpView,
        children: [
            { meta: { title: "Help - General" }, path: "general", component: GeneralView },
            { meta: { title: "Help - Formats" }, path: "formats", component: DocumentFormatsView },
            { meta: { title: "Help - Evaluation" }, path: "evaluation", component: EvaluationView },
        ],
    },
    { meta: { title: "User" }, path: "/user", component: UserView },
]

const router = createRouter({
    history: createWebHistory("/galahad/"), //import.meta.env.BASE_URL ),
    routes,
    scrollBehavior(to, from, savedPosition): any {
        if (to?.hash) {
            return {
                el: to.hash,
                top: 10, // avoid the top bar
            }
        }
        if (savedPosition) {
            return savedPosition
        }
        if (from.path === to.path) {
            return // since we're on the same page, don't scroll to top
        }
        return { top: 0 }
    },
})

// Whenever we navigate, change the document title to the name of the route
router.afterEach((to) => {
    if (to.meta.title) {
        document.title = `GaLAHaD - ${to.meta.title}`
    } else {
        document.title = "GaLAHaD"
    }
})

export default router
