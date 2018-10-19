import {
  Card,
  CardGrid,
  Col,
  ListView,
  MenuItem,
  Row,
} from 'patternfly-react';
import * as React from 'react';
import {IIntegration, WithIntegrations} from "../containers";
import {IntegrationsListItem} from "./components/IntegrationsListItem";

export interface IIntegrationsPageState {
  project: string;
}

export class DashboardPage extends React.Component<{}, IIntegrationsPageState> {
  public state = {
    project: 'default'
  };

  public render() {
    return (
      <div className={'container-fluid container-cards-pf'}>
        <div className={'row row-cards-pf'}>
          <CardGrid>
            <Row style={{marginBottom: '20px',marginTop: '20px'}}>
              <Col xs={6} sm={4} md={4}>
                <Card accented={false}>
                  <Card.Heading>
                    <Card.DropdownButton id="cardDropdownButton1" title="Last 30 Days">
                      <MenuItem eventKey="1" active={true}>
                        Last 30 Days
                      </MenuItem>
                      <MenuItem eventKey="2">
                        Last 60 Days
                      </MenuItem>
                      <MenuItem eventKey="3">
                        Last 90 Days
                      </MenuItem>
                    </Card.DropdownButton>
                    <Card.Title>
                      Top 5 Integrations
                    </Card.Title>
                  </Card.Heading>
                  <Card.Body>
                    <WithIntegrations>
                      {({integrations}) =>
                        <ListView>
                          {integrations.map((integration: IIntegration, index) => (
                            <IntegrationsListItem
                              integration={integration}
                              key={index}
                            />
                          ))}
                        </ListView>
                      }
                    </WithIntegrations>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          </CardGrid>
        </div>
      </div>
    );
  }

  public setNamespace = (project: string) => {
    this.setState({
      project
    });
  }
}
