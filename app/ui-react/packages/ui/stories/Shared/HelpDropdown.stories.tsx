import { action } from '@storybook/addon-actions';
import { boolean, select, withKnobs } from '@storybook/addon-knobs';
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
          dropdownDirection={select(
            'dropdownDirection',
            ['down', 'up'],
            'down'
          )}
          dropdownPosition={select(
            'dropdownPosition',
            ['left', 'right'],
            'left'
          )}
          isTabletView={boolean('isTabletView', false)}
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
