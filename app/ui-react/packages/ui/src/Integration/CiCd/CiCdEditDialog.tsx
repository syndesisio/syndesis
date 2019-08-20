import { Text, TextContent, TextVariants } from '@patternfly/react-core';
import { Button } from 'patternfly-react';
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

export class CiCdEditDialog extends React.Component<
  ICiCdEditDialogProps,
  ICiCdEditDialogState
> {
  constructor(props: ICiCdEditDialogProps) {
    super(props);
    this.state = {
      tagName: this.props.tagName,
    };
    this.handleClick = this.handleClick.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }
  public handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    const name = event.target.value.trim();
    this.setState({ tagName: name }, () => this.props.onValidate(name));
  }
  public handleClick() {
    this.props.onSave(this.state.tagName);
  }
  public render() {
    return (
      <Dialog
        body={
          <TextContent data-testid={'cicd-edit-dialog'}>
            <form className="form-horizontal">
              <Text component={TextVariants.p}>
                {this.props.i18nDescription}
              </Text>
              <div
                className={
                  this.props.validationError === TagNameValidationError.NoErrors
                    ? 'form-group'
                    : 'form-group has-error'
                }
              >
                <label
                  className="col-sm-3 control-label"
                  htmlFor="tagNameInput"
                >
                  {this.props.i18nInputLabel}
                </label>
                <div className="col-sm-9">
                  <input
                    id="tagNameInput"
                    data-testid={'cicd-edit-dialog-tag-name'}
                    className="form-control"
                    type="text"
                    defaultValue={this.props.tagName}
                    onChange={this.handleChange}
                  />
                  {this.props.validationError ===
                    TagNameValidationError.NoErrors && (
                    <span className="help-block">
                      &nbsp;{/* todo: pad out the area */}
                    </span>
                  )}
                  {this.props.validationError ===
                    TagNameValidationError.NoName && (
                    <span className="help-block">
                      {this.props.i18nNoNameError}
                    </span>
                  )}
                  {this.props.validationError ===
                    TagNameValidationError.NameInUse && (
                    <span className="help-block">
                      {this.props.i18nNameInUseError}
                    </span>
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
              onClick={this.props.onHide}
            >
              {this.props.i18nCancelButtonText}
            </Button>
            <Button
              data-testid={'cicd-edit-dialog-save-button'}
              bsStyle={'primary'}
              onClick={this.handleClick}
              disabled={
                this.state.tagName === '' ||
                this.props.validationError !== TagNameValidationError.NoErrors
              }
            >
              {this.props.i18nSaveButtonText}
            </Button>
          </>
        }
        onHide={this.props.onHide}
        title={this.props.i18nTitle}
      />
    );
  }
}
