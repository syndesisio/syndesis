import { Text } from '@patternfly/react-core';
import { Card, Label } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import './DvConnectionCard.css';

export enum ConnectionStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export interface IDvConnectionCardProps {
  name: string;
  description: string;
  dvStatus: string;
  icon: string;
  selected: boolean;
  onSelectionChanged: (connName: string, isSelected: boolean) => void;
}
export interface IDvConnectionCardState {
  isSelected: boolean;
}

export class DvConnectionCard extends React.PureComponent<
  IDvConnectionCardProps,
  IDvConnectionCardState
> {
  public constructor(props: IDvConnectionCardProps) {
    super(props);
    this.state = {
      isSelected: props.selected, // initial item selection
    };
    this.toggleSelected = this.toggleSelected.bind(this);
  }

  public toggleSelected = (connName: string) => (event: any) => {
    // User can only select active connections
    if (this.props.dvStatus === ConnectionStatus.ACTIVE) {
      this.setState({
        isSelected: !this.state.isSelected,
      });
      this.props.onSelectionChanged(connName, !this.state.isSelected);
    }
  };

  public render() {
    return (
      <div
        className={'dv-connection-card'}
        onClick={this.toggleSelected(this.props.name)}
      >
        <Card
          data-testid={`dv-connection-card-${toValidHtmlId(
            this.props.name
          )}-card`}
          matchHeight={true}
          accented={this.state.isSelected}
        >
          <Card.Body>
            <div className="dv-connection-card__status">
              <Label
                type={
                  this.props.dvStatus === ConnectionStatus.ACTIVE
                    ? 'success'
                    : 'danger'
                }
              >
                {this.props.dvStatus}
              </Label>
            </div>
            <div className={'dv-connection-card__content'}>
              <div className="dv-connection-card__icon">
                <img src={this.props.icon} alt={this.props.name} width={46} />
              </div>
              <div
                className="dv-connection-card__title h2"
                data-testid={'dv-connection-card--title'}
              >
                {this.props.name}
              </div>
              <Text className="dv-connection-card__description">
                {this.props.description}
              </Text>
            </div>
          </Card.Body>
        </Card>
      </div>
    );
  }
}
