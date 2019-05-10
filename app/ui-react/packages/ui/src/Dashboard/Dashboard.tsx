import {
  Grid,
  GridItem,
  Split,
  SplitItem,
  Stack,
  StackItem,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { CardGrid } from 'patternfly-react';
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
      <PageSection>
        <Stack gutter={'md'} className={'dashboard'}>
          <StackItem isFilled={false} className={'dashboard__header'}>
            <Split>
              <SplitItem isFilled={true}>
                <SimplePageHeader
                  i18nTitle={this.props.i18nTitle}
                  titleSize={'xl'}
                  variant={'default'}
                />
              </SplitItem>

              <SplitItem isFilled={false} className={'header__actions'}>
                <Split gutter={'md'}>
                  <SplitItem
                    isFilled={false}
                    className={'header__actions--link'}
                  >
                    <Link to={this.props.linkToIntegrations}>
                      {this.props.i18nLinkToIntegrations}
                    </Link>
                  </SplitItem>
                  <SplitItem isFilled={false}>
                    <ButtonLink
                      href={this.props.linkToIntegrationCreation}
                      as={'primary'}
                    >
                      {this.props.i18nLinkCreateIntegration}
                    </ButtonLink>
                  </SplitItem>
                </Split>
              </SplitItem>
            </Split>
          </StackItem>

          <StackItem isFilled={false} className={'dashboard__metrics'}>
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
          </StackItem>

          <PageSection>
            <StackItem isFilled={false} className={'dashboard__integrations'}>
              <Grid gutter={'sm'}>
                <GridItem span={7} rowSpan={3} sm={12}>
                  {this.props.topIntegrations}
                </GridItem>
                <GridItem sm={6}>{this.props.integrationBoard}</GridItem>
                <GridItem sm={6}>{this.props.integrationUpdates}</GridItem>
              </Grid>
            </StackItem>
          </PageSection>

          <StackItem isFilled={false} className={'dashboard__connections'}>
            <Split>
              <SplitItem isFilled={true}>
                <Title size={'lg'}>{this.props.i18nConnections}</Title>
              </SplitItem>

              <SplitItem isFilled={false}>
                <Split gutter={'md'}>
                  <SplitItem isFilled={false}>
                    <Link to={this.props.linkToConnections}>
                      {this.props.i18nLinkToConnections}
                    </Link>
                  </SplitItem>
                  <SplitItem isFilled={false}>
                    <ButtonLink
                      href={this.props.linkToConnectionCreation}
                      as={'primary'}
                    >
                      {this.props.i18nLinkCreateConnection}
                    </ButtonLink>
                  </SplitItem>
                </Split>
              </SplitItem>
            </Split>

            <CardGrid fluid={true} matchHeight={true}>
              <CardGrid.Row>{this.props.connections}</CardGrid.Row>
            </CardGrid>
          </StackItem>
        </Stack>
      </PageSection>
    );
  }
}
