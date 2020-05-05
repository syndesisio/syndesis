import { Virtualization } from '@syndesis/models';
import { Container, TabBar, TabBarItem } from '@syndesis/ui';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import resolvers from '../resolvers';
import './VirtualizationNavBar.css';

/**
 * @param virtualization - the virtualization whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationNavBarProps#virtualization}.
 */

export interface IVirtualizationNavBarProps {
  virtualization: Virtualization;
}

/**
 * A component that displays a nav bar with 4 items:
 *
 * 1. a link to the page that displays a list of Views
 * 2. a link to the page that displays the virtualization state and history
 * 3. a link to the page that displays the SQL Query editor
 *
 */
export const VirtualizationNavBar: React.FunctionComponent<
  IVirtualizationNavBarProps
> = props => {
  const { t } = useTranslation(['data']);
  const virtualization = props.virtualization;

  return (
    <Container className={'virtualization-nav-bar'}>
      <TabBar>
        <TabBarItem
          label={t('views')}
          to={resolvers.virtualizations.views.root({
            virtualization,
          })}
        />
        <TabBarItem
          label={t('sqlClient')}
          to={resolvers.virtualizations.sqlClient({
            virtualization,
          })}
        />
        <TabBarItem
          label={t('versions')}
          to={resolvers.virtualizations.versions({
            virtualization,
          })}
        />
        <TabBarItem
          label={t('Metrics')}
          to={resolvers.virtualizations.metrics({
            virtualization,
          })}
        />
        <TabBarItem
          label={'Data Permission'}
          to={resolvers.virtualizations.dataPermission({
            virtualization,
          })}
        />
      </TabBar>
    </Container>
  );
};
