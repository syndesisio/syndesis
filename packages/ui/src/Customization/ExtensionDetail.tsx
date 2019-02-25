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
import './ExtensionDetail.css';

export interface IExtensionDetailProps {
  extensionName: string;
  extensionUses: number;
  i18nDelete: string;
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

export class ExtensionDetail extends React.Component<IExtensionDetailProps> {
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

  public render() {
    return (
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
                    onClick={this.props.onDelete}
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
    );
  }
}
