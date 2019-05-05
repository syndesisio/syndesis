import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  Container,
  IntegrationDetailActivity,
  IntegrationDetailActivityItem,
  IntegrationDetailActivityItemSteps,
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
  IntegrationDetailInfo,
  IntegrationDetailMetrics,
  IntegrationStepsHorizontalItem,
  IntegrationStepsHorizontalView,
  TabBar,
  TabBarItem,
} from '../../src';

const datePublished = Date.parse('24 Feb 2019 08:19:42 GMT');

const activityItemsSteps1 = [
  <IntegrationDetailActivityItemSteps
    key={0}
    duration={'4'}
    name={'Data Mapper'}
    output={'No output'}
    status={'Success'}
    time={'Mar 14, 2019, 14:24:29'}
  />,
  <IntegrationDetailActivityItemSteps
    key={1}
    duration={'4'}
    name={'Invoke stored procedure'}
    output={'No output'}
    status={'Success'}
    time={'Mar 14, 2019, 14:24:29'}
  />,
];

const activityItemsSteps2 = [
  <IntegrationDetailActivityItemSteps
    key={0}
    duration={'67'}
    name={'Invoke stored procedure'}
    output={
      'io.atlasmap.api.AtlasException: java.lang.IllegalArgumentException: document cannot be null nor empty'
    }
    status={'Error'}
    time={'Mar 14, 2019, 14:23:35'}
  />,
];

const activityItems = [
  <IntegrationDetailActivityItem
    steps={activityItemsSteps1}
    date={'4/16/2019'}
    errorCount={0}
    i18nErrorsFound={'Errors found'}
    i18nHeaderDuration={'Duration'}
    i18nHeaderDurationUnit={'ms'}
    i18nHeaderOutput={'Output'}
    i18nHeaderStatus={'Status'}
    i18nHeaderStep={'Step'}
    i18nHeaderTime={'Time'}
    i18nNoErrors={'No errors'}
    i18nNoSteps={'No steps information was found for this integration'}
    i18nVersion={'Version'}
    key={0}
    time={'07:40:28'}
    version={'2'}
  />,
  <IntegrationDetailActivityItem
    steps={activityItemsSteps2}
    date={'4/14/2019'}
    errorCount={5}
    i18nErrorsFound={'Errors found'}
    i18nHeaderDuration={'Duration'}
    i18nHeaderDurationUnit={'ms'}
    i18nHeaderOutput={'Output'}
    i18nHeaderStatus={'Status'}
    i18nHeaderStep={'Step'}
    i18nHeaderTime={'Time'}
    i18nNoErrors={'No errors'}
    i18nNoSteps={'No steps information was found for this integration'}
    i18nVersion={'Version'}
    key={1}
    time={'07:40:28'}
    version={'2'}
  />,
];

storiesOf('Integration/Detail', module)
  .add('Details Tab Page', () => (
    <Router>
      <>
        <Container
          style={{
            background: '#fff',
          }}
        >
          <IntegrationDetailInfo
            name={'Integration name'}
            version={1}
            currentState={'Published'}
            i18nLogUrlText={'log url'}
            i18nProgressPending={'progress pending'}
            i18nProgressStarting={'progress starting'}
            i18nProgressStopping={'progress stopping'}
            targetState={'Published'}
          />
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
        </Container>
        <IntegrationStepsHorizontalView
          children={[
            {
              icon:
                'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxOTUuMiAzMDAiPjxzdHlsZT48L3N0eWxlPjxnIGlkPSJMYXllcl8yIj48bGluZWFyR3JhZGllbnQgaWQ9IlNWR0lEXzFfIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgeDE9IjYyLjY0NCIgeTE9IjI2OC4yMDEiIHgyPSIzMC43MjEiIHkyPSI4Ny4xNTUiPjxzdG9wIG9mZnNldD0iLjE1NCIgc3RvcC1jb2xvcj0iI2ZmZTQwZSIvPjxzdG9wIG9mZnNldD0iLjkxIiBzdG9wLWNvbG9yPSIjZTEwZTE5Ii8+PC9saW5lYXJHcmFkaWVudD48cGF0aCBkPSJNNzkuNiA3OC41bC00NCAzOC41Uy04IDE1MS43IDEuMyAyMDUuOWMwIDAgMi45IDI5LjIgNDMuMyA2NS41IDAgMC0zMy00MS4xLTI2LjYtOTAuMlM3OS4zIDgzLjcgNzkuNiA3OC41eiIgZmlsbD0idXJsKCNTVkdJRF8xXykiLz48bGluZWFyR3JhZGllbnQgaWQ9IlNWR0lEXzJfIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgeDE9Ijg0LjI3MiIgeTE9IjI5OC4xNjciIHgyPSI4NC4yNzIiIHkyPSIwIj48c3RvcCBvZmZzZXQ9Ii4xNTQiIHN0b3AtY29sb3I9IiNmZmU0MGUiLz48c3RvcCBvZmZzZXQ9Ii4xOTciIHN0b3AtY29sb3I9IiNmZWRiMGUiLz48c3RvcCBvZmZzZXQ9Ii4yNjciIHN0b3AtY29sb3I9IiNmYWMzMTAiLz48c3RvcCBvZmZzZXQ9Ii4zNTUiIHN0b3AtY29sb3I9IiNmNTliMTIiLz48c3RvcCBvZmZzZXQ9Ii40NTkiIHN0b3AtY29sb3I9IiNlZDY0MTUiLz48c3RvcCBvZmZzZXQ9Ii41NzQiIHN0b3AtY29sb3I9IiNlMzFmMTgiLz48c3RvcCBvZmZzZXQ9Ii42IiBzdG9wLWNvbG9yPSIjZTEwZTE5Ii8+PC9saW5lYXJHcmFkaWVudD48cGF0aCBkPSJNMTEzIDBzMTIuNSA0My44LTE4LjMgNzkuOS0xMzAuNCAxMzkuMy00LjYgMjE4LjNjMCAwLTM4LTQ0LjggNy4yLTExMS43QzEzOC41IDEyNS44IDE2MS43IDg0IDExMyAweiIgZmlsbD0idXJsKCNTVkdJRF8yXykiLz48bGluZWFyR3JhZGllbnQgaWQ9IlNWR0lEXzNfIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgeDE9IjE0MC41MzUiIHkxPSIzMDAiIHgyPSIxNDAuNTM1IiB5Mj0iODUuMjc2Ij48c3RvcCBvZmZzZXQ9Ii4xNTQiIHN0b3AtY29sb3I9IiNmZmU0MGUiLz48c3RvcCBvZmZzZXQ9Ii4yMSIgc3RvcC1jb2xvcj0iI2ZlZGIwZSIvPjxzdG9wIG9mZnNldD0iLjMiIHN0b3AtY29sb3I9IiNmYWMzMTAiLz48c3RvcCBvZmZzZXQ9Ii40MTUiIHN0b3AtY29sb3I9IiNmNTliMTIiLz48c3RvcCBvZmZzZXQ9Ii41NDkiIHN0b3AtY29sb3I9IiNlZDY0MTUiLz48c3RvcCBvZmZzZXQ9Ii42OTciIHN0b3AtY29sb3I9IiNlMzFmMTgiLz48c3RvcCBvZmZzZXQ9Ii43MzEiIHN0b3AtY29sb3I9IiNlMTBlMTkiLz48L2xpbmVhckdyYWRpZW50PjxwYXRoIGQ9Ik0xNDkuNyA4NS4zczYuMSAzMi43LTI1LjMgODQuMS01NC45IDYxLjktMjQuMSAxMzAuNmMwIDAgODMuMSA1LjggOTUtOTMuNCAwIDAtOS4zIDIxLjUtMzEuMSAzNC43LS4xLS4xIDQwLjctNjkuNC0xNC41LTE1NnoiIGZpbGw9InVybCgjU1ZHSURfM18pIi8+PGxpbmVhckdyYWRpZW50IGlkPSJTVkdJRF80XyIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiIHgxPSIxMjMuNTE2IiB5MT0iMjkzLjg0MSIgeDI9IjEyMy41MTYiIHkyPSIxNzEuMjY5Ij48c3RvcCBvZmZzZXQ9Ii4wNzMiIHN0b3AtY29sb3I9IiNmZmY4ZGEiLz48c3RvcCBvZmZzZXQ9Ii41MzYiIHN0b3AtY29sb3I9IiNmZmVlNmYiLz48c3RvcCBvZmZzZXQ9IjEiIHN0b3AtY29sb3I9IiNmZmU0MDAiLz48L2xpbmVhckdyYWRpZW50PjxwYXRoIGQ9Ik0xMDMuMyAyOTMuOHMtMTguOS0zNS4zIDYuMS02Ny43IDI3LjMtMjUuNyAzOS44LTU0LjljMCAwIDEwLjMgNTAuNy0yMy4xIDEwMC4xIDAgMCAxMS4zLTEuNiAyNC42LTkuOS0uMSAwLTEzLjQgMjkuNi00Ny40IDMyLjR6IiBmaWxsPSJ1cmwoI1NWR0lEXzRfKSIvPjxsaW5lYXJHcmFkaWVudCBpZD0iU1ZHSURfNV8iIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIiB4MT0iODUuODI3IiB5MT0iMjg0LjIxNCIgeDI9Ijg1LjgyNyIgeTI9IjYwLjU2OSI+PHN0b3Agb2Zmc2V0PSIuMTI1IiBzdG9wLWNvbG9yPSIjZmZmOGRhIi8+PHN0b3Agb2Zmc2V0PSIuNTExIiBzdG9wLWNvbG9yPSIjZmZlNDAwIi8+PHN0b3Agb2Zmc2V0PSIuNTkzIiBzdG9wLWNvbG9yPSIjZmJjZTAzIi8+PHN0b3Agb2Zmc2V0PSIuNzY0IiBzdG9wLWNvbG9yPSIjZjI5NTBjIi8+PHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjZTMzZTE5Ii8+PC9saW5lYXJHcmFkaWVudD48cGF0aCBkPSJNNzkuMyAyODQuMnMtMjEuOC0yMi4xLTkuMy03My44IDU2LjYtNDYuMiA1My4zLTE0OS44YzAgMC05IDQ2LjItMzcuNiA2OC4zLTI4LjUgMjIuMi02NC44IDk1LTYuNCAxNTUuM3oiIGZpbGw9InVybCgjU1ZHSURfNV8pIi8+PGxpbmVhckdyYWRpZW50IGlkPSJTVkdJRF82XyIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiIHgxPSI5My42OTMiIHkxPSIyNjkuMjI5IiB4Mj0iOTMuNjkzIiB5Mj0iMi45NDgiPjxzdG9wIG9mZnNldD0iLjA3MyIgc3RvcC1jb2xvcj0iI2ZmZjhkYSIvPjxzdG9wIG9mZnNldD0iLjI0OCIgc3RvcC1jb2xvcj0iI2ZmZjNhMCIvPjxzdG9wIG9mZnNldD0iLjQ0IiBzdG9wLWNvbG9yPSIjZmZlZDY3Ii8+PHN0b3Agb2Zmc2V0PSIuNjE5IiBzdG9wLWNvbG9yPSIjZmZlOTNiIi8+PHN0b3Agb2Zmc2V0PSIuNzc3IiBzdG9wLWNvbG9yPSIjZmZlNjFiIi8+PHN0b3Agb2Zmc2V0PSIuOTA5IiBzdG9wLWNvbG9yPSIjZmZlNTA3Ii8+PHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjZmZlNDAwIi8+PC9saW5lYXJHcmFkaWVudD48cGF0aCBkPSJNMTM3LjcgMi45czEzLjggMzguMi0yMS44IDkzLjEtODQgOTItNDguMSAxNzMuMmMwIDAtMzUuNC00NS41LTEzLjktOTcuNSAyNi43LTY0LjEgOTYuMi05Ny41IDgzLjgtMTY4Ljh6IiBmaWxsPSJ1cmwoI1NWR0lEXzZfKSIvPjwvZz48L3N2Zz4=',
              name: 'Fhir Test',
            },
            {
              icon:
                'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNDYgMzAwIj48c3R5bGU+LnN0MTB7ZmlsbDojMjMxZjIwfS5zdDYye2ZpbGw6I2QxZDNkNH08L3N0eWxlPjxnIGlkPSJMYXllcl8xIj48cGF0aCBjbGFzcz0ic3QxMCIgZD0iTTE1Mi4xIDE2Ni4ySDI0LjdjLTIuMSAwLTMuNy0xLjctMy43LTMuNyAwLTIuMSAxLjctMy43IDMuNy0zLjdoMTI3LjRjMi4xIDAgMy43IDEuNyAzLjcgMy43cy0xLjcgMy43LTMuNyAzLjd6TTE1Mi43IDE5NC4xaC0xMjhjLTIuMSAwLTMuNy0xLjctMy43LTMuNyAwLTIuMSAxLjctMy43IDMuNy0zLjdoMTI4YzIuMSAwIDMuNyAxLjcgMy43IDMuN3MtMS42IDMuNy0zLjcgMy43eiIvPjxwYXRoIGQ9Ik0zMS4xIDE2LjJjLjkgNy4xIDQuNiAxNCA1LjYgMTUuOC45IDEuNiAyLjYgMi41IDQuMyAyLjUuOCAwIDEuNy0uMiAyLjQtLjcgMi40LTEuNCAzLjItNC40IDEuOS02LjgtMS42LTIuOC0zLjQtNy4yLTQuMi0xMC44aDExM2MuOSA3LjEgNC42IDE0IDUuNiAxNS44LjkgMS42IDIuNiAyLjUgNC4zIDIuNS44IDAgMS43LS4yIDIuNC0uNyAyLjQtMS40IDMuMi00LjQgMS45LTYuOC0xLjYtMi44LTMuNC03LjItNC4yLTEwLjhoMzMuNVY0NEg3LjRWMTYuMmgyMy43eiIgZmlsbD0iIzAwYWVlZiIvPjxwYXRoIGNsYXNzPSJzdDEwIiBkPSJNMzYuOCA0MC42YzEuNC41IDIuOC44IDQuMi44IDEuNyAwIDMuMy0uNCA0LjktMS4xIDIuOS0xLjMgNS4xLTMuNyA2LjItNi43IDEuMS0zIDEtNi4yLS4zLTkuMS0uOC0xLjctMi0zLjItMy42LTQuNS0xLjEtLjgtMi42LS43LTMuNS40LS44IDEuMS0uNyAyLjYuNCAzLjUgMSAuOCAxLjcgMS42IDIuMSAyLjYuOCAxLjcuOCAzLjYuMiA1LjMtLjcgMS43LTIgMy4xLTMuNiAzLjktMS43LjgtMy42LjgtNS4zLjItMS43LS43LTMuMS0yLTMuOS0zLjctLjUtMS0uNi0yLjEtLjYtMy4zLjEtMS40LTEtMi41LTIuMy0yLjYtMS40LS4xLTIuNSAxLTIuNiAyLjMtLjEgMiAuMiAzLjkgMSA1LjYgMS4zIDMuMSAzLjcgNS4zIDYuNyA2LjR6TTE1OS44IDQwLjZjMS40LjUgMi44LjggNC4yLjggMS43IDAgMy4zLS40IDQuOS0xLjEgMi45LTEuMyA1LjEtMy43IDYuMi02LjcgMS4xLTMgMS02LjItLjMtOS4xLS44LTEuNy0yLTMuMi0zLjYtNC41LTEuMS0uOC0yLjYtLjctMy41LjQtLjggMS4xLS42IDIuNi40IDMuNSAxIC43IDEuNyAxLjYgMi4xIDIuNi44IDEuNy44IDMuNi4yIDUuMy0uNyAxLjctMiAzLjEtMy42IDMuOS0xLjcuOC0zLjYuOC01LjMuMi0xLjctLjctMy4xLTItMy45LTMuNy0uNS0xLS42LTIuMS0uNi0zLjMuMS0xLjQtMS0yLjUtMi4zLTIuNi0xLjQtLjEtMi41IDEtMi42IDIuMy0uMSAyIC4yIDMuOSAxIDUuNyAxLjQgMyAzLjggNS4yIDYuNyA2LjN6Ii8+PHBhdGggY2xhc3M9InN0MTAiIGQ9Ik0yNDQuNSAxOC4xTDIxOC4zIDYuOGMtLjYtLjMtMS4zLS4zLTEuOSAwLS42LjItMS4xLjctMS40IDEuM2wtMy45IDktNy4xLTcuM2MtLjMtLjMtLjgtLjYtMS4yLS44LS41LS4yLS45LS4zLTEuNC0uM2gtMjYuOWMtLjYtMi4yLTItNS44LTUuMS03LjYtMi4xLTEuMi00LjUtMS40LTYuOS0uNy0zLjYgMS4yLTYuMyAzLjctNy42IDcuMi0uMS40LS4yLjctLjMgMS4xSDUxLjRjLS42LTIuMi0yLTUuOC01LjEtNy42LTIuMS0xLjItNC41LTEuNC02LjktLjctMy42IDEuMi02LjMgMy43LTcuNiA3LjItLjEuNC0uMi43LS4zIDEuMUgzLjdjLTIgMC0zLjcgMS43LTMuNyAzLjd2MjcyLjhjMCAuNS4xLjkuMyAxLjQuMi41LjUuOS44IDEuMmwxMSAxMWMuNy43IDEuNiAxLjEgMi42IDEuMWgxOTcuNmMuNSAwIDEtLjEgMS40LS4zLjQtLjIuNy0uNCAxLS43LjEgMCAuMS0uMS4yLS4xIDAgMCAuMS0uMS4xLS4yLjMtLjMuNS0uNy43LTEuMS4yLS41LjMtLjkuMy0xLjRWOTAuNGwyOS43LTY5LjFjLjYtMS4yLjEtMi43LTEuMi0zLjJ6bS03Mi40IDg5LjhIMjQuN2MtMi4xIDAtMy43IDEuNy0zLjcgMy43IDAgMi4xIDEuNyAzLjcgMy43IDMuN2gxNDQuMmwtNy42IDE3LjZjLS4zLS4xLS43LS4yLTEtLjJIMjQuN2MtMi4xIDAtMy43IDEuNy0zLjcgMy43IDAgMi4xIDEuNyAzLjcgMy43IDMuN2gxMzMuNmwtOC44IDIwLjVjLS4yLjMtLjIuNy0uMiAxLjF2LjFsMS44IDMwYy4xLjkuNiAxLjcgMS40IDIuMS4zLjIuNy4yIDEgLjIuNSAwIDEuMS0uMiAxLjUtLjVsMjQuNC0xOC44czAtLjEuMS0uMWMuMy0uMi41LS41LjctLjlsMTcuNC00MC41VjI1M2MtLjUuNi0xIDEuMi0xLjcgMS43LTQuNiAzLjItMTQuMyAzLjMtMjIuOS4yLS44LS4zLTEuNi0uMi0yLjMuMy0uNy41LTEuMSAxLjItMS4xIDIgMCAxMC03LjggMjEuNC0xOC41IDI0LjNINy40di0yMzBoMTg5bC0yNC4zIDU2LjR6bS0xNy43IDU3LjZsMTcuMSA3LjQgMS41LjYtNyA1LjQtMTAuOC45LS44LTE0LjN6bTQzLjIgOTguOXYxNy4ySDE3MGwyNy42LTE3LjJ6bS0zMiAxNC4xYzQuOC01IDgtMTEuNSA4LjktMTcuOSA2IDEuNyAxMyAyLjMgMTguNy44bC0yNy42IDE3LjF6bTM5LjUtMjU3LjFsMyAzLTMgNi45di05Ljl6bS0xNzQtNS4yYy45IDcuMSA0LjYgMTQgNS42IDE1LjguOSAxLjYgMi42IDIuNSA0LjMgMi41LjggMCAxLjctLjIgMi40LS43IDIuNC0xLjQgMy4yLTQuNCAxLjktNi44LTEuNi0yLjgtMy40LTcuMi00LjItMTAuOGgxMTNjLjkgNy4xIDQuNiAxNCA1LjYgMTUuOC45IDEuNiAyLjYgMi41IDQuMyAyLjUuOCAwIDEuNy0uMiAyLjQtLjcgMi40LTEuNCAzLjItNC40IDEuOS02LjgtMS42LTIuOC0zLjQtNy4yLTQuMi0xMC44aDMzLjVWNDRINy40VjE2LjJoMjMuN3pNMTIuNyAyODloMTg3LjFsMy42IDMuNkgxNi4zbC0zLjYtMy42em0xOTYtMS43bC0zLjYtMy42VjExNmwzLjYtOC4zdjE3OS42em0tMzIuMS0xMTcuN2wtMTMuMS01LjYtOC41LTMuNyAxOS44LTQ2Yy42LS42IDEtMS41IDEuMS0yLjVMMjE1IDIwLjlsMy43LTguNSAyMS42IDkuMy02My43IDE0Ny45eiIvPjxwYXRoIGNsYXNzPSJzdDEwIiBkPSJNMTc2LjYgMTY5LjZsLTEzLjEtNS42LTguNS0zLjcgMTkuOC00NmMuNi0uNiAxLTEuNSAxLjEtMi41TDIxNSAyMC45bDMuNy04LjUgMjEuNiA5LjMtNjMuNyAxNDcuOXoiLz48cGF0aCBjbGFzcz0ic3Q2MiIgZD0iTTEyLjcgMjg5aDE4Ny4xbDMuNiAzLjZIMTYuM3pNMjA4LjcgMjg3LjNsLTMuNi0zLjZWMTE2bDMuNi04LjN6Ii8+PC9nPjwvc3ZnPg==',
              name: 'Log',
            },
          ].map((s, idx) => (
            <IntegrationStepsHorizontalItem
              name={s.name}
              icon={s.icon}
              isFirst={idx === 0}
            />
          ))}
        />
        <IntegrationDetailDescription description={'This is my description.'} />
        <IntegrationDetailHistoryListView
          hasHistory={true}
          isDraft={false}
          i18nTextDraft={'Draft'}
          i18nTextHistory={'History'}
          children={[
            {
              actions: {},
              updatedAt: datePublished,
              version: 2,
            },
            {
              actions: {},
              updatedAt: datePublished,
              version: 3,
            },
          ].map((deployment, idx) => (
            <IntegrationDetailHistoryListViewItem
              key={idx}
              actions={action('onActionClicked')}
              currentState={'Published'}
              i18nTextLastPublished={'Last published on '}
              i18nTextVersion={'Version'}
              updatedAt={'' + deployment.updatedAt}
              version={deployment.version}
            />
          ))}
        />
      </>
    </Router>
  ))
  .add('Activity Tab Page', () => (
    <Router>
      <>
        <Container
          style={{
            background: '#fff',
          }}
        >
          <IntegrationDetailInfo
            name={'Integration name'}
            version={1}
            currentState={'Published'}
            i18nLogUrlText={'log url'}
            i18nProgressPending={'progress pending'}
            i18nProgressStarting={'progress starting'}
            i18nProgressStopping={'progress stopping'}
            targetState={'Published'}
          />
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
        </Container>
        <IntegrationDetailActivity
          i18nBtnRefresh={'Refresh'}
          i18nLastRefresh={'Last refresh'}
          i18nViewLogOpenShift={'View Log in OpenShift'}
          linkToOpenShiftLog={'/link'}
          children={activityItems}
          onRefresh={action('onRefresh')}
        />
      </>
    </Router>
  ))
  .add('Metrics Tab Page', () => (
    <Router>
      <>
        <Container
          style={{
            background: '#fff',
          }}
        >
          <IntegrationDetailInfo
            name={'Integration name'}
            version={1}
            currentState={'Published'}
            i18nLogUrlText={'log url'}
            i18nProgressPending={'progress pending'}
            i18nProgressStarting={'progress starting'}
            i18nProgressStopping={'progress stopping'}
            targetState={'Published'}
          />
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
        </Container>
        <IntegrationDetailMetrics
          i18nLastProcessed={'Last Processed'}
          i18nSince={'Since '}
          i18nTotalErrors={'Total Errors'}
          i18nTotalMessages={'Total Messages'}
          i18nUptime={'Uptime'}
          errors={2}
          lastProcessed={'2 May 2019 08:19:42 GMT'}
          messages={26126}
          start={2323342333}
        />
      </>
    </Router>
  ));
