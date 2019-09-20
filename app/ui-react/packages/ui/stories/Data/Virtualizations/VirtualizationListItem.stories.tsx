import { action } from '@storybook/addon-actions';
import { select, text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { VirtualizationListItem } from '../../../src';

const stories = storiesOf(
  'Data/Virtualizations/VirtualizationListItem',
  module
);

const virtualizationName = 'Virtualization_1';
const virtualizationDescription = 'Virtualization_1 description ...';
const virtualizationIconData =
  'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4NCjwhLS0gR2VuZXJhdG9yOiBBZG9iZSBJbGx1c3RyYXRvciAxOS4xLjAsIFNWRyBFeHBvcnQgUGx1Zy1JbiAuIFNWRyBWZXJzaW9uOiA2LjAwIEJ1aWxkIDApICAtLT4NCjxzdmcgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB4PSIwcHgiIHk9IjBweCINCgkgdmlld0JveD0iMCAwIDIyNy4yIDMwMCIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgMjI3LjIgMzAwOyIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSI+DQo8c3R5bGUgdHlwZT0idGV4dC9jc3MiPg0KCS5zdDB7ZmlsbDojODMyRUFCO30NCgkuc3Qxe2ZpbGw6I0ZFQ0QwMDt9DQoJLnN0MntmaWxsOiMwMEFFRUY7fQ0KCS5zdDN7ZmlsbDpub25lO30NCgkuc3Q0e2ZpbGw6IzhEQzYzRjt9DQoJLnN0NXtmaWxsOiMwRjlCRDc7fQ0KCS5zdDZ7ZmlsbDojRkZGRkZGO30NCgkuc3Q3e2ZpbGwtcnVsZTpldmVub2RkO2NsaXAtcnVsZTpldmVub2RkO2ZpbGw6I0ZGRkZGRjt9DQoJLnN0OHtmaWxsOiMxREExRjI7fQ0KCS5zdDl7ZmlsbDojM0Q1QTk4O30NCgkuc3QxMHtmaWxsOiMyMzFGMjA7fQ0KCS5zdDExe2ZpbGw6IzAwN0JCNTt9DQoJLnN0MTJ7ZmlsbDojREM0RTQxO30NCgkuc3QxM3tmaWxsLXJ1bGU6ZXZlbm9kZDtjbGlwLXJ1bGU6ZXZlbm9kZDtmaWxsOiMxODE2MTY7fQ0KCS5zdDE0e2ZpbGw6IzE4MTYxNjt9DQoJLnN0MTV7ZmlsbDojRjJGMkYyO30NCgkuc3QxNntmaWxsOiNFM0UzRTM7fQ0KCS5zdDE3e2ZpbGw6I0Q1NEIzRDt9DQoJLnN0MTh7ZmlsbDojRDcyQjI3O30NCgkuc3QxOXtvcGFjaXR5OjAuNTY7ZmlsbDp1cmwoI1NWR0lEXzFfKTt9DQoJLnN0MjB7ZmlsbDojQkFCQUJBO30NCgkuc3QyMXtmaWxsOiM5MkQ0MDA7fQ0KCS5zdDIye2ZpbGw6IzAwQjlFNDt9DQoJLnN0MjN7ZmlsbDojMkIzOTkwO30NCgkuc3QyNHtmaWxsOiMzRjlDMzU7fQ0KCS5zdDI1e2ZpbGw6IzhGQ0VEODt9DQoJLnN0MjZ7ZmlsbDojRDIxRjIxO30NCgkuc3QyN3tmaWxsOiNGRkY4REE7fQ0KCS5zdDI4e2ZpbGw6I0MzMjI2NTt9DQoJLnN0Mjl7ZmlsbDojRDZENkQ2O30NCgkuc3QzMHtmaWxsOiNGMUYxRjE7fQ0KCS5zdDMxe2ZpbGw6IzhDMzEyMzt9DQoJLnN0MzJ7ZmlsbDojRTA1MjQzO30NCgkuc3QzM3tmaWxsOiM1RTFGMTg7fQ0KCS5zdDM0e2ZpbGw6I0YyQjBBOTt9DQoJLnN0MzV7ZmlsbDojQ0FDQ0NFO30NCgkuc3QzNntmaWxsOiNBMUFGREI7fQ0KCS5zdDM3e2ZpbGw6IzAwMjA4Nzt9DQoJLnN0Mzh7ZmlsbDojNkM0MTk5O30NCgkuc3QzOXtmaWxsOiNDN0M3Qzc7fQ0KCS5zdDQwe2ZpbGw6IzE5NzZEMjt9DQoJLnN0NDF7ZmlsbDojMUU4OEU1O30NCgkuc3Q0MntmaWxsOiNGQUZBRkE7fQ0KCS5zdDQze2ZpbGw6I0UwRTBFMDt9DQoJLnN0NDR7ZmlsbDojRjZBMTFGO30NCgkuc3Q0NXtmaWxsOiMwQTZEQUU7fQ0KCS5zdDQ2e2ZpbGw6IzBCQTI1RTt9DQoJLnN0NDd7ZmlsbDojMDg5MTU2O30NCgkuc3Q0OHtmaWxsOiMxMDk4NUI7fQ0KCS5zdDQ5e2ZpbGw6I0Y5Qzk0MTt9DQoJLnN0NTB7ZmlsbDojRThCODM1O30NCgkuc3Q1MXtmaWxsOiMyOTZBRDk7fQ0KCS5zdDUye2ZpbGw6IzJBNzFFOTt9DQoJLnN0NTN7ZmlsbDojMjg2RUU2O30NCgkuc3Q1NHtmaWxsOiMwMDk0NDQ7fQ0KCS5zdDU1e2ZpbGw6IzAwNjgzODt9DQoJLnN0NTZ7ZmlsbDojQkNCRUMwO30NCgkuc3Q1N3tmaWxsOiMyQkI2NzM7fQ0KCS5zdDU4e2ZpbGw6I0Y3OTQxRTt9DQoJLnN0NTl7ZmlsbDojRUY0MTM2O30NCgkuc3Q2MHtmaWxsOiNGRkZGRkY7c3Ryb2tlOiMyMzFGMjA7c3Ryb2tlLXdpZHRoOjEwO3N0cm9rZS1saW5lY2FwOnJvdW5kO3N0cm9rZS1saW5lam9pbjpyb3VuZDtzdHJva2UtbWl0ZXJsaW1pdDoxMDt9DQoJLnN0NjF7ZmlsbDojRDFEM0Q0O3N0cm9rZTojMjMxRjIwO3N0cm9rZS13aWR0aDoxMDtzdHJva2UtbGluZWNhcDpyb3VuZDtzdHJva2UtbGluZWpvaW46cm91bmQ7c3Ryb2tlLW1pdGVybGltaXQ6MTA7fQ0KCS5zdDYye2ZpbGw6IzIzMUYyMDtzdHJva2U6IzIzMUYyMDtzdHJva2Utd2lkdGg6MTA7c3Ryb2tlLWxpbmVjYXA6cm91bmQ7c3Ryb2tlLWxpbmVqb2luOnJvdW5kO3N0cm9rZS1taXRlcmxpbWl0OjEwO30NCgkuc3Q2M3tmaWxsOm5vbmU7c3Ryb2tlOiMyMzFGMjA7c3Ryb2tlLXdpZHRoOjEwO3N0cm9rZS1taXRlcmxpbWl0OjEwO30NCgkuc3Q2NHtmaWxsOm5vbmU7c3Ryb2tlOiMyMzFGMjA7c3Ryb2tlLXdpZHRoOjEwO3N0cm9rZS1saW5lam9pbjpyb3VuZDtzdHJva2UtbWl0ZXJsaW1pdDoxMDt9DQoJLnN0NjV7ZmlsbDojRTFERkU0O30NCgkuc3Q2NntmaWxsOiMzNTQ3NEY7fQ0KCS5zdDY3e2ZpbGw6I0ZDMDAyRjt9DQoJLnN0Njh7ZmlsbDojRkRENTE4O30NCgkuc3Q2OXtmaWxsOiMwMEU3Njk7fQ0KCS5zdDcwe2ZpbGw6I0QxRDNENDt9DQoJLnN0NzF7ZmlsbDojMjdBQUUxO30NCgkuc3Q3MntmaWxsOiMyMzFGMjA7c3Ryb2tlOiMyN0FBRTE7c3Ryb2tlLW1pdGVybGltaXQ6MTA7fQ0KCS5zdDcze2ZpbGw6dXJsKCNTVkdJRF8yXyk7fQ0KCS5zdDc0e2ZpbGw6dXJsKCNTVkdJRF8zXyk7fQ0KCS5zdDc1e2ZpbGw6dXJsKCNTVkdJRF80Xyk7fQ0KCS5zdDc2e2ZpbGw6dXJsKCNTVkdJRF81Xyk7fQ0KCS5zdDc3e2ZpbGw6dXJsKCNTVkdJRF82Xyk7fQ0KCS5zdDc4e2ZpbGw6dXJsKCNTVkdJRF83Xyk7fQ0KPC9zdHlsZT4NCjxnIGlkPSJMYXllcl8xIj4NCgk8Zz4NCgkJPGc+DQoJCQk8cGF0aCBjbGFzcz0ic3QxIiBkPSJNMjE3LjcsMTk0LjVjNS45LDMuNiw5LjUsNy43LDkuNSwxMi43djYzLjRjMCwyMS42LTY3LjksMjkuNS0xMTMuNiwyOS41QzY3LjksMzAwLDAsMjkyLjEsMCwyNzAuNXYtNjMuNA0KCQkJCWMwLTQuOSwzLjUtOS4xLDkuNS0xMi43Yy01LjktMy42LTkuNS03LjctOS41LTEyLjd2LTYzLjRjMC01LDMuNi05LjIsOS43LTEyLjhjLTYuMS0zLjYtOS44LTcuOS05LjgtMTIuOVYyOS41DQoJCQkJQy0wLjEsNy45LDY3LjgsMCwxMTMuNSwwYzQ1LjcsMCwxMTMuNiw3LjksMTEzLjYsMjkuNXY2My40YzAsNS0zLjYsOS4yLTkuNywxMi44YzYuMSwzLjYsOS44LDcuOSw5LjgsMTIuOXY2My40DQoJCQkJQzIyNy4yLDE4Ni43LDIyMy43LDE5MC45LDIxNy43LDE5NC41eiIvPg0KCQkJPHBhdGggY2xhc3M9InN0MjciIGQ9Ik0yMTcuNiwxOTQuN2M1LjksMy42LTg2LjEsMjcuMi0yMDguMywwYy01LjktMy42LTkuNS03LjctOS41LTEyLjd2LTYzLjRjMC01LDkuNy0xMi44LDkuNy0xMi44DQoJCQkJczEwOS42LDIwLDIwNy43LTAuMWM5LjcsMS44LDkuOCw3LjksOS44LDEyLjlWMTgyQzIyNy4xLDE4Ni45LDIyMy42LDE5MS4xLDIxNy42LDE5NC43eiIvPg0KCQkJPHBhdGggY2xhc3M9InN0NiIgZD0iTTkuNSwxMDUuNUMzLjUsMTAxLjksMCw5Ny44LDAsOTIuOFYyOS41QzAsNy45LDY3LjksMCwxMTMuNiwwYzQ1LjcsMCwxMTMuNiw3LjksMTEzLjYsMjkuNXY2My40DQoJCQkJYzAsNC45LTMuNSw5LjEtOS41LDEyLjdDMTE0LjQsMTMwLjEsMy41LDEwOS4xLDkuNSwxMDUuNXoiLz4NCgkJCTxwYXRoIGQ9Ik03OC4xLDIyMi42YzEyLjEsMSwyNC43LDEuNSwzNy42LDEuNWMzNS41LDAsNjguNy00LDg4LjktMTAuN2MxLjYtMC41LDMuNCwwLjMsMy45LDJjMC41LDEuNi0wLjMsMy40LTIsMy45DQoJCQkJYy0yMC44LDYuOS01NC44LDExLTkwLjksMTFjLTEzLDAtMjUuOS0wLjUtMzguMS0xLjZjLTEuNy0wLjEtMy0xLjctMi44LTMuNEM3NC45LDIyMy44LDc2LjQsMjIyLjUsNzguMSwyMjIuNnogTTUyLjksMjIwLjYNCgkJCQljLTEyLjMtMi0yMi45LTQuNy0zMC41LTcuNmMtMS4xLTAuNC0yLjMsMC4xLTIuNywxLjJjLTAuNCwxLjEsMC4xLDIuMywxLjIsMi43YzgsMy4xLDE4LjYsNS44LDMxLjQsNy45YzAuMSwwLDAuMiwwLDAuMywwDQoJCQkJYzEsMCwxLjktMC43LDItMS43QzU0LjgsMjIxLjksNTQsMjIwLjgsNTIuOSwyMjAuNnogTTIxNy43LDE5NC41YzUuOSwzLjYsOS41LDcuNyw5LjUsMTIuN3Y2My40YzAsMjEuNi02Ny45LDI5LjUtMTEzLjYsMjkuNQ0KCQkJCUM2Ny45LDMwMCwwLDI5Mi4xLDAsMjcwLjV2LTYzLjRjMC00LjksMy41LTkuMSw5LjUtMTIuN2MtNS45LTMuNi05LjUtNy43LTkuNS0xMi43di02My40YzAtNSwzLjYtOS4yLDkuNy0xMi44DQoJCQkJYy02LjEtMy42LTkuOC03LjktOS44LTEyLjlWMjkuNUMtMC4xLDcuOSw2Ny44LDAsMTEzLjUsMGM0NS43LDAsMTEzLjYsNy45LDExMy42LDI5LjV2NjMuNGMwLDUtMy42LDkuMi05LjcsMTIuOA0KCQkJCWM2LjEsMy42LDkuOCw3LjksOS44LDEyLjl2NjMuNEMyMjcuMiwxODYuNywyMjMuNywxOTAuOSwyMTcuNywxOTQuNXogTTE5LjMsMTAxLjNjMy43LDEuNSw4LjIsMywxMy43LDQuNA0KCQkJCWMxNy43LDQuNiw0NC42LDguMyw4MC41LDguM2MzNiwwLDYzLTMuNyw4MC42LTguNGM1LjQtMS40LDEwLTIuOSwxMy43LTQuNGM3LjItMywxMS02LDExLTguNFYyOS41YzAtNy40LTM2LjItMjEuMi0xMDUuMy0yMS4yDQoJCQkJUzguMiwyMi4xLDguMiwyOS41djYzLjRDOC4yLDk1LjIsMTIsOTguMiwxOS4zLDEwMS4zeiBNMjA4LjMsMTk4LjljLTIzLjUsOC45LTY0LjEsMTIuNC05NC43LDEyLjRjLTMwLjYsMC03MS4zLTMuNS05NC43LTEyLjQNCgkJCQljLTcsMy0xMC42LDUuOS0xMC42LDguMnY2My40YzAsNy40LDM2LjIsMjEuMiwxMDUuMywyMS4yczEwNS4zLTEzLjgsMTA1LjMtMjEuMnYtNjMuNEMyMTguOSwyMDQuOCwyMTUuMywyMDEuOSwyMDguMywxOTguOXoNCgkJCQkgTTIxOC45LDExOC41YzAtMi40LTMuOC01LjQtMTEuMS04LjRjLTIzLjUsOC44LTYzLjksMTIuMy05NC4zLDEyLjNjLTMwLjQsMC03MC43LTMuNS05NC4yLTEyLjJjLTcuMiwzLTExLDYtMTEsOC40djYzLjQNCgkJCQljMCwyLjMsMy42LDUuMywxMC42LDguMmMzLjYsMS41LDguMSwzLDEzLjUsNC40YzE3LjcsNC43LDQ0LjgsOC41LDgxLjIsOC41czYzLjUtMy44LDgxLjItOC41YzUuNC0xLjQsOS45LTIuOSwxMy41LTQuNA0KCQkJCWM3LTMsMTAuNi01LjksMTAuNi04LjJWMTE4LjV6IE0yMDQuNiwxMjQuOGMtMjAuMiw2LjctNTMuNCwxMC43LTg4LjksMTAuN2MtMTIuOSwwLTI1LjYtMC41LTM3LjYtMS41Yy0xLjctMC4xLTMuMiwxLjEtMy40LDIuOA0KCQkJCWMtMC4xLDEuNywxLjEsMy4yLDIuOCwzLjRjMTIuMiwxLDI1LDEuNiwzOC4xLDEuNmMzNi4xLDAsNzAuMS00LjEsOTAuOS0xMWMxLjYtMC41LDIuNS0yLjMsMi0zLjkNCgkJCQlDMjA4LDEyNS4xLDIwNi4zLDEyNC4yLDIwNC42LDEyNC44eiBNNTIuOSwxMzEuOWMtMTIuMy0yLTIyLjktNC43LTMwLjUtNy42Yy0xLjEtMC40LTIuMywwLjEtMi43LDEuMmMtMC40LDEuMSwwLjEsMi4zLDEuMiwyLjcNCgkJCQljOCwzLjEsMTguNiw1LjgsMzEuNCw3LjljMC4xLDAsMC4yLDAsMC4zLDBjMSwwLDEuOS0wLjcsMi0xLjdDNTQuOCwxMzMuMiw1NCwxMzIuMSw1Mi45LDEzMS45eiBNNzcuNSw1MS4yDQoJCQkJYzEyLjIsMSwyNSwxLjYsMzguMSwxLjZjMzYuMSwwLDcwLjEtNC4xLDkwLjktMTFjMS42LTAuNSwyLjUtMi4zLDItMy45Yy0wLjUtMS42LTIuMy0yLjUtMy45LTJjLTIwLjIsNi43LTUzLjQsMTAuNy04OC45LDEwLjcNCgkJCQlDMTAyLjcsNDYuNSw5MC4xLDQ2LDc4LDQ1Yy0xLjctMC4xLTMuMiwxLjEtMy40LDIuOEM3NC41LDQ5LjUsNzUuOCw1MSw3Ny41LDUxLjJ6IE01Mi41LDQ3LjFjMSwwLDEuOS0wLjcsMi0xLjcNCgkJCQljMC4yLTEuMS0wLjYtMi4yLTEuNy0yLjRjLTEyLjMtMi0yMi45LTQuNy0zMC41LTcuNmMtMS4xLTAuNC0yLjMsMC4xLTIuNywxLjJjLTAuNCwxLjEsMC4xLDIuMywxLjIsMi43YzgsMy4xLDE4LjYsNS44LDMxLjQsNy45DQoJCQkJQzUyLjIsNDcuMSw1Mi4zLDQ3LjEsNTIuNSw0Ny4xeiIvPg0KCQk8L2c+DQoJPC9nPg0KPC9nPg0KPGcgaWQ9IkxheWVyXzIiPg0KPC9nPg0KPC9zdmc+DQo=';
const editText = 'Edit';
const editTip = 'Edit ' + virtualizationName + ' virtualization';
const draftText = 'Draft';
const publishedText = 'Published';
const confirmDeleteTitle = 'Delete Virtualization';
const confirmDeleteMessage =
  'This will permanently delete the Virtualization.  Proceed with the delete?';
const confirmUnpublishTitle = 'Unpublish Virtualization';
const confirmUnpublishMessage =
  'This Virtualization has been published.  Please unpublish the Virtualization first.';
const cancelText = 'Cancel';
const deleteText = 'Delete';
const errorText = 'Error';
const exportText = 'Export';
const unpublishText = 'Unpublish';
const publishText = 'Publish';
const publishInProgressText = 'publish in progress...';
const unpublishInProgressText = 'unpublish in progress...';
const publishLogUrl = 'testUrl';
const publishLogUrlText = 'View Logs';

const publishedVirtualizationNotes =
  '- Verify the custom virtualization icon is showing on the left\n' +
  '- Verify virtualization name is "' +
  virtualizationName +
  '"\n' +
  '- Verify virtualization description is "' +
  virtualizationDescription +
  '"\n' +
  '- Verify the edit button is labeled "' +
  editText +
  '"\n' +
  '- Verify the edit button tooltip is "' +
  editTip +
  '"\n' +
  '- Verify the published button is labeled "' +
  publishedText +
  '"\n' +
  '- Verify the dropdown menu contains "' +
  deleteText +
  '"\n' +
  '- Verify the dropdown menu contains "' +
  exportText +
  '"\n' +
  '- Verify the dropdown menu contains "' +
  unpublishText +
  '"\n' +
  '[ ACTION ] Select KNOBS tab and delete all text in the icon area\n' +
  '-- >  Verify the custom virtualization icon changes to the default' +
  '\n' +
  '[ ACTION ] Select KNOBS tab and uncheck the "' +
  publishedText +
  '" checkbox\n' +
  '-- >  Verify the "' +
  publishedText +
  '" button label was changed to "' +
  draftText +
  '"\n' +
  '-- >  Verify the dropdown menu contains "' +
  publishText +
  '"\n';

stories
  .add(
    'sample virtualization item',
    withNotes(publishedVirtualizationNotes)(() => (
      <VirtualizationListItem
        hasViews={true}
        virtualizationName={virtualizationName}
        virtualizationDescription={virtualizationDescription}
        i18nCancelText={cancelText}
        i18nDelete={deleteText}
        i18nDeleteModalMessage={confirmDeleteMessage}
        i18nDeleteModalTitle={confirmDeleteTitle}
        i18nDraft={draftText}
        i18nError={errorText}
        icon={text('icon', virtualizationIconData)}
        i18nEdit={editText}
        i18nEditTip={editTip}
        /* TD-636: Commented out for TP 
        i18nExport={'Export'} */
        i18nInUseText={'The virtualization is in use by an integration.'}
        i18nPublished={publishedText}
        i18nUnpublish={unpublishText}
        i18nPublish={publishText}
        i18nPublishInProgress={publishInProgressText}
        i18nPublishLogUrlText={publishLogUrlText}
        i18nUnpublishInProgress={unpublishInProgressText}
        i18nUnpublishModalMessage={confirmUnpublishMessage}
        i18nUnpublishModalTitle={confirmUnpublishTitle}
        detailsPageLink={''}
        onDelete={action(deleteText)}
        /* TD-636: Commented out for TP 
        onExport={action(exportText)} */
        onUnpublish={action(unpublishText)}
        onPublish={action(publishText)}
        currentPublishedState={select(
          'currentState',
          [
            'BUILDING',
            'CANCELLED',
            'CONFIGURING',
            'DEPLOYING',
            'FAILED',
            'NOTFOUND',
            'RUNNING',
            'SUBMITTED',
            'DELETE_SUBMITTED',
            'DELETE_REQUEUE',
            'DELETE_DONE',
          ],
          'NOTFOUND'
        )}
        publishingLogUrl={text('publishLogUrl', publishLogUrl)}
        usedBy={['stuff']}
      />
    ))
  )

  .add('virtualization item with odata', () => (
    <VirtualizationListItem
      hasViews={true}
      virtualizationName={virtualizationName}
      virtualizationDescription={virtualizationDescription}
      i18nCancelText={cancelText}
      i18nDelete={deleteText}
      i18nDeleteModalMessage={confirmDeleteMessage}
      i18nDeleteModalTitle={confirmDeleteTitle}
      i18nDraft={draftText}
      i18nError={errorText}
      icon={text('icon', virtualizationIconData)}
      odataUrl={'http://redhat.com'}
      i18nEdit={editText}
      i18nEditTip={editTip}
      /* TD-636: Commented out for TP 
        i18nExport={'Export'} */
      i18nInUseText={'The virtualization is in use by an integration.'}
      i18nPublished={publishedText}
      i18nUnpublish={unpublishText}
      i18nPublish={publishText}
      i18nPublishInProgress={publishInProgressText}
      i18nPublishLogUrlText={publishLogUrlText}
      i18nUnpublishInProgress={unpublishInProgressText}
      i18nUnpublishModalMessage={confirmUnpublishMessage}
      i18nUnpublishModalTitle={confirmUnpublishTitle}
      detailsPageLink={''}
      onDelete={action(deleteText)}
      /* TD-636: Commented out for TP 
        onExport={action(exportText)} */
      onUnpublish={action(unpublishText)}
      onPublish={action(publishText)}
      currentPublishedState={select(
        'currentState',
        [
          'BUILDING',
          'CANCELLED',
          'CONFIGURING',
          'DEPLOYING',
          'FAILED',
          'NOTFOUND',
          'RUNNING',
          'SUBMITTED',
          'DELETE_SUBMITTED',
          'DELETE_REQUEUE',
          'DELETE_DONE',
        ],
        'NOTFOUND'
      )}
      publishingLogUrl={text('publishLogUrl', publishLogUrl)}
      usedBy={['stuff']}
    />
  ));
