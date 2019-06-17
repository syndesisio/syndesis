import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, PageSection } from '../Layout';
import {
  IListViewToolbarProps,
  ListViewToolbar,
  SimplePageHeader,
} from '../Shared';
import { IntegrationsEmptyState } from './IntegrationsEmptyState';

export interface IIntegrationsListViewProps extends IListViewToolbarProps {
  linkToManageCiCd: H.LocationDescriptor;
  linkToIntegrationImport: H.LocationDescriptor;
  linkToIntegrationCreation: H.LocationDescriptor;
  i18nTitle: string;
  i18nDescription: string;
  i18nManageCiCd: string;
  i18nImport: string;
  i18nLinkCreateConnection: string;
  i18nLinkCreateIntegrationTip?: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
}

export class IntegrationsListView extends React.Component<
  IIntegrationsListViewProps
> {
  public render() {
    return (
      <>
        <SimplePageHeader
          i18nTitle={this.props.i18nTitle}
          i18nDescription={this.props.i18nDescription}
        />
        <PageSection>
          <ListViewToolbar {...this.props}>
            <div className="form-group">
              <ButtonLink
                data-testid={'integrations-list-view-manage-cicd-button'}
                href={this.props.linkToManageCiCd}
              >
                {this.props.i18nManageCiCd}
              </ButtonLink>
              <ButtonLink
                data-testid={'integrations-list-view-import-button'}
                href={this.props.linkToIntegrationImport}
              >
                {this.props.i18nImport}
              </ButtonLink>
              <ButtonLink
                data-testid={'integrations-list-view-create-button'}
                href={this.props.linkToIntegrationCreation}
                as={'primary'}
              >
                {this.props.i18nLinkCreateConnection}
              </ButtonLink>
            </div>
          </ListViewToolbar>
          {this.props.children ? (
            this.props.children
          ) : (
            <IntegrationsEmptyState
              i18nCreateIntegration={this.props.i18nLinkCreateConnection}
              i18nCreateIntegrationTip={this.props.i18nLinkCreateIntegrationTip}
              i18nEmptyStateInfo={this.props.i18nEmptyStateInfo}
              i18nEmptyStateTitle={this.props.i18nEmptyStateTitle}
              linkCreateIntegration={this.props.linkToIntegrationCreation}
            />
          )}
        </PageSection>
      </>
    );
  }
}
