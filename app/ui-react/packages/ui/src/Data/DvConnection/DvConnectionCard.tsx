// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  Card,
  CardBody,
  CardHeader,
  Text,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import './DvConnectionCard.css';
import { DvConnectionStatus } from './DvConnectionStatus';

export enum ConnectionStatus {
  ACTIVE = 'ACTIVE',
  FAILED = 'FAILED',
  INACTIVE = 'INACTIVE',
}

export interface IDvConnectionCardProps {
  name: string;
  description: string;
  dvStatusMessage?: string;
  dvStatus: string;
  i18nRefreshInProgress: string;
  i18nStatusErrorPopoverTitle: string;
  i18nStatusErrorPopoverLink: string;
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
        <DvConnectionStatus 
          dvStatus={props.dvStatus}
          dvStatusMessage={props.dvStatusMessage}
          i18nRefreshInProgress={props.i18nRefreshInProgress}
          i18nStatusErrorPopoverTitle={props.i18nStatusErrorPopoverTitle}
          i18nStatusErrorPopoverLink={props.i18nStatusErrorPopoverLink}
          loading={props.loading}
        />
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
