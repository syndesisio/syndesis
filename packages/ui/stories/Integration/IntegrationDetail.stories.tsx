// import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  Container,
  IntegrationDetailHistoryListView,
  TabBar,
  TabBarItem,
} from '../../src';

storiesOf('Integration/Detail', module).add('integration published', () => (
  <>
    <Container
      style={{
        background: '#fff',
      }}
    >
      <TabBar>
        <TabBarItem
          label={'integrationDetailTitle'}
          to={'#integration-detail'}
        />
        <TabBarItem label={'integrationDetailTitle'} to={'#detail'} />
      </TabBar>
    </Container>
    <IntegrationDetailHistoryListView integrationIsDraft={false} />
  </>
));
