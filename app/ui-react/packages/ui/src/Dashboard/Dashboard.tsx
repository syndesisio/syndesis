import {
  Grid,
  GridItem,
  Stack,
  StackItem,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { ButtonLink, PageSection } from '../Layout';
import { SimplePageHeader } from '../Shared';
import './Dashboard.css';

export interface IIntegrationsPageProps {
  linkToIntegrations: H.LocationDescriptor;
  linkToIntegrationCreation: H.LocationDescriptor;
  linkToConnections: H.LocationDescriptor;
  linkToConnectionCreation: H.LocationDescriptor;
  integrationsOverview: JSX.Element;
  connectionsOverview: JSX.Element;
  messagesOverview: JSX.Element;
  uptimeOverview: JSX.Element;
  topIntegrations: JSX.Element;
  integrationBoard: JSX.Element;
  integrationUpdates: JSX.Element;
  connections: JSX.Element;
  noIntegrations?: boolean;
  i18nConnections: string;
  i18nLinkCreateConnection: string;
  i18nLinkCreateIntegration: string;
  i18nLinkToConnections: string;
  i18nLinkToIntegrations: string;
  i18nTitle: string;
}

export class Dashboard extends React.PureComponent<IIntegrationsPageProps> {
  public render() {
    return (
      <>
        <SimplePageHeader i18nTitle={this.props.i18nTitle} titleSize={'xl'} />
        <PageSection>
          <Stack gutter={'md'}>
            <StackItem
              className="dashboard__integrations__actions"
              isFilled={false}
            >
              <ButtonLink
                data-testid={'dashboard-create-integration-button'}
                href={this.props.linkToIntegrationCreation}
                as={'primary'}
                className={'pull-right'}
              >
                {this.props.i18nLinkCreateIntegration}
              </ButtonLink>
              <Link
                data-testid={'dashboard-integrations-link'}
                to={this.props.linkToIntegrations}
                className={'pull-right view'}
              >
                {this.props.i18nLinkToIntegrations}
              </Link>
            </StackItem>

            <StackItem className="dashboard__metrics" isFilled={false}>
              <Grid md={6} xl={3} gutter={'sm'}>
                <GridItem>{this.props.integrationsOverview}</GridItem>
                <GridItem>{this.props.connectionsOverview}</GridItem>
                <GridItem>{this.props.messagesOverview}</GridItem>
                <GridItem>{this.props.uptimeOverview}</GridItem>
              </Grid>
            </StackItem>

            <StackItem className="dashboard__integrations" isFilled={false}>
              {/* TODO last minute empty state hack with minimal changes, this needs a revisit */
              this.props.noIntegrations ? (
                <>{this.props.topIntegrations}</>
              ) : (
                <>
                  <Grid gutter={'lg'}>
                    <GridItem xl={7} xlRowSpan={12}>
                      {this.props.topIntegrations}
                    </GridItem>
                    <GridItem lg={6} xl={5} xlRowSpan={6}>
                      {this.props.integrationBoard}
                    </GridItem>
                    <GridItem lg={6} xl={5} xlRowSpan={6}>
                      {this.props.integrationUpdates}
                    </GridItem>
                  </Grid>
                </>
              )}
            </StackItem>

            <StackItem
              className="dashboard__connections__actions"
              isFilled={false}
            >
              <Title size={'lg'} className={'pull-left'}>
                {this.props.i18nConnections}
              </Title>
              <ButtonLink
                data-testid={'dashboard-create-connection-button'}
                href={this.props.linkToConnectionCreation}
                as={'primary'}
                className={'pull-right'}
              >
                {this.props.i18nLinkCreateConnection}
              </ButtonLink>
              <Link
                data-testid={'dashboard-connections-link'}
                to={this.props.linkToConnections}
                className={'pull-right view'}
              >
                {this.props.i18nLinkToConnections}
              </Link>
            </StackItem>

            <StackItem
              className="dashboard__connections__grid"
              isFilled={false}
            >
              {this.props.connections}
            </StackItem>
          </Stack>
        </PageSection>
      </>
    );
  }
}
