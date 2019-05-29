import {
  Level,
  LevelItem,
  PageSection,
  Text,
  TextContent,
  Title,
  TitleLevel,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import {
  Button,
  Card,
  CardBody,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../../Layout';
import { Container } from '../../Layout/Container';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
} from '../../Shared';
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
   * An href to use when the extension is being updated.
   */
  linkUpdateExtension: H.LocationDescriptor;

  /**
   * The callback for when the delete button is clicked.
   */
  onDelete: () => void;

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
        <ConfirmationDialog
          buttonStyle={ConfirmationButtonStyle.DANGER}
          i18nCancelButtonText={this.props.i18nCancelText}
          i18nConfirmButtonText={this.props.i18nDelete}
          i18nConfirmationMessage={this.props.i18nDeleteModalMessage}
          i18nTitle={this.props.i18nDeleteModalTitle}
          icon={ConfirmationIconType.DANGER}
          showDialog={this.state.showDeleteDialog}
          onCancel={this.doCancel}
          onConfirm={this.doDelete}
        />
        <PageSection variant={'light'}>
          <Level gutter={'sm'}>
            <TextContent>
              <Title
                size="xl"
                headingLevel={TitleLevel.h1}
                className="extension-detail__extensionTitle"
              >
                {this.props.extensionName}
              </Title>
              <Text className="extension-detail__extensionId">
                {this.props.i18nIdMessage}
              </Text>
            </TextContent>
            <LevelItem className="extension-detail__titleButtons">
              <OverlayTrigger overlay={this.getUpdateTooltip()} placement="top">
                <ButtonLink
                  data-testid={'extension-detail-update-button'}
                  href={this.props.linkUpdateExtension}
                  as={'primary'}
                >
                  {this.props.i18nUpdate}
                </ButtonLink>
              </OverlayTrigger>
              <OverlayTrigger overlay={this.getDeleteTooltip()} placement="top">
                <Button
                  bsStyle="default"
                  disabled={this.props.extensionUses !== 0}
                  onClick={this.showDeleteDialog}
                >
                  {this.props.i18nDelete}
                </Button>
              </OverlayTrigger>
            </LevelItem>
          </Level>
        </PageSection>
        <PageSection>
          <Card>
            <CardBody>
              <TextContent>
                <Title
                  headingLevel="h5"
                  size="md"
                  className="customization-details__heading"
                >
                  {this.props.i18nOverviewSectionTitle}
                </Title>
                <Container>{this.props.overviewSection}</Container>

                <Title
                  headingLevel="h5"
                  size="md"
                  className="customization-details__heading"
                >
                  {this.props.i18nSupportsSectionTitle}
                </Title>
                <Container>{this.props.supportsSection}</Container>

                <Title
                  headingLevel="h5"
                  size="md"
                  className="customization-details__heading"
                >
                  {this.props.i18nUsageSectionTitle}
                </Title>
                <Container>{this.props.integrationsSection}</Container>
              </TextContent>
            </CardBody>
          </Card>
        </PageSection>
      </>
    );
  }
}
