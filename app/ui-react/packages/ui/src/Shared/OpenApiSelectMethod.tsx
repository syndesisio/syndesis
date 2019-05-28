// tslint:disable:no-console
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

  /**
   * The action fired when the user presses the Next button.
   */
  onNext(method?: string, specification?: string): void;
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

    this.onAddUrlSpecification = this.onAddUrlSpecification.bind(this);
    this.onSelectMethod = this.onSelectMethod.bind(this);
    this.onUploadAccepted = this.onUploadAccepted.bind(this);
    this.onUploadRejected = this.onUploadRejected.bind(this);
  }

  public checkValidUrl(url: string): boolean {
    const regexp = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
    return regexp.test(url);
  }

  /**
   * User has added a specification via a string URL, which will be
   * checked if is a valid HTTP/HTTPS string.
   * @param e
   */
  public onAddUrlSpecification(e: React.FormEvent<HTMLInputElement>) {
    this.setState({ specification: e.currentTarget.value });

    if (
      this.state.method === 'url' &&
      this.checkValidUrl(e.currentTarget.value)
    ) {
      this.setState({ valid: true });
    } else {
      this.setState({ valid: false });
    }
  }

  /**
   * The action fired when the user selects the method
   * to provide an OpenAPI specification.
   * @param newMethod
   */
  public onSelectMethod(newMethod: string) {
    this.setState({
      method: newMethod,
      specification: '',
      valid: newMethod === 'scratch',
    });
  }

  /**
   * Callback for when one or more file uploads have been accepted.
   */
  public onUploadAccepted(files: File[]): void {
    const reader = new FileReader();
    reader.readAsText(files[0]);
    reader.onload = () => {
      this.setState({ specification: reader.result as string, valid: true });
    };
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
                  id={'method-file'}
                  name={'method'}
                  onClick={() => this.onSelectMethod('file')}
                  checked={this.state.method === 'file'}
                  readOnly={true}
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
                  id={'method-url'}
                  name={'method'}
                  onClick={() => this.onSelectMethod('url')}
                  readOnly={true}
                >
                  <div>{this.props.i18nMethodFromUrl}</div>
                  {this.state.method === 'url' && (
                    <div>
                      <FormControl
                        type={'text'}
                        disabled={false}
                        value={this.state.specification}
                        onChange={this.onAddUrlSpecification}
                      />
                      <br />
                      <span className={'url-note'}>
                        {this.props.i18nUrlNote}
                      </span>
                    </div>
                  )}
                </Radio>
                <Radio
                  id={'method-scratch'}
                  name={'method'}
                  onClick={() => this.onSelectMethod('scratch')}
                  readOnly={true}
                >
                  <div>{this.props.i18nMethodFromScratch}</div>
                </Radio>
              </div>
            </FormGroup>
          </Col>

          <ButtonLink
            disabled={!this.state.valid}
            as={'primary'}
            onClick={this.props.onNext(
              this.state.method,
              this.state.specification
            )}
          >
            {this.props.i18nBtnNext}
          </ButtonLink>
        </Row>
      </Grid>
    );
  }
}
