import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { MemoryRouter } from 'react-router';
import { ViewEditorNavBar } from '../../../src';

const stories = storiesOf('Data/ViewEditor/ViewEditorNavBar', module);

stories.add('render', () => {
  return (
    <MemoryRouter>
      <ViewEditorNavBar
        i18nFinishButton={'Done'}
        i18nViewOutputTab={'View Output'}
        i18nViewSqlTab={'View SQL'}
        viewOutputHref="https://access.redhat.com/support/offerings/techpreview"
        viewSqlHref="https://access.redhat.com/support/offerings/techpreview"
        onEditFinished={action('done')}
      />
    </MemoryRouter>
  );
});
