import { Level, LevelItem, PageSection, Title } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { CardGrid, Grid } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { ButtonLink } from '../Layout';
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
  i18nIntegrations: string;
  i18nConnections: string;
  i18nLinkCreateConnection: string;
  i18nLinkCreateIntegration: string;
  i18nLinkToConnections: string;
  i18nLinkToIntegrations: string;
  i18nTitle: string;
  i18nDescription: string;
}

export class Dashboard extends React.PureComponent<IIntegrationsPageProps> {
  public render() {
    return (
      <>
        <SimplePageHeader
          i18nTitle={this.props.i18nTitle}
          i18nDescription={this.props.i18nDescription}
        />
        <PageSection>
          <Level gutter={'sm'}>
            <LevelItem>
              <Title size={'lg'}>{this.props.i18nIntegrations}</Title>
            </LevelItem>
            <LevelItem>
              <Link to={this.props.linkToIntegrations}>
                {this.props.i18nLinkToIntegrations}
              </Link>
              <ButtonLink
                href={this.props.linkToIntegrationCreation}
                as={'primary'}
              >
                {this.props.i18nLinkCreateIntegration}
              </ButtonLink>
            </LevelItem>
          </Level>
          <CardGrid fluid={true} matchHeight={true}>
            <CardGrid.Row>
              <CardGrid.Col sm={6} md={3}>
                {this.props.integrationsOverview}
              </CardGrid.Col>
              <CardGrid.Col sm={6} md={3}>
                {this.props.connectionsOverview}
              </CardGrid.Col>
              <CardGrid.Col sm={6} md={3}>
                {this.props.messagesOverview}
              </CardGrid.Col>
              <CardGrid.Col sm={6} md={3}>
                {this.props.uptimeOverview}
              </CardGrid.Col>
            </CardGrid.Row>
          </CardGrid>
          <Grid fluid={true}>
            <Grid.Row>
              <Grid.Col sm={12}>{this.props.topIntegrations}</Grid.Col>
            </Grid.Row>
            <Grid.Row>
              <Grid.Col sm={12} md={6}>
                {this.props.integrationBoard}
              </Grid.Col>
              <Grid.Col sm={12} md={6}>
                {this.props.integrationUpdates}
              </Grid.Col>
            </Grid.Row>
          </Grid>
        </PageSection>

        <PageSection>
          <Level gutter={'sm'}>
            <LevelItem>
              <Title size={'lg'}>{this.props.i18nConnections}</Title>
            </LevelItem>
            <LevelItem>
              <Link to={this.props.linkToConnections}>
                {this.props.i18nLinkToConnections}
              </Link>
              <ButtonLink
                href={this.props.linkToConnectionCreation}
                as={'primary'}
              >
                {this.props.i18nLinkCreateConnection}
              </ButtonLink>
            </LevelItem>
          </Level>
          <CardGrid fluid={true} matchHeight={true}>
            <CardGrid.Row>{this.props.connections}</CardGrid.Row>
          </CardGrid>
        </PageSection>
      </>
    );
  }
}
