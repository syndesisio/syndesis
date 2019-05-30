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

export interface IVirtualizationListProps extends IListViewToolbarProps {
  hasListData: boolean;
  i18nCreateDataVirtualization: string;
  i18nCreateDataVirtualizationTip?: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  /* TD-636: Commented out for TP
  i18nImport: string;
  i18nImportTip: string; */
  i18nLinkCreateVirtualization: string;
  i18nLinkCreateVirtualizationTip?: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
  linkCreateHRef: H.LocationDescriptor;
  /* TD-636: Commented out for TP
  onImport: (name: string) => void; */
}

export class VirtualizationList extends React.Component<
  IVirtualizationListProps
> {
  public constructor(props: IVirtualizationListProps) {
    super(props);
    /* TD-636: Commented out for TP {this.handleImport = this.handleImport.bind(this);} */
  }

  public getCreateVirtualizationTooltip() {
    return (
      <Tooltip id="createTip">
        {this.props.i18nLinkCreateVirtualizationTip
          ? this.props.i18nLinkCreateVirtualizationTip
          : this.props.i18nLinkCreateVirtualization}
      </Tooltip>
    );
  }

  /* TD-636: Commented out for TP 
    public getImportVirtualizationTooltip() {
    return (
      <Tooltip id="importTip">
        {this.props.i18nImportTip
          ? this.props.i18nImportTip
          : this.props.i18nImport}
      </Tooltip>
    ); 
  }

  public handleImport() {
    this.props.onImport('');
  } */

  public render() {
    return (
      <>
        <PageSection noPadding={true} variant={'light'}>
          <ListViewToolbar {...this.props}>
            <div className="form-group">
              {/* TD-636: Commented out for TP 
              <OverlayTrigger
                overlay={this.getImportVirtualizationTooltip()}
                placement="top"
              >
               <Button
                  data-testid={'virtualization-list-import-button'}
                  bsStyle="default"
                  to={this.props.i18nImport}
                  onClick={this.handleImport}
                >
                  {this.props.i18nImport}
                </Button> 
              </OverlayTrigger> */}
              <OverlayTrigger
                overlay={this.getCreateVirtualizationTooltip()}
                placement="top"
              >
                <ButtonLink
                  data-testid={
                    'virtualization-list-create-virtualization-button'
                  }
                  href={this.props.linkCreateHRef}
                  as={'primary'}
                >
                  {this.props.i18nLinkCreateVirtualization}
                </ButtonLink>
              </OverlayTrigger>
            </div>
          </ListViewToolbar>
        </PageSection>
        <PageSection noPadding={true} variant={'light'}>
          {this.props.hasListData ? (
            <ListView>{this.props.children}</ListView>
          ) : (
            <EmptyState>
              <EmptyState.Icon />
              <EmptyState.Title>
                {this.props.i18nEmptyStateTitle}
              </EmptyState.Title>
              <EmptyState.Info>{this.props.i18nEmptyStateInfo}</EmptyState.Info>
              <EmptyState.Action>
                <OverlayTrigger
                  overlay={this.getCreateVirtualizationTooltip()}
                  placement="top"
                >
                  <ButtonLink
                    data-testid={
                      'virtualization-list-empty-state-create-button'
                    }
                    href={this.props.linkCreateHRef}
                    as={'primary'}
                  >
                    {this.props.i18nLinkCreateVirtualization}
                  </ButtonLink>
                </OverlayTrigger>
              </EmptyState.Action>
            </EmptyState>
          )}
        </PageSection>
      </>
    );
  }
}
