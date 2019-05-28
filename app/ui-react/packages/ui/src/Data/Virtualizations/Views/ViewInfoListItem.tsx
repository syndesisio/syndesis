import { Label, ListViewInfoItem, ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';

export interface IViewInfoListItemProps {
  name: string;
  description?: string;
  connectionName: string;
  nodePath: string[];
  selected: boolean;
  i18nUpdate: string;
  isUpdateView: boolean;
  onSelectionChanged: (name: string, selected: boolean) => void;
}

export interface IViewInfoListItemState {
  itemSelected: boolean;
}

export class ViewInfoListItem extends React.Component<
  IViewInfoListItemProps,
  IViewInfoListItemState
> {
  public constructor(props: IViewInfoListItemProps) {
    super(props);
    this.state = {
      itemSelected: props.selected, // initial item selection
    };
    this.handleCheckboxToggle = this.handleCheckboxToggle.bind(this);
  }

  public temp() {
    const tDescription = this.props.description;
    const tConnectionName = this.props.connectionName;
    return tDescription + tConnectionName;
  }

  public getNodePathStr() {
    let path = '';
    for (const segment of this.props.nodePath) {
      path += '/' + segment;
    }
    return path;
  }

  public handleCheckboxToggle = (viewName: string) => (event: any) => {
    this.setState({
      itemSelected: !this.state.itemSelected,
    });
    this.props.onSelectionChanged(viewName, !this.state.itemSelected);
  };

  public render() {
    return (
      <ListViewItem
        data-testid={`view-info-list-item-${toValidHtmlId(
          this.props.name
        )}-list-item`}
        heading={this.props.name}
        description={this.getNodePathStr()}
        checkboxInput={
          <input
            data-testid={'view-info-list-item-selected-input'}
            type="checkbox"
            value=""
            defaultChecked={this.props.selected}
            onChange={this.handleCheckboxToggle(this.props.name)}
          />
        }
        additionalInfo={[
          <ListViewInfoItem key={1}>
            {this.props.isUpdateView === true ? (
              <Label type="warning">{this.props.i18nUpdate}</Label>
            ) : (
              ''
            )}
          </ListViewInfoItem>,
        ]}
        hideCloseIcon={true}
        stacked={false}
      />
    );
  }
}
