// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  Card,
  CardBody,
  CardHeader,
  Text,
  Title,
} from '@patternfly/react-core';
import { Label, Spinner } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import './DvConnectionCard.css';

export enum ConnectionStatus {
  ACTIVE = 'ACTIVE',
  FAILED = 'FAILED',
  INACTIVE = 'INACTIVE',
}

export interface IDvConnectionCardProps {
  name: string;
  description: string;
  dvStatus: string;
  icon: React.ReactNode;
  loading: boolean;
  selected: boolean;
  onSelectionChanged: (connName: string, isSelected: boolean) => void;
}

export const DvConnectionCard: React.FunctionComponent<
  IDvConnectionCardProps
> = props => {
  const [isSelected, setIsSelected] = React.useState(props.selected);

  const doToggleSelected = (connName: string) => (event: any) => {
    // User can only select active connections that are not loading
    if (props.dvStatus === ConnectionStatus.ACTIVE && props.loading === false) {
      setIsSelected(!isSelected);
      props.onSelectionChanged(connName, !isSelected);
    }
  };

  return (
    <Card
      className={
        props.selected ? 'dv-connection-card--accented' : 'dv-connection-card'
      }
      data-testid={`dv-connection-card-${toValidHtmlId(props.name)}-card`}
      onClick={doToggleSelected(props.name)}
    >
      <CardHeader>
        {props.loading ? (
          <Spinner loading={true} inline={true} />
        ) : ( <></> )
        }
        <Label
          className="dv-connection-card__status"
          type={
            props.dvStatus === ConnectionStatus.ACTIVE ? 'success' : 'danger'
          }
        >
          {props.dvStatus}
        </Label>
      </CardHeader>
      <CardBody className="dv-connection-card__content">
        <div className="dv-connection-card__icon">{props.icon}</div>
        <Title
          className="dv-connection-card__title"
          data-testid={'dv-connection-card--title'}
          headingLevel="h2"
          size="lg"
        >
          {props.name}
        </Title>
        <Text className="dv-connection-card__description">
          {props.description}
        </Text>
      </CardBody>
    </Card>
  );
};
