import { action } from '@storybook/addon-actions';
import { withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import { HelpDropdown } from '../../src';
import * as React from 'react';
const stories = storiesOf('Shared/Help Dropdown', module);
stories.addDecorator(withKnobs);
const logDropdownItemSelection = action('select dropdown item log');
stories.add('HelpDropdown', () => {
  return (
    <div className="pf-u-display-flex pf-u-align-items-flex-start pf-u-flex-wrap">
      <div className="pf-u-m-xl">
        <HelpDropdown
          launchConnectorsGuide={logDropdownItemSelection}
          launchContactUs={logDropdownItemSelection}
          launchSampleIntegrationTutorials={logDropdownItemSelection}
          launchSupportPage={logDropdownItemSelection}
          launchUserGuide={logDropdownItemSelection}
          launchAboutModal={logDropdownItemSelection}
          isOpen={true}
        />
      </div>
    </div>
  );
});
