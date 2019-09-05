import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, PageSection } from '../Layout';
import {
  IListViewToolbarProps,
  ListViewToolbar,
  SimplePageHeader,
} from '../Shared';

export interface IIntegrationsListViewProps extends IListViewToolbarProps {
  linkToManageCiCd: H.LocationDescriptor;
  linkToIntegrationImport: H.LocationDescriptor;
  linkToIntegrationCreation: H.LocationDescriptor;
  i18nTitle: string;
  i18nDescription: string;
  i18nManageCiCd: string;
  i18nImport: string;
  i18nLinkCreateIntegration: string;
  i18nLinkCreateIntegrationTip?: string;
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
                {this.props.i18nLinkCreateIntegration}
              </ButtonLink>
            </div>
          </ListViewToolbar>
          {this.props.children}
        </PageSection>
      </>
    );
  }
}
