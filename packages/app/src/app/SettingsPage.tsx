import { Button, Col, Form, Grid, Row } from 'patternfly-react';
import * as React from 'react';
import { AppContext } from './';

interface ISettingsPageProps {
  apiUri: string;
  authorizationUri: string;

  onSave(settings: ISettingsPageState): void;
}

interface ISettingsPageState {
  apiUri: string;
  authorizationUri: string;
}

class SettingsPageBase extends React.Component<
  ISettingsPageProps,
  ISettingsPageState
> {
  public constructor(props: ISettingsPageProps) {
    super(props);
    this.state = {
      apiUri: this.props.apiUri,
      authorizationUri: this.props.authorizationUri
    };
  }

  public render() {
    const setState = (property: string) => {
      return (event: { target: HTMLInputElement }) => {
        const state = {
          ...this.state
        };
        state[property] = event.target.value;
        this.setState(state);
      };
    };

    return (
      <div className={'container-fluid'}>
        <Grid>
          <Form>
            <Row>
              <Col>
                <Form.FormGroup>
                  <Form.ControlLabel>Api URI</Form.ControlLabel>
                  <Form.FormControl
                    type="text"
                    placeholder={'https://example'}
                    value={this.state.apiUri}
                    onChange={setState('apiUri')}
                  />
                  <Form.HelpBlock>The Api remote URI.</Form.HelpBlock>
                </Form.FormGroup>
              </Col>
            </Row>
            <Row>
              <Col>
                <Form.FormGroup>
                  <Form.ControlLabel>Authorization URI</Form.ControlLabel>
                  <Form.FormControl
                    type="text"
                    placeholder={'https://example/com/oauth2/authorize'}
                    value={this.state.authorizationUri}
                    onChange={setState('authorizationUri')}
                  />
                  <Form.HelpBlock>
                    The address of an OAuth2 server with support for the
                    `implicit` grant flow.
                  </Form.HelpBlock>
                </Form.FormGroup>
              </Col>
            </Row>
            <Row style={{ paddingTop: '10px', paddingBottom: '10px' }}>
              <Col>
                <span>
                  <Button className={'btn btn-primary'} onClick={this.onSave}>
                    Save
                  </Button>
                </span>
              </Col>
            </Row>
          </Form>
        </Grid>
      </div>
    );
  }

  public onSave = () => {
    this.props.onSave(this.state);
  };
}

export const SettingsPage = () => (
  <AppContext.Consumer>
    {({ apiUri, authorizationUri, saveSettings }) => (
      <SettingsPageBase
        apiUri={apiUri}
        authorizationUri={authorizationUri}
        onSave={saveSettings}
      />
    )}
  </AppContext.Consumer>
);
