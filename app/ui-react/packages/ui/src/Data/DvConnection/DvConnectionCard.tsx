// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
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
  icon: React.ReactNode;
  selected: boolean;
  onSelectionChanged: (connName: string, isSelected: boolean) => void;
}

export const DvConnectionCard: React.FunctionComponent<
  IDvConnectionCardProps
> = props => {

  const [isSelected, setIsSelected] = React.useState(props.selected);


  const doToggleSelected = (connName: string) => (event: any) => {
    // User can only select active connections
    if (props.dvStatus === ConnectionStatus.ACTIVE) {
      setIsSelected(!isSelected);
      props.onSelectionChanged(connName, !isSelected);
    }
  };

  return (
    <div
      className={'dv-connection-card'}
      onClick={doToggleSelected(props.name)}
    >
      <Card
        data-testid={`dv-connection-card-${toValidHtmlId(
          props.name
        )}-card`}
        matchHeight={true}
        accented={props.selected}
      >
        <Card.Body>
          <div className="dv-connection-card__status">
            <Label
              type={
                props.dvStatus === ConnectionStatus.ACTIVE
                  ? 'success'
                  : 'danger'
              }
            >
              {props.dvStatus}
            </Label>
          </div>
          <div className={'dv-connection-card__content'}>
            <div className="dv-connection-card__icon">{props.icon}</div>
            <div
              className="dv-connection-card__title h2"
              data-testid={'dv-connection-card--title'}
            >
              {props.name}
            </div>
            <Text className="dv-connection-card__description">
              {props.description}
            </Text>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
}
