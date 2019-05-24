import { action } from '@storybook/addon-actions';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ImportPageUI } from '../../../src/Integration/Import/ImportPageUI';
import { Container } from '../../../src/Layout';

const stories = storiesOf('Integration/Import/ImportPageUI', module);
stories.addDecorator(withKnobs);

const i18nInstructions =
  "Drag 'n' drop one or more files here, or <strong>click</strong> to select files using a file chooser dialog.";

stories.add('normal', () => (
  <Container>
    <ImportPageUI
      onUploadRejected={action('onUploadRejected')}
      onUploadAccepted={action('onUploadAccepted')}
      i18nPageTitle={text('Import Integration', 'Import Integration')}
      i18nPageDescription={text(
        'Description',
        'Choose one or more zip files that contain exported integrations that you want to import.'
      )}
      i18nNoFileSelectedMessage={text('No File Selected', 'No File Selected')}
      i18nSelectedFileLabel={text('Selected File Label', '')}
      i18nInstructions={i18nInstructions}
      i18nHelpMessage={text(
        'Help Message',
        'Note: The imported integration will be in the draft state. If you previously imported and this environment has a draft version of the integration, then that draft is lost.'
      )}
    />
  </Container>
));
