import {
  DataList,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateVariant,
  Text,
  Title,
  Tooltip
} from '@patternfly/react-core';
import { AddCircleOIcon } from '@patternfly/react-icons';
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
          <div className={'form-group'}>
            <ButtonLink data-testid={'api-connector-list-view-create-button'}
                        href={this.props.linkCreateApiConnector}
                        as={'primary'}>
              {this.props.i18nLinkCreateApiConnector}
            </ButtonLink>
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
          <DataList aria-label={'api connector list'}>{this.props.children}</DataList>
        ) : (
          <EmptyState variant={EmptyStateVariant.full}>
            <EmptyStateIcon icon={AddCircleOIcon} />
            <Title headingLevel={'h5'} size={'lg'}>
              {this.props.i18nEmptyStateTitle}
            </Title>
            <EmptyStateBody>{this.props.i18nEmptyStateInfo}</EmptyStateBody>
            <Tooltip
              position={'top'}
              content={
                this.props.i18nLinkCreateApiConnectorTip
                  ? this.props.i18nLinkCreateApiConnectorTip
                  : this.props.i18nLinkCreateApiConnector
              }
              enableFlip={true}
            >
              <>
                <br/>
                <ButtonLink data-testid={'api-connector-list-view-empty-state-create-button'}
                            href={this.props.linkCreateApiConnector}
                            as={'primary'}>
                  {this.props.i18nLinkCreateApiConnector}
                </ButtonLink>
              </>
            </Tooltip>
          </EmptyState>
        )}
      </PageSection>
    );
  }
}
