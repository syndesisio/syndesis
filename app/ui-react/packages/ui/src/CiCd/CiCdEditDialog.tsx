import { Button } from 'patternfly-react';
import * as React from 'react';
import { Dialog } from '../Shared';

export interface ICiCdEditDialogProps {
  i18nTitle: string;
  i18nDescription: string;
  tagName: string;
  i18nInputLabel: string;
  i18nSaveButtonText: string;
  i18nCancelButtonText: string;
  onHide: () => void;
  onSave: (newName: string) => void;
}

export class CiCdEditDialog extends React.Component<ICiCdEditDialogProps> {
  constructor(props: ICiCdEditDialogProps) {
    super(props);
    this.handleClick = this.handleClick.bind(this);
  }
  public handleClick() {
    this.props.onSave(this.props.tagName);
  }
  public render() {
    return (
      <Dialog
        body={
          <form className="form-horizontal">
            <p>{this.props.i18nDescription}</p>
            <div className="form-group">
              <label className="col-sm-3 control-label" htmlFor="tagNameInput">
                {this.props.i18nInputLabel}
              </label>
              <div className="col-sm-9">
                <input
                  id="tagNameInput"
                  className="form-control"
                  type="text"
                  defaultValue={this.props.tagName}
                />
              </div>
            </div>
          </form>
        }
        footer={
          <>
            <Button onClick={this.props.onHide}>
              {this.props.i18nCancelButtonText}
            </Button>
            <Button bsStyle={'primary'} onClick={this.handleClick}>
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
