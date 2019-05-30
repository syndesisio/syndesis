import { ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';

export interface ITagIntegrationListItemProps {
  selected: boolean;
  name: string;
  onChange: (name: string, selected: boolean) => void;
}

export class TagIntegrationListItem extends React.Component<
  ITagIntegrationListItemProps
> {
  constructor(props: ITagIntegrationListItemProps) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
  }
  public handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    this.props.onChange(this.props.name, event.target.checked);
  }
  public render() {
    return (
      <ListViewItem
        checkboxInput={
          <input
            data-testid={`tag-integration-list-item-${toValidHtmlId(
              this.props.name
            )}-selected-input`}
            type="checkbox"
            defaultChecked={this.props.selected}
            onChange={this.handleChange}
          />
        }
        heading={this.props.name}
        description={''}
        additionalInfo={[]}
      />
    );
  }
}
