import { Text, Title } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import {
  EmptyState,
  ListView,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, PageSection } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

export interface IExtensionListViewProps extends IListViewToolbarProps {
  i18nDescription: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLinkImportExtension: H.LocationDescriptor;
  i18nLinkImportExtensionTip?: H.LocationDescriptor;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
  i18nTitle: string;
  linkImportExtension: H.LocationDescriptor;
}

export class ExtensionListView extends React.Component<
  IExtensionListViewProps
> {
  public getImportTooltip() {
    return (
      <Tooltip id="importTip">
        {this.props.i18nLinkImportExtensionTip
          ? this.props.i18nLinkImportExtensionTip
          : this.props.i18nLinkImportExtension}
      </Tooltip>
    );
  }

  public render() {
    return (
      <PageSection>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <OverlayTrigger overlay={this.getImportTooltip()} placement="top">
              <ButtonLink
                data-testid={'extension-list-view-import-button'}
                href={this.props.linkImportExtension}
                as={'primary'}
              >
                {this.props.i18nLinkImportExtension}
              </ButtonLink>
            </OverlayTrigger>
          </div>
        </ListViewToolbar>
        {this.props.i18nTitle !== '' && (
          <Title size="lg">{this.props.i18nTitle}</Title>
        )}
        {this.props.i18nDescription !== '' && (
          <Text
            dangerouslySetInnerHTML={{ __html: this.props.i18nDescription }}
          />
        )}
        {this.props.children ? (
          <ListView>{this.props.children}</ListView>
        ) : (
          <EmptyState>
            <EmptyState.Icon />
            <EmptyState.Title>
              {this.props.i18nEmptyStateTitle}
            </EmptyState.Title>
            <EmptyState.Info>{this.props.i18nEmptyStateInfo}</EmptyState.Info>
            <EmptyState.Action>
              <OverlayTrigger overlay={this.getImportTooltip()} placement="top">
                <ButtonLink
                  data-testid={'extension-list-view-empty-state-import-button'}
                  href={this.props.linkImportExtension}
                  as={'primary'}
                >
                  {this.props.i18nLinkImportExtension}
                </ButtonLink>
              </OverlayTrigger>
            </EmptyState.Action>
          </EmptyState>
        )}
      </PageSection>
    );
  }
}
