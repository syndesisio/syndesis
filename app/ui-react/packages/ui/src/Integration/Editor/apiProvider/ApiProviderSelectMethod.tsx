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

export class ApiProviderSelectMethod extends React.Component<
  IApiProviderSelectMethodProps
> {
  public render() {
    return (
      <Grid>
        <Form>
          <Row>
            <Col>
              <FormGroup controlId={'method'} disabled={false}>
                <div>
                  <Radio name={'method'}>
                    <div>{this.props.i18nMethodFromFile}</div>
                    <div>
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
                    </div>
                  </Radio>
                  <Radio name={'method'}>
                    <div>{this.props.i18nMethodFromUrl}</div>
                    <div>
                      <FormControl type={'text'} disabled={false} />
                    </div>
                  </Radio>
                  <Radio name={'method'}>
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
