import { DataList } from '@patternfly/react-core';
import { action } from '@storybook/addon-actions';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { CiCdListItem } from '../../../src';

const stories = storiesOf('Integration/CiCd/CiCdListItem', module);
stories.addDecorator(withKnobs);

stories.add('Normal', () => (
  <DataList aria-label={'CiCdList'}>
    <CiCdListItem
      onEditClicked={action('onEditClicked')}
      onRemoveClicked={action('onRemoveClicked')}
      i18nEditButtonText={text('Edit Button', 'Edit')}
      i18nRemoveButtonText={text('Remove Button', 'Remove')}
      name={text('Name', 'MyAwesomeEnvironment')}
      i18nUsesText={text('Uses', 'Used by 3 integrations')}
    />
  </DataList>
));
