import { Button, ButtonVariant, Text, TextContent, TextVariants } from '@patternfly/react-core';
import * as React from 'react';
import { Dialog } from '../../Shared';
import { TagNameValidationError } from './CiCdUIModels';

export interface ICiCdEditDialogProps {
  i18nTitle: string;
  i18nDescription: string;
  tagName: string;
  i18nInputLabel: string;
  i18nSaveButtonText: string;
  i18nCancelButtonText: string;
  i18nNoNameError: string;
  i18nNameInUseError: string;
  validationError: TagNameValidationError;
  onHide: () => void;
  onValidate: (name: string) => void;
  onSave: (newName: string) => void;
}

export interface ICiCdEditDialogState {
  tagName: string;
}

export const CiCdEditDialog: React.FC<ICiCdEditDialogProps> = ({
  i18nTitle,
  i18nDescription,
  tagName,
  i18nInputLabel,
  i18nSaveButtonText,
  i18nCancelButtonText,
  i18nNoNameError,
  i18nNameInUseError,
  validationError,
  onHide,
  onValidate,
  onSave,
}) => {
  const [theTagName, setTagName] = React.useState(tagName);

  const handleChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const name = event.target.value.trim();
    setTagName(name);
    onValidate(name);
  };

  const handleClick = () => {
    onSave(theTagName);
  };

  return (
    <Dialog
      body={
        <TextContent data-testid={'cicd-edit-dialog'}>
          <form className="form-horizontal">
            <Text component={TextVariants.p}>{i18nDescription}</Text>
            <div
              className={
                validationError === TagNameValidationError.NoErrors
                  ? 'form-group'
                  : 'form-group has-error'
              }
            >
              <label className="col-sm-3 control-label" htmlFor="tagNameInput">
                {i18nInputLabel}
              </label>
              <div className="col-sm-9">
                <input
                  id="tagNameInput"
                  data-testid={'cicd-edit-dialog-tag-name'}
                  className="form-control"
                  type="text"
                  defaultValue={tagName}
                  onChange={handleChange}
                />
                {validationError === TagNameValidationError.NoErrors && (
                  <span className="help-block">
                    &nbsp;{/* todo: pad out the area */}
                  </span>
                )}
                {validationError === TagNameValidationError.NoName && (
                  <span className="help-block">{i18nNoNameError}</span>
                )}
                {validationError === TagNameValidationError.NameInUse && (
                  <span className="help-block">{i18nNameInUseError}</span>
                )}
              </div>
            </div>
          </form>
        </TextContent>
      }
      footer={
        <>
          <Button
            data-testid={'cicd-edit-dialog-cancel-button'}
            variant={ButtonVariant.secondary}
            onClick={onHide}
          >
            {i18nCancelButtonText}
          </Button>
          <Button
            data-testid={'cicd-edit-dialog-save-button'}
            variant={ButtonVariant.primary}
            onClick={handleClick}
            isDisabled={
              theTagName === '' ||
              validationError !== TagNameValidationError.NoErrors
            }
          >
            {i18nSaveButtonText}
          </Button>
        </>
      }
      onHide={onHide}
      title={i18nTitle}
    />
  );
};
