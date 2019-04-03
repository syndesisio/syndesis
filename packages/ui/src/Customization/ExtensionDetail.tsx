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
  /**
   * The name of the extension.
   */
  extensionName: string;

  /**
   * The number of integrations that use this extension.
   */
  extensionUses: number;

  /**
   * The localized text of the cancel button used when deleting this extension.
   */
  i18nCancelText: string;

  /**
   * The localized text of the delete button.
   */
  i18nDelete: string;

  /**
   * The localized delete confirmation message.
   */
  i18nDeleteModalMessage: string;

  /**
   * The localized title of the delete dialog.
   */
  i18nDeleteModalTitle: string;

  /**
   * The localized tooltip of the delete button.
   */
  i18nDeleteTip?: string;

  /**
   * The localized message that displays the extension ID.
   */
  i18nIdMessage: string;

  /**
   * The localized text for the overview section title.
   */
  i18nOverviewSectionTitle: string;

  /**
   * The localized text of the supports section title.
   */
  i18nSupportsSectionTitle: string;

  /**
   * The localized text of the update button.
   */
  i18nUpdate: string;

  /**
   * The localized tooltip of the update button.
   */
  i18nUpdateTip?: string;

  /**
   * The localized text of the usage section title.
   */
  i18nUsageSectionTitle: string;

  /**
   * The JSX element that displayes the integrations used by this extension.
   */
  integrationsSection: JSX.Element;

  /**
   * The callback for when the delete button is clicked.
   */
  onDelete: () => void;

  /**
   * The callback for when the update button is clicked.
   */
  onUpdate: () => void;

  /**
   * The JSX element that displays the overview section.
   */
  overviewSection: JSX.Element;

  /**
   * The JSX element that displays the supports section.
   */
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
