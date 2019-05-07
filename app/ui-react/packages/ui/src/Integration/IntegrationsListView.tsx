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
  i18nLinkCreateConnection: string;
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
              <ButtonLink href={this.props.linkToManageCiCd}>
                {this.props.i18nManageCiCd}
              </ButtonLink>
              <ButtonLink href={this.props.linkToIntegrationImport}>
                {this.props.i18nImport}
              </ButtonLink>
              <ButtonLink
                href={this.props.linkToIntegrationCreation}
                as={'primary'}
              >
                {this.props.i18nLinkCreateConnection}
              </ButtonLink>
            </div>
          </ListViewToolbar>
          {this.props.children}
        </PageSection>
      </>
    );
  }
}
