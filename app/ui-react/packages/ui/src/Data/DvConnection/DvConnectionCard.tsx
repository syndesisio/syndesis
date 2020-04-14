// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  Card,
  CardBody,
  CardHeader,
  Label,
  Spinner,
  Text,
  Title,
  Tooltip
} from '@patternfly/react-core';
import { global_active_color_100, global_danger_color_100 } from '@patternfly/react-tokens';
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
  dvStatusTooltip: string;
  dvStatus: string;
  icon: React.ReactNode;
  loading: boolean;
  selected: boolean;
  onSelectionChanged: (connName: string, isSelected: boolean) => void;
}

export const DvConnectionCard: React.FunctionComponent<
  IDvConnectionCardProps
> = props => {
  const doToggleSelected = (connName: string) => (event: any) => {
    // User can only select active connections that are not loading
    if (props.dvStatus === ConnectionStatus.ACTIVE) {
      props.onSelectionChanged(connName, !props.selected);
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
        {props.loading && props.dvStatus !== ConnectionStatus.ACTIVE ? (
          <Spinner size={'lg'} />
        ) : (
          <></>
        )}
        <Tooltip content={props.dvStatusTooltip} position={'bottom'}>
          <Label
            className="dv-connection-card__status"
            style={
              props.dvStatus === ConnectionStatus.ACTIVE
                ? { background: global_active_color_100.value }
                : { background: global_danger_color_100.value }
            }
          >
            {props.dvStatus}
          </Label>
        </Tooltip>
      </CardHeader>
      <CardBody>
        <div className={'dv-connection-card__body'}>
          <div className="dv-connection-card__icon">{props.icon}</div>
          <Title
            className="dv-connection-card__title h2"
            data-testid={'dv-connection-card--title'}
            headingLevel="h2"
            size="lg"
          >
            {props.name}
          </Title>
          <Text className="dv-connection-card__description">
            {props.description}
          </Text>
        </div>
      </CardBody>
    </Card>
  );
};
