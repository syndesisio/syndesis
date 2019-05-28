import { Text, Title } from '@patternfly/react-core';
import {
  EmptyState,
  ListView,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, PageSection } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

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
      <PageSection>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <OverlayTrigger
              overlay={this.getCreateConnectorTooltip()}
              placement="top"
            >
              <ButtonLink
                data-testid={'api-connector-list-view-create-button'}
                href={this.props.linkCreateApiConnector}
                as={'primary'}
              >
                {this.props.i18nLinkCreateApiConnector}
              </ButtonLink>
            </OverlayTrigger>
          </div>
        </ListViewToolbar>
        {this.props.i18nTitle !== '' && (
          <Title size="xl">{this.props.i18nTitle}</Title>
        )}
        {this.props.i18nDescription !== '' && (
          <Text
            dangerouslySetInnerHTML={{ __html: this.props.i18nDescription }}
          />
        )}
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
                  data-testid={
                    'api-connector-list-view-empty-state-create-button'
                  }
                  href={this.props.linkCreateApiConnector}
                  as={'primary'}
                >
                  {this.props.i18nLinkCreateApiConnector}
                </ButtonLink>
              </OverlayTrigger>
            </EmptyState.Action>
          </EmptyState>
        )}
      </PageSection>
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
