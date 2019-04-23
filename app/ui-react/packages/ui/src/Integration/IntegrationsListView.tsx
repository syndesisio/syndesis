import * as React from 'react';
import { ButtonLink, Container } from '../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../Shared';

export interface IIntegrationsListViewProps extends IListViewToolbarProps {
  linkToManageCiCd: string;
  linkToIntegrationImport: string;
  linkToIntegrationCreation: string;
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
        <Container>{this.props.children}</Container>
      </>
    );
  }
}
