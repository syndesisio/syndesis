import {
  Radio,
  Split,
  SplitItem,
  Stack,
  StackItem,
  TextInput,
} from '@patternfly/react-core';
import * as React from 'react';
import { ButtonLink } from '../../../Layout';
import { DndFileChooser } from '../../../Shared/DndFileChooser';
import './ApiConnectorCreatorSelectMethod.css';

export type ApiConnectorCreatorMethod =
  | 'file'
  | 'url'
  | 'scratch2x'
  | 'scratch3x';
const FILE = 'file';
const URL = 'url';

export interface IApiConnectorCreatorSelectMethodProps {
  disableDropzone: boolean;
  fileExtensions?: string;
  /**
   * Localized strings to be displayed.
   */
  i18nBtnNext: string;
  i18nHelpMessage?: string;
  i18nInstructions: string;
  i18nMethodFromFile: string;
  i18nMethodFromUrl: string;
  // These aren't needed if allowFromScratch is false
  i18nNoFileSelectedMessage: string;
  i18nSelectedFileLabel: string;
  i18nUploadFailedMessage?: string;
  i18nUploadSuccessMessage?: string;
  i18nUrlNote: string;
  /**
   * The action fired when the user presses the Next button
   */
  onNext(specification?: string, connectorTemplateId?: string): void;
}

export const ApiConnectorCreatorSelectMethod: React.FunctionComponent<IApiConnectorCreatorSelectMethodProps> = ({
  disableDropzone,
  fileExtensions,
  i18nBtnNext,
  i18nHelpMessage,
  i18nInstructions,
  i18nMethodFromFile,
  i18nMethodFromUrl,
  i18nNoFileSelectedMessage,
  i18nSelectedFileLabel,
  i18nUploadFailedMessage,
  i18nUploadSuccessMessage,
  i18nUrlNote,
  onNext,
}) => {
  const [connectorTemplateId, setConnectorTemplateId] = React.useState('');
  const [method, setMethod] = React.useState(FILE);
  const [specification, setSpecification] = React.useState('');
  const [url, setUrl] = React.useState('');
  const [uploadSuccessMessage, setUploadSuccessMessage] = React.useState('');
  const [uploadFailedMessage, setUploadFailedMessage] = React.useState('');

  /**
   * Helper function used to build the D&D upload success/fail
   * messages, which are subsequently set in the UI state
   * @param fileName - The name of the file that was uploaded
   * @param succeeded - Boolean value that specifies whether or not the
   * upload was successful.
   */
  const buildUploadMessage = (fileName: string, succeeded: boolean): void => {
    if (succeeded && fileName) {
      setUploadSuccessMessage(i18nUploadSuccessMessage + "'" + fileName + "'");
    } else {
      setUploadFailedMessage("'" + fileName + "'" + i18nUploadFailedMessage);
    }
  };

  const isValidUrl = (toCheck: string): boolean => {
    const regexp = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
    return regexp.test(toCheck);
  };

  /**
   * Formats file extension,
   * checks if it's a WSDL file
   * @param fileName
   */
  const isWsdlFile = (fileName: string): boolean => {
    const fileExt = fileName
      .split('.')
      .pop()!
      .toLowerCase();
    return fileExt === 'wsdl' || fileExt.includes('?wsdl');
  };

  /**
   * User has added a specification via a string URL, which will be
   * checked if is a valid HTTP/HTTPS string.
   * @param e
   */
  const onAddUrlSpecification = (e: React.FormEvent<HTMLInputElement>) => {
    const newUrl = e.currentTarget.value;

    if (isWsdlFile(newUrl)) {
      setConnectorTemplateId('soap-connector-template');
    }

    setUrl(newUrl);
  };

  /**
   * The action fired when the user selects the method
   * to provide an OpenAPI specification.
   * @param newMethod
   */
  const onSelectMethod = (newMethod: ApiConnectorCreatorMethod) => {
    setMethod(newMethod);
    setSpecification('');
    setUploadFailedMessage('');
    setUploadSuccessMessage('');
  };

  /**
   * Callback for when one or more file uploads have been accepted.
   */
  const onUploadAccepted = (files: File[]): void => {
    if (isWsdlFile(files[0].name)) {
      setConnectorTemplateId('soap-connector-template');
    }
    const reader = new FileReader();
    reader.readAsText(files[0]);
    buildUploadMessage(files[0].name, true);

    reader.onload = () => {
      setSpecification(reader.result as string);
    };
  };

  /**
   * Obtains the localized text (may include HTML tags) that appears when the file upload was rejected. This
   * will occur when a DnD of a file with the wrong extension is dropped. This message is presented
   * as a timed toast notification.
   * @param fileName - Name of file that failed to be uploaded
   */
  const onUploadRejected = (fileName: string): string => {
    buildUploadMessage(fileName, false);
    setSpecification('');
    return `<span>File <strong>${fileName}</strong> could not be uploaded</span>`;
  };

  const handleClickNext = () => {
    if (method === URL) {
      onNext(url, connectorTemplateId);
    } else {
      onNext(specification, connectorTemplateId);
    }
  };

  const handleAddSpec = (val: any, evt: React.FormEvent<HTMLInputElement>) =>
    onAddUrlSpecification(evt);
  const handleSelectFile = () => onSelectMethod(FILE);
  const handleSelectUrl = () => onSelectMethod(URL);

  const isDisabled = () => {
    if (method === FILE) {
      return specification === '';
    }
    if (method === URL) {
      return !isValidUrl(url);
    }
    return false;
  };

  return (
    <Stack
      className={'api-connector-creator-select-method'}
      data-testid={'openapi-select-method'}
    >
      <StackItem>
        <Split onClick={handleSelectFile}>
          <SplitItem>
            <Radio
              aria-label={'From File'}
              id={'method-file'}
              data-testid={'method-file'}
              name={'method'}
              onClick={handleSelectFile}
              isChecked={method === FILE}
              readOnly={true}
            />
          </SplitItem>
          <SplitItem>
            <div>{i18nMethodFromFile}</div>
            <div className="api-connector-creator-select-method__dnd-container">
              <DndFileChooser
                allowMultiple={false}
                disableDropzone={disableDropzone || method !== FILE}
                fileExtensions={fileExtensions}
                i18nHelpMessage={i18nHelpMessage}
                i18nInstructions={i18nInstructions}
                i18nNoFileSelectedMessage={i18nNoFileSelectedMessage}
                i18nSelectedFileLabel={i18nSelectedFileLabel}
                i18nUploadFailedMessage={uploadFailedMessage}
                i18nUploadSuccessMessage={uploadSuccessMessage}
                onUploadAccepted={onUploadAccepted}
                onUploadRejected={onUploadRejected}
              />
            </div>
          </SplitItem>
        </Split>
      </StackItem>
      <StackItem>
        <Split onClick={handleSelectUrl}>
          <SplitItem>
            <Radio
              aria-label={'from url radio'}
              id={'method-url'}
              data-testid={'method-url'}
              name={'method'}
              label={<></>}
              isChecked={method === URL}
              onClick={handleSelectUrl}
              readOnly={true}
            />
          </SplitItem>
          <SplitItem>
            <div>{i18nMethodFromUrl}</div>
            <div
              className={'api-connector-creator-select-method__url-container'}
            >
              <TextInput
                aria-label={'method url text input'}
                id={'method-url-text-input'}
                data-testid={'method-url-text-input'}
                type={'text'}
                isDisabled={method !== URL}
                value={url}
                onChange={handleAddSpec}
              />
              <br />
              <span className={'url-note'}>{i18nUrlNote}</span>
            </div>
          </SplitItem>
        </Split>
      </StackItem>
      <StackItem>
        <ButtonLink
          id={'button-next'}
          data-testid={'button-next'}
          disabled={isDisabled()}
          as={'primary'}
          onClick={handleClickNext}
        >
          {i18nBtnNext}
        </ButtonLink>
      </StackItem>
    </Stack>
  );
};
