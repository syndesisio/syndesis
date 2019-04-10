import {
  EmptyState,
  ListView,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, Container } from '../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../Shared';

export interface IApiConnectorListViewProps extends IListViewToolbarProps {
  i18nDescription: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLinkCreateApiConnector: string;
  i18nLinkCreateApiConnectorTip?: string;
  i18nName: string;
  i18nTitle: string;
  linkCreateApiConnector: string;
}

export class ApiConnectorListView extends React.Component<
  IApiConnectorListViewProps
> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <OverlayTrigger
              overlay={this.getCreateConnectorTooltip()}
              placement="top"
            >
              <ButtonLink
                href={this.props.linkCreateApiConnector}
                as={'primary'}
              >
                {this.props.i18nLinkCreateApiConnector}
              </ButtonLink>
            </OverlayTrigger>
          </div>
        </ListViewToolbar>
        <Container>
          <h1>{this.props.i18nTitle}</h1>
          <div
            dangerouslySetInnerHTML={{ __html: this.props.i18nDescription }}
          />
          {this.props.children ? (
            <ListView>{this.props.children}</ListView>
          ) : (
            <EmptyState>
              <EmptyState.Icon />
              <EmptyState.Title>
                {this.props.i18nEmptyStateTitle}
              </EmptyState.Title>
              <EmptyState.Info>{this.props.i18nEmptyStateInfo}</EmptyState.Info>
              <EmptyState.Action>
                <OverlayTrigger
                  overlay={this.getCreateConnectorTooltip()}
                  placement="top"
                >
                  <ButtonLink
                    href={this.props.linkCreateApiConnector}
                    as={'primary'}
                  >
                    {this.props.i18nLinkCreateApiConnector}
                  </ButtonLink>
                </OverlayTrigger>
              </EmptyState.Action>
            </EmptyState>
          )}
        </Container>
      </>
    );
  }

  private getCreateConnectorTooltip() {
    return (
      <Tooltip id="createTip">
        {this.props.i18nLinkCreateApiConnectorTip
          ? this.props.i18nLinkCreateApiConnectorTip
          : this.props.i18nLinkCreateApiConnector}
      </Tooltip>
    );
  }
}
