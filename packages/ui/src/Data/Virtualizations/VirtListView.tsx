import {
  Button,
  EmptyState,
  ListView,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

export interface IVirtsListViewProps extends IListViewToolbarProps {
  i18nCreateDataVirt: string;
  i18nCreateDataVirtTip?: string;
  i18nDescription: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nImport: string;
  i18nImportTip: string;
  i18nLinkCreateVirt: string;
  i18nLinkCreateVirtTip?: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
  i18nTitle: string;
  linkCreateHRef: string;
  onCreate: (name: string) => void;
  onImport: (name: string) => void;
}

export class VirtListView extends React.Component<IVirtsListViewProps> {
  public constructor(props: IVirtsListViewProps) {
    super(props);
    this.handleImport = this.handleImport.bind(this);
  }

  public getCreateVirtTooltip() {
    return (
      <Tooltip id="createTip">
        {this.props.i18nLinkCreateVirtTip
          ? this.props.i18nLinkCreateVirtTip
          : this.props.i18nLinkCreateVirt}
      </Tooltip>
    );
  }

  public getImportVirtTooltip() {
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
        <h2>{this.props.i18nTitle}</h2>
        <h3>{this.props.i18nDescription}</h3>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <OverlayTrigger
              overlay={this.getImportVirtTooltip()}
              placement="top"
            >
              <Button
                psStyle="default"
                to={this.props.i18nImport}
                onClick={this.handleImport}
              >
                {this.props.i18nImport}
              </Button>
            </OverlayTrigger>
            <OverlayTrigger
              overlay={this.getCreateVirtTooltip()}
              placement="top"
            >
              <Link
                to={this.props.linkCreateHRef}
                className={'btn btn-primary'}
              >
                {this.props.i18nLinkCreateVirt}
              </Link>
            </OverlayTrigger>
          </div>
        </ListViewToolbar>
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
              <OverlayTrigger
                overlay={this.getCreateVirtTooltip()}
                placement="top"
              >
                <Link
                  to={this.props.linkCreateHRef}
                  className={'btn btn-primary'}
                >
                  {this.props.i18nLinkCreateVirt}
                </Link>
              </OverlayTrigger>
            </EmptyState.Action>
          </EmptyState>
        )}
      </>
    );
  }
}
