import { ListViewItem } from 'patternfly-react';
import * as React from 'react';

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
            type="checkbox"
            defaultChecked={this.props.selected}
            onChange={this.handleChange}
          />
        }
        additionalInfo={this.props.name}
      />
    );
  }
}
