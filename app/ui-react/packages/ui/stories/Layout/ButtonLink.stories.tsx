import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { ButtonLink } from '../../src';

const stories = storiesOf('Layout/ButtonLink', module);

stories
  .add('lg-primary enabled', () => (
    <Router>
      <ButtonLink href={'test/link'} as={'primary'} size={'lg'}>
        Test
      </ButtonLink>
    </Router>
  ))
  .add('lg-primary disabled', () => (
    <Router>
      <ButtonLink href={'test/link'} as={'primary'} size={'lg'} disabled={true}>
        Test
      </ButtonLink>
    </Router>
  ));
