import {
  Col,
  Form,
  FormControl,
  FormGroup,
  Grid,
  Radio,
  Row,
} from 'patternfly-react';
import * as React from 'react';

import { Container } from '../../../Layout';
import { DndFileChooser } from '../../../Shared';
import './ApiProviderSelectMethod.css';

export interface IApiProviderSelectMethodProps {
  allowMultiple?: boolean;
  disableDropzone: boolean;
  fileExtensions?: string;
  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;
  /**
   * Localized strings to be displayed.
   */
  i18nHelpMessage?: string;
  i18nInstructions: string;
  i18nMethodFromFile: string;
  i18nMethodFromUrl: string;
  i18nMethodFromScratch: string;
  i18nNoFileSelectedMessage: string;
  i18nSelectedFileLabel: string;
  i18nUploadFailedMessage?: string;
  i18nUploadFailedMessages?: string[];
  i18nUploadSuccessMessage?: string;
  i18nUploadSuccessMessages?: string[];
  i18nUrlNote: string;

  /**
   * Callback for when one or more file uploads have been accepted.
   */
  onUploadAccepted(file: File[]): void;

  /**
   * Obtains the localized text (may include HTML tags) that appears when the file upload was rejected. This
   * will occur when a DnD of a file with the wrong extension is dropped. This message is presented
   * as a timed toast notification.
   */
  onUploadRejected(fileName: string): string;
}

export interface IApiProviderSelectMethodState {
  method: string;
}

export class ApiProviderSelectMethod extends React.Component<
  IApiProviderSelectMethodProps,
  IApiProviderSelectMethodState
> {
  constructor(props: any) {
    super(props);
    this.state = {
      method: 'file',
    };

    this.onChangeMethod = this.onChangeMethod.bind(this);
  }

  public onChangeMethod(newMethod: string) {
    console.log('this.state.method before: ' + this.state.method);
    console.log('newMethod: ' + JSON.stringify(newMethod));
    this.setState({ method: newMethod });
    console.log('this.state.method after: ' + this.state.method);
  }

  public render() {
    return (
      <Grid>
        <Form onSubmit={this.props.handleSubmit}>
          <Row>
            <Col>
              <FormGroup controlId={'method'} disabled={false}>
                <div>
                  <Radio
                    name={'method'}
                    onClick={() => this.onChangeMethod('file')}
                  >
                    <div>{this.props.i18nMethodFromFile}</div>
                    {this.state.method === 'file' && (
                      <Container style={{ margin: '50px' }}>
                        <DndFileChooser
                          allowMultiple={this.props.allowMultiple}
                          disableDropzone={this.props.disableDropzone}
                          fileExtensions={this.props.fileExtensions}
                          i18nHelpMessage={this.props.i18nHelpMessage}
                          i18nInstructions={this.props.i18nInstructions}
                          i18nNoFileSelectedMessage={
                            this.props.i18nNoFileSelectedMessage
                          }
                          i18nSelectedFileLabel={
                            this.props.i18nSelectedFileLabel
                          }
                          i18nUploadFailedMessage={
                            this.props.i18nUploadFailedMessage
                          }
                          i18nUploadFailedMessages={
                            this.props.i18nUploadFailedMessages
                          }
                          i18nUploadSuccessMessage={
                            this.props.i18nUploadSuccessMessage
                          }
                          i18nUploadSuccessMessages={
                            this.props.i18nUploadSuccessMessages
                          }
                          onUploadAccepted={this.props.onUploadAccepted}
                          onUploadRejected={this.props.onUploadRejected}
                        />
                      </Container>
                    )}
                  </Radio>
                  <Radio
                    name={'method'}
                    onClick={() => this.onChangeMethod('url')}
                  >
                    <div>{this.props.i18nMethodFromUrl}</div>
                    {this.state.method === 'url' && (
                      <div>
                        <FormControl type={'text'} disabled={false} />
                      </div>
                    )}
                  </Radio>
                  <Radio
                    name={'method'}
                    onClick={() => this.onChangeMethod('scratch')}
                  >
                    <div>{this.props.i18nMethodFromScratch}</div>
                  </Radio>
                </div>
              </FormGroup>
            </Col>
          </Row>
        </Form>
      </Grid>
    );
  }
}
