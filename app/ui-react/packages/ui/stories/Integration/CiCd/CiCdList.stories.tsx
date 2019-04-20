import { action } from '@storybook/addon-actions';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { CiCdList, CiCdListItem } from '../../../src';

const stories = storiesOf('Integration/CiCd/CiCdList', module);
stories.addDecorator(withKnobs);

stories.add('Normal', () => (
  <CiCdList
    children={[
      {
        i18nUsesText: 'Used by 3 integrations',
        name: 'Environment 1',
      },
      {
        i18nUsesText: 'Used by 0 integrations',
        name: 'Environment 2',
      },
    ].map((env, index) => (
      <CiCdListItem
        key={index}
        onEditClicked={action('onEditClicked')}
        onRemoveClicked={action('onRemoveClicked')}
        i18nEditButtonText={text('Edit Button', 'Edit')}
        i18nRemoveButtonText={text('Remove Button', 'Remove')}
        name={env.name}
        i18nUsesText={env.i18nUsesText}
      />
    ))}
  />
));
