import {
  Col,
  FormControl,
  FormGroup,
  Grid,
  Radio,
  Row,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, Container } from '../Layout';
import { DndFileChooser } from './DndFileChooser';
import './OpenApiSelectMethod.css';

export interface IOpenApiSelectMethodProps {
  allowMultiple?: boolean;
  disableDropzone: boolean;
  fileExtensions?: string;
  /**
   * Localized strings to be displayed.
   */
  i18nBtnCancel: string;
  i18nBtnNext: string;
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
}

export interface IOpenApiSelectMethodState {
  method?: string;
  specification?: string;
  valid?: boolean;
}

export class OpenApiSelectMethod extends React.Component<
  IOpenApiSelectMethodProps,
  IOpenApiSelectMethodState
> {
  constructor(props: any) {
    super(props);
    this.state = {
      method: 'file',
      specification: '',
      valid: false,
    };

    this.disableNext = this.disableNext.bind(this);
    this.onAddSpecification = this.onAddSpecification.bind(this);
    this.onNext = this.onNext.bind(this);
    this.onSelectMethod = this.onSelectMethod.bind(this);
  }

  public checkValidUrl(url: string): boolean {
    console.log('Check URL: ' + url);
    return true;
  }

  /**
   * Boolean value that determines whether or not to disable
   * the Next button
   * 1. The specification string must not be empty,
   * unless the method is 'scratch'
   * 2. If the method is 'url', that URL must be valid
   */
  public disableNext() {
    console.log('Allow next or no?');
    console.log(
      this.state.method === 'scratch' || this.state.specification !== ''
    );
    this.setState({
      valid: this.state.method === 'scratch' || this.state.specification !== '',
    });
  }

  /**
   * User has added a specification either via a URL or file upload
   * @param e
   */
  public onAddSpecification(e: React.FormEvent<HTMLInputElement>) {
    this.setState({ specification: e.currentTarget.value });

    if (
      this.state.method === 'url' &&
      this.checkValidUrl(e.currentTarget.value)
    ) {
      console.log('Valid');
      this.setState({ valid: true });
    }
  }

  /**
   * The action fired when the user presses the Next button.
   */
  public onNext(): void {
    console.log('User wants to click Next');
  }

  /**
   * The action fired when the user selects the method
   * to provide an OpenAPI specification.
   * @param newMethod
   */
  public onSelectMethod(newMethod: string) {
    this.setState({ method: newMethod, valid: newMethod === 'scratch' });
  }

  /**
   * Callback for when one or more file uploads have been accepted.
   */
  public onUploadAccepted(files: File[]): void {
    this.setState({ valid: true });
    files.forEach(file => {
      return '<span>Process file ' + file.name + '</span>\n';
    });
  }

  /**
   * Obtains the localized text (may include HTML tags) that appears when the file upload was rejected. This
   * will occur when a DnD of a file with the wrong extension is dropped. This message is presented
   * as a timed toast notification.
   */
  public onUploadRejected(fileName: string): string {
    return (
      '<span>File <strong>' +
      fileName +
      '</strong> could not be uploaded</span>'
    );
  }

  public render() {
    return (
      <Grid className={'open-api-select-method'}>
        <Row>
          <Col>
            <FormGroup controlId={'method'} disabled={false}>
              <div>
                <Radio
                  name={'method'}
                  onClick={() => this.onSelectMethod('file')}
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
                        i18nSelectedFileLabel={this.props.i18nSelectedFileLabel}
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
                        onUploadAccepted={this.onUploadAccepted}
                        onUploadRejected={this.onUploadRejected}
                      />
                    </Container>
                  )}
                </Radio>
                <Radio
                  name={'method'}
                  onClick={() => this.onSelectMethod('url')}
                >
                  <div>{this.props.i18nMethodFromUrl}</div>
                  {this.state.method === 'url' && (
                    <div>
                      <FormControl
                        type={'text'}
                        disabled={false}
                        value={this.state.specification}
                        onChange={this.onAddSpecification}
                      />
                      <br />
                      <span className={'url-note'}>
                        {this.props.i18nUrlNote}
                      </span>
                    </div>
                  )}
                </Radio>
                <Radio
                  name={'method'}
                  onClick={() => this.onSelectMethod('scratch')}
                >
                  <div>{this.props.i18nMethodFromScratch}</div>
                </Radio>
              </div>
            </FormGroup>
          </Col>

          <ButtonLink
            disabled={!this.state.valid}
            as={'primary'}
            onClick={this.onNext}
          >
            {this.props.i18nBtnNext}
          </ButtonLink>
          <ButtonLink>{this.props.i18nBtnCancel}</ButtonLink>
        </Row>
      </Grid>
    );
  }
}
