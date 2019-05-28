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

import { Container } from '../Layout';
import { DndFileChooser } from './index';
import './OpenApiSelectMethod.css';

export interface IOpenApiSelectMethodProps {
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

export interface IOpenApiSelectMethodState {
  method: string;
}

export class OpenApiSelectMethod extends React.Component<
  IOpenApiSelectMethodProps,
  IOpenApiSelectMethodState
> {
  constructor(props: any) {
    super(props);
    this.state = {
      method: 'file',
    };

    this.onChangeMethod = this.onChangeMethod.bind(this);
  }

  public onChangeMethod(newMethod: string) {
    this.setState({ method: newMethod });
  }

  public render() {
    return (
      <Grid className={'open-api-select-method'}>
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
                        <br />
                        <span className={'url-note'}>
                          {this.props.i18nUrlNote}
                        </span>
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
