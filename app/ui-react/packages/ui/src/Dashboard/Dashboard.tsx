import {
  Grid,
  GridItem,
  Level,
  LevelItem,
  PageSection,
  Split,
  SplitItem,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { CardGrid } from 'patternfly-react';
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
        <PageSection className={'dashboard__header'}>
          <Split>
            <SplitItem isFilled={false}>
              <SimplePageHeader
                i18nTitle={this.props.i18nTitle}
                titleSize={'xl'}
                variant={'default'}
              />
            </SplitItem>
            <SplitItem isFilled />
            <SplitItem isFilled={false}>
              <Level gutter={'sm'}>
                <LevelItem>&nbsp;</LevelItem>
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
            </SplitItem>
          </Split>
        </PageSection>

        <PageSection>
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
          <Grid gutter={'lg'}>
            <GridItem span={7} rowSpan={3}>
              {this.props.topIntegrations}
            </GridItem>
            <GridItem span={5}>{this.props.integrationBoard}</GridItem>
            <GridItem span={5}>{this.props.integrationUpdates}</GridItem>
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
