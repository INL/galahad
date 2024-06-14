import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '@/views/HomeView.vue'

import AnnotateView from '@/views/annotate/AnnotateView.vue'

import CorporaView from '@/views/annotate/subviews/CorporaView.vue'
import DocumentsView from '@/views/annotate/subviews/DocumentsView.vue'
import JobsView from '@/views/annotate/subviews/JobsView.vue'

import EvaluateView from '@/views/annotate/subviews/evaluate/EvaluateView.vue'
import DistributionView from '@/views/annotate/subviews/evaluate/subviews/DistributionView.vue'
import GlobalMetricsView from '@/views/annotate/subviews/evaluate/subviews/GlobalMetricsView.vue'
import GroupedMetricsView from '@/views/annotate/subviews/evaluate/subviews/GroupedMetricsView.vue'
import ConfusionView from '@/views/annotate/subviews/evaluate/subviews/ConfusionView.vue'

import ExportView from '@/views/annotate/subviews/ExportView.vue'

import ApplicationView from '@/views/application/ApplicationView.vue'
import AboutView from '@/views/application/subviews/AboutView.vue'

import OverviewView from '@/views/overview/OverviewView.vue'
import TaggersView from '@/views/overview/subviews/TaggersView.vue'
import TagsetsView from '@/views/overview/subviews/TagsetsView.vue'
import DatasetsView from '@/views/overview/subviews/DatasetsView.vue'
import BenchmarksView from '@/views/overview/subviews/BenchmarksView.vue'

import ContributeView from '@/views/contribute/ContributeView.vue'
import ContributeTaggersView from '@/views/contribute/subviews/ContributeTaggersView.vue'
import ContributeDatasetsView from '@/views/contribute/subviews/ContributeDatasetsView.vue'

import HelpView from '@/views/help/HelpView.vue'
import GeneralView from '@/views/help/subviews/GeneralView.vue'
import DocumentFormatsView from '@/views/help/subviews/formats/DocumentFormatsView.vue'
import GlossaryView from '@/views/help/subviews/GlossaryView.vue'

import UserView from '@/views/UserView.vue'
import PageNotFound from '@/views/PageNotFound.vue'

export type RouterQuery = { corpus?: string, referenceJob?: string, hypothesisJob?: string }

const routes = [
  { path: '/:pathMatch(.*)*', component: PageNotFound },
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: HomeView
  }, {
    path: '/annotate',
    name: 'Annotate',
    redirect: '/annotate/corpora',
    component: AnnotateView,
    children: [
      {
        path: 'corpora',
        name: 'Corpora',
        component: CorporaView,
      },
      {
        path: 'documents',
        name: 'Documents',
        component: DocumentsView,
      },
      {
        path: 'jobs',
        name: 'Jobs',
        component: JobsView
      },
      {
        path: 'evaluate',
        name: 'Evaluate',
        component: EvaluateView,
        redirect: '/annotate/evaluate/distribution',
        props: { basePath: "/annotate/evaluate" },
        children: [
          {
            path: 'distribution',
            component: DistributionView
          },
          {
            path: 'global_metrics',
            component: GlobalMetricsView,
          },
          {
            path: 'grouped_metrics',
            component: GroupedMetricsView,
          },
          {
            path: 'confusion',
            component: ConfusionView
          }
        ]
      },
      {
        path: 'export',
        name: 'Export',
        component: ExportView
      }
    ]
  }, {
    path: '/application',
    name: 'Application',
    component: ApplicationView,
    children: [
      { path: 'about', component: AboutView },
    ]
  }, {
    path: '/overview',
    name: 'Overview',
    redirect: '/overview/taggers',
    component: OverviewView,
    children: [
      { path: 'taggers', component: TaggersView },
      { path: 'tagsets', component: TagsetsView },
      { path: 'datasets', component: DatasetsView },
      { path: 'benchmarks', component: BenchmarksView },
    ]
  }, {
    path: '/contribute', component: ContributeView, children: [
      { path: 'taggers', component: ContributeTaggersView },
      { path: 'datasets', component: ContributeDatasetsView }
    ]
  }, {
    path: '/help',
    name: 'Help',
    redirect: '/help/general',
    component: HelpView,
    children: [
      {
        path: 'general',
        component: GeneralView
      },
      {
        path: 'formats',
        component: DocumentFormatsView
      },
      {
        path: 'glossary',
        component: GlossaryView
      },
    ]
  }, {
    path: '/user',
    component: UserView
  },
]

const router = createRouter({
  history: createWebHistory('/galahad/'), //import.meta.env.BASE_URL ),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (to && to.hash) {
      return {
        el: to.hash,
        top: 200 // avoid the top bar
        // behavior: 'smooth'
      };
    } else if (savedPosition) {
      return savedPosition;
    } else if (from.path === to.path) {
      return // since we're on the same page, don't scroll to top
    } else {
      return { top: 0 };
    }
  }
})

export default router
