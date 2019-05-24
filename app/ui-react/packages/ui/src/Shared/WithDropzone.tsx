import * as React from 'react';
import Dropzone from 'react-dropzone';

export interface IWithDropzoneChildrenProps {
  getRootProps: (props: any) => any;
  getInputProps: () => any;
}

export interface IWithDropzoneProps {
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

  maxSize?: number;
  minSize?: number;

  children: (props: IWithDropzoneChildrenProps) => any;
  /**
   * Obtains the localized text (may include HTML tags) that appears when the file upload was rejected. This
   * will occur when a DnD of a file with the wrong extension is dropped. This message is presented
   * as a timed toast notification.
   */
  onDropRejected(files: File[]): void;

  /**
   * Callback for when one or more file uploads have been accepted. Caller should handler processing of the files.
   */
  onDropAccepted(file: File[]): void;
}

export class WithDropzone extends React.Component<IWithDropzoneProps> {
  public render() {
    return (
      <Dropzone
        accept={this.props.fileExtensions}
        disabled={this.props.disableDropzone}
        multiple={this.props.allowMultiple}
        maxSize={this.props.maxSize}
        minSize={this.props.minSize}
        preventDropOnDocument={true}
        onDropAccepted={this.props.onDropAccepted}
        onDropRejected={this.props.onDropRejected}
      >
        {({ getRootProps, getInputProps }) => {
          return this.props.children({ getRootProps, getInputProps });
        }}
      </Dropzone>
    );
  }
}
