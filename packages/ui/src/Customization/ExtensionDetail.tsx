import {
  Button,
  Card,
  CardBody,
  CardHeading,
  CardTitle,
  OverlayTrigger,
  Row,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { DeleteConfirmationDialog } from '../Shared';
import './ExtensionDetail.css';

export interface IExtensionDetailProps {
  extensionName: string;
  extensionUses: number;
  i18nCancelText: string;
  i18nDelete: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nDeleteTip?: string;
  i18nIdMessage: string;
  i18nOverviewSectionTitle: string;
  i18nSupportsSectionTitle: string;
  i18nUpdate: string;
  i18nUpdateTip?: string;
  i18nUsageSectionTitle: string;
  integrationsSection: JSX.Element;
  onDelete: () => void;
  onUpdate: () => void;
  overviewSection: JSX.Element;
  supportsSection: JSX.Element;
}

export interface IExtensionDetailState {
  showDeleteDialog: boolean;
}

export class ExtensionDetail extends React.Component<
  IExtensionDetailProps,
  IExtensionDetailState
> {
  public constructor(props: IExtensionDetailProps) {
    super(props);

    this.state = {
      showDeleteDialog: false, // initial visibility of delete dialog
    };

    this.doCancel = this.doCancel.bind(this);
    this.doDelete = this.doDelete.bind(this);
    this.showDeleteDialog = this.showDeleteDialog.bind(this);
  }

  public doCancel() {
    this.setState({
      showDeleteDialog: false, // hide dialog
    });
  }

  public doDelete() {
    this.setState({
      showDeleteDialog: false, // hide dialog
    });

    // TODO: disable components while delete is processing
    this.props.onDelete();
  }

  public getDeleteTooltip() {
    return (
      <Tooltip id="deleteTip">
        {this.props.i18nDeleteTip
          ? this.props.i18nDeleteTip
          : this.props.i18nDelete}
      </Tooltip>
    );
  }

  public getUpdateTooltip() {
    return (
      <Tooltip id="updateTip">
        {this.props.i18nUpdateTip
          ? this.props.i18nUpdateTip
          : this.props.i18nUpdate}
      </Tooltip>
    );
  }

  public showDeleteDialog() {
    this.setState({
      showDeleteDialog: true,
    });
  }

  public render() {
    return (
      <>
        <DeleteConfirmationDialog
          i18nCancelButtonText={this.props.i18nCancelText}
          i18nDeleteButtonText={this.props.i18nDelete}
          i18nDeleteMessage={this.props.i18nDeleteModalMessage}
          i18nTitle={this.props.i18nDeleteModalTitle}
          showDialog={this.state.showDeleteDialog}
          onCancel={this.doCancel}
          onDelete={this.doDelete}
        />
        <Card matchHeight={true}>
          <CardHeading>
            <CardTitle>
              <Row>
                <h1 className={'col-sm-8 extension-detail__extensionTitle'}>
                  {this.props.extensionName}
                  <span className={'extension-detail__extensionId'}>
                    {this.props.i18nIdMessage}
                  </span>
                </h1>
                <div className="col-sm-4 text-right extension-detail__titleButtons">
                  <OverlayTrigger
                    overlay={this.getUpdateTooltip()}
                    placement="top"
                  >
                    <Button bsStyle="primary" onClick={this.props.onUpdate}>
                      {this.props.i18nUpdate}
                    </Button>
                  </OverlayTrigger>
                  <OverlayTrigger
                    overlay={this.getDeleteTooltip()}
                    placement="top"
                  >
                    <Button
                      bsStyle="default"
                      disabled={this.props.extensionUses !== 0}
                      onClick={this.showDeleteDialog}
                    >
                      {this.props.i18nDelete}
                    </Button>
                  </OverlayTrigger>
                </div>
              </Row>
            </CardTitle>
          </CardHeading>
          <CardBody>
            <h3 className="extension-detail__sectionHeading">
              {this.props.i18nOverviewSectionTitle}
            </h3>
            {this.props.overviewSection}
            <h3 className="extension-detail__sectionHeading">
              {this.props.i18nSupportsSectionTitle}
            </h3>
            {this.props.supportsSection}
            <h3 className="extension-detail__sectionHeading">
              {this.props.i18nUsageSectionTitle}
            </h3>
            {this.props.integrationsSection}
          </CardBody>
        </Card>
      </>
    );
  }
}
