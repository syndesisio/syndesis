import * as React from 'react';
import {
  Grid,
  Form,
  FormGroup,
  Col,
  ControlLabel,
  FormControl,
  HelpBlock,
  FieldLevelHelp,
  InputGroup,
  InputGroupButton,
  Button,
  CopyUrl,
} from 'patternfly-react';

export interface IApiProviderSetInfoProps {
  /**
   * The title
   */
  i18nTitle?: string;
}

export class ApiProviderSetInfo extends React.Component<
  IApiProviderSetInfoProps
> {
  public render() {
    return (
      <>
        <Grid>
          <Form horizontal={true}>
            <FormGroup controlId={'name'} disabled={false}>
              <Col componentClass={ControlLabel} sm={3}>
                Name
              </Col>
              <Col sm={9}>
                <FormControl type={'text'} disabled={false} />
                <HelpBlock>Enter your name</HelpBlock>
              </Col>
            </FormGroup>
            <FormGroup controlId={'address'} disabled={false}>
              <Col componentClass={ControlLabel} sm={3}>
                Address
              </Col>
              <Col sm={9}>
                <FormControl type={'address'} disabled={false} />
                <HelpBlock>Enter your address</HelpBlock>
              </Col>
            </FormGroup>
            <FormGroup controlId={'city'} disabled={false}>
              <Col componentClass={ControlLabel} sm={3}>
                City
              </Col>
              <Col sm={9}>
                <FormControl type={'text'} disabled={false} />
                <HelpBlock>Enter your city</HelpBlock>
              </Col>
            </FormGroup>
            <FormGroup controlId={'email'} disabled={false}>
              <Col componentClass={ControlLabel} sm={3}>
                Email
              </Col>
              <Col sm={9}>
                <FormControl type={'email'} disabled={false} />
                <HelpBlock>Enter a valid email address</HelpBlock>
              </Col>
            </FormGroup>
            <FormGroup controlId={'phone'} disabled={false}>
              <Col componentClass={ControlLabel} sm={3}>
                Phone
                <FieldLevelHelp content={<div />} close={true} />
              </Col>
              <Col sm={9}>
                <FormControl type={'phone'} disabled={false} />
                <HelpBlock>Enter a valid phone number</HelpBlock>
              </Col>
            </FormGroup>
            <FormGroup controlId={'url'} disabled={false}>
              <Col componentClass={ControlLabel} sm={3}>
                My meeting URL
              </Col>
              <Col sm={9}>
                <InputGroup>
                  <FormControl type={'url'} disabled={false} />
                  <InputGroupButton>
                    <Button onClick={CopyUrl}>Copy URL</Button>
                  </InputGroupButton>
                </InputGroup>
                <HelpBlock>Enter a valid URL</HelpBlock>
              </Col>
            </FormGroup>
          </Form>
        </Grid>
      </>
    );
  }
}
