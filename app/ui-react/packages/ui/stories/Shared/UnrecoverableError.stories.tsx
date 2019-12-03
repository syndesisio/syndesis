import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { UnrecoverableError } from '../../src';

const stories = storiesOf('Shared/UnrecoverableError', module);
stories.addDecorator(withKnobs);

stories.add('404', () => (
  <Router>
    <UnrecoverableError
      i18nTitle={text('404 Title', 'Oops! 404')}
      i18nInfo={text('404 Info', "This page couldn't be found.")}
      i18nHelp={text(
        'Help Text',
        'If you think this is a bug, please report the issue.'
      )}
      i18nRefreshLabel={text('Refresh Label', 'Refresh')}
      i18nReportIssue={text('Report the issue', 'Report the issue')}
      i18nShowErrorInfoLabel={text('Show Error Info', 'Show Error Info')}
      error={{ name: '404', message: 'File not found'} as Error}
      errorInfo={{
        componentStack: "Stuff in here\nand more stuff\nand lines\nand stuff"
      } as React.ErrorInfo}
    />
  </Router>
));
