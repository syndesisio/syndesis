import { Grid, Icon } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../helpers';
import { Container } from '../Layout/Container';
import './DndFileChooser.css';
import { INotification, INotificationType } from './Notifications';
import { WithDropzone } from './WithDropzone';

/**
 * The properties of the `DndFileChooser`.
 */
export interface IDndFileChooserProps {
  /**
   * Indicates if multiple files can be added. Defaults to `false`.
   */
  allowMultiple?: boolean;

  /**
   * `true` if the dropzone should be disabled. Defaults to `false`.
   */
  disableDropzone: boolean;

  /**
   * A comma delimited string of file extensions. For example, '.jar,.txt'. Defaults to any file extension.
   */
  fileExtensions?: string;

  /**
   * The localized text that appears below the selected file information at the bottom of the drop area.
   */
  i18nHelpMessage?: string;

  /**
   * The localized text (may include HTML tags) that appears above the selected file information at the
   * top of the drop area.
   */
  i18nInstructions: string;

  /**
   * The localized text that appears when no file has been selected.
   */
  i18nNoFileSelectedMessage: string;

  /**
   * The localized text for the label that appears before the selected file information.
   */
  i18nSelectedFileLabel: string;

  /**
   * A general, localized message for when a file upload failed. This message will be shown
   * along with an error icon and appears next to the selected file message. If this property
   * is set then `i18nUploadSuccessMessage` should not be set.
   */
  i18nUploadFailedMessage?: string;

  /**
   * A list of error messages for failed uploads
   */
  i18nUploadFailedMessages?: string[];

  /**
   * A general, localized message for when a file upload was successful. This message will be shown
   * along with an OK icon and appear next to the selected file message. If this property
   * is set then `i18nUploadFailedMessage` should not be set.
   */
  i18nUploadSuccessMessage?: string;

  /**
   * A list of error messages for successful uploads
   */
  i18nUploadSuccessMessages?: string[];

  /**
   * Obtains the localized text (may include HTML tags) that appears when the file upload was rejected. This
   * will occur when a DnD of a file with the wrong extension is dropped. This message is presented
   * as a timed toast notification.
   */
  onUploadRejected(fileName: string): string;

  /**
   * Callback for when one or more file uploads have been accepted. Caller should handle processing of the files.
   */
  onUploadAccepted(file: File[]): void;
}

/**
 * The state properties of the `DndFileChooser`.
 */
export interface IDndFileChooserState {
  /**
   * The files that were uploaded successfully. Defaults to an empty array.
   */
  files: File[];

  /**
   * The error notifications for rejected files. After a toast is displayed for a notification it is removed
   * from the array.
   */
  notifications: INotification[];
}

/**
 * A component that can be used to upload files. Files can be uploaded by clicking in the drop zone or by
 * drag and dropping files into the drop zone.
 */
export class DndFileChooser extends React.Component<
  IDndFileChooserProps,
  IDndFileChooserState
> {
  // setup default prop values
  public static defaultProps = {
    allowMultiple: false,
  };

  public constructor(props: IDndFileChooserProps) {
    super(props);

    // set up initial state
    this.state = {
      files: [],
      notifications: [],
    };

    this.handleAcceptedFiles = this.handleAcceptedFiles.bind(this);
    this.handleRejectedFiles = this.handleRejectedFiles.bind(this);
  }

  /**
   * Obtains an element that lists the names of the files that have been uploaded.
   */
  public getSelectedFileMessage(): JSX.Element {
    // one file uploaded
    if (this.state.files.length === 1) {
      return <Container>{this.state.files[0].name}</Container>;
    }

    // multiple files uploaded
    if (this.state.files.length > 1) {
      return (
        <ul>
          {this.state.files.map((file, index) => (
            <li
              data-testid={`dnd-file-chooser-${toValidHtmlId(
                file.name
              )}-list-item`}
              key={index}
            >
              {file.name}
            </li>
          ))}
        </ul>
      );
    }

    // no files uploaded
    return <Container>{this.props.i18nNoFileSelectedMessage}</Container>;
  }

  /**
   * Obtains an element that indicates the if the upload was successful.
   */
  public getUploadMessage(): JSX.Element {
    if (this.props.i18nUploadSuccessMessage) {
      return (
        <span className="dnd-file-chooser__uploadMessage">
          <Icon type="pf" name="ok" />
          &nbsp;{this.props.i18nUploadSuccessMessage}
        </span>
      );
    }
    if (this.props.i18nUploadFailedMessage) {
      return (
        <span className="dnd-file-chooser__uploadMessage">
          <Icon type="pf" name="error-circle-o" />
          &nbsp;{this.props.i18nUploadFailedMessage}
        </span>
      );
    }
    if (
      this.props.i18nUploadSuccessMessages &&
      this.props.i18nUploadFailedMessages
    ) {
      return (
        <ul>
          {this.props.i18nUploadSuccessMessages!.map((message, idx) => (
            <li
              data-testid={`dnd-file-chooser-${toValidHtmlId(
                message
              )}-success-message`}
              key={'success' + idx}
              className="dnd-file-chooser__uploadMessage"
            >
              <Icon type="pf" name="ok" />
              &nbsp;{message}
            </li>
          ))}
          {this.props.i18nUploadFailedMessages!.map((message, idx) => (
            <li
              data-testid={`dnd-file-chooser-${toValidHtmlId(
                message
              )}-failed-message-${idx}`}
              key={'fail' + idx}
              className="dnd-file-chooser__uploadMessage"
            >
              <Icon type="pf" name="error-circle-o" />
              &nbsp;{message}
            </li>
          ))}
        </ul>
      );
    }
    // no message
    return <Container />;
  }

  /**
   * Callback for when one or more files were uploaded successfully.
   * @param acceptedFiles the files that have been uploaded
   */
  public handleAcceptedFiles(acceptedFiles: File[]) {
    this.setState({ ...this.state, files: acceptedFiles });
    this.props.onUploadAccepted(acceptedFiles); // notify callback
  }

  /**
   * Callback for when one or more files were rejected.
   * @param rejectedFiles the files that were rejected
   */
  public handleRejectedFiles(rejectedFiles: File[]) {
    const notifications = rejectedFiles.map(file => ({
      key: file.name,
      message: this.props.onUploadRejected(file.name),
      persistent: false,
      type: 'error' as INotificationType,
    }));

    // If single file dropped then all files will be cleared. If multiple allowed, and multiple dropped,
    // and some are accepted and some are rejected, the accepted files will still be uploaded because the
    // handleAcceptedFiles is called after this method.
    this.setState({
      ...this.state,
      files: [],
      notifications: [...this.state.notifications, ...notifications],
    });
  }

  public render() {
    return (
      <WithDropzone
        fileExtensions={this.props.fileExtensions}
        disableDropzone={this.props.disableDropzone}
        allowMultiple={this.props.allowMultiple}
        onDropAccepted={this.handleAcceptedFiles}
        onDropRejected={this.handleRejectedFiles}
      >
        {({ getRootProps, getInputProps }) => (
          <Grid
            disabled={this.props.disableDropzone}
            fluid={true}
            className="dnd-file-chooser"
            {...getRootProps({ refKey: 'dnd-file-chooser' })}
          >
            <Grid.Row>
              <Grid.Col
                className="dnd-file-chooser__instructions"
                dangerouslySetInnerHTML={{
                  __html: this.props.i18nInstructions,
                }}
              />
            </Grid.Row>
            <Grid.Row>
              <Grid.Col>
                <input {...getInputProps()} />
              </Grid.Col>
            </Grid.Row>
            <Grid.Row>
              <Grid.Col className="dnd-file-chooser__selectedFileLabel" xs={3}>
                {this.props.i18nSelectedFileLabel}
              </Grid.Col>
              <Grid.Col className="dnd-file-chooser__selectedFile" xs={6}>
                {this.getSelectedFileMessage()}
              </Grid.Col>
              <Grid.Col xs={3}>{this.getUploadMessage()}</Grid.Col>
            </Grid.Row>
            {this.props.i18nHelpMessage ? (
              <Grid.Row>
                <Grid.Col className="dnd-file-chooser__helpText">
                  <em>{this.props.i18nHelpMessage}</em>
                </Grid.Col>
              </Grid.Row>
            ) : null}
          </Grid>
        )}
      </WithDropzone>
    );
  }
}
