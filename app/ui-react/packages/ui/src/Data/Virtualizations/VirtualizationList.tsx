import * as H from 'history';
import {
  Button,
  EmptyState,
  ListView,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../../Layout';
import {
  IListViewToolbarProps,
  ListViewToolbar,
  SimplePageHeader,
} from '../../Shared';

export interface IVirtualizationListProps extends IListViewToolbarProps {
  hasListData: boolean;
  i18nCreateDataVirtualization: string;
  i18nCreateDataVirtualizationTip?: string;
  i18nDescription: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nImport: string;
  i18nImportTip: string;
  i18nLinkCreateVirtualization: string;
  i18nLinkCreateVirtualizationTip?: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
  i18nTitle: string;
  linkCreateHRef: H.LocationDescriptor;
  onImport: (name: string) => void;
}

export class VirtualizationList extends React.Component<
  IVirtualizationListProps
> {
  public constructor(props: IVirtualizationListProps) {
    super(props);
    this.handleImport = this.handleImport.bind(this);
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
  }

  public render() {
    return (
      <>
        <SimplePageHeader
          i18nTitle={this.props.i18nTitle}
          i18nDescription={this.props.i18nDescription}
        />
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <OverlayTrigger
              overlay={this.getImportVirtualizationTooltip()}
              placement="top"
            >
              <Button
                bsStyle="default"
                to={this.props.i18nImport}
                onClick={this.handleImport}
              >
                {this.props.i18nImport}
              </Button>
            </OverlayTrigger>
            <OverlayTrigger
              overlay={this.getCreateVirtualizationTooltip()}
              placement="top"
            >
              <ButtonLink href={this.props.linkCreateHRef} as={'primary'}>
                {this.props.i18nLinkCreateVirtualization}
              </ButtonLink>
            </OverlayTrigger>
          </div>
        </ListViewToolbar>
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
                <ButtonLink href={this.props.linkCreateHRef} as={'primary'}>
                  {this.props.i18nLinkCreateVirtualization}
                </ButtonLink>
              </OverlayTrigger>
            </EmptyState.Action>
          </EmptyState>
        )}
      </>
    );
  }
}
