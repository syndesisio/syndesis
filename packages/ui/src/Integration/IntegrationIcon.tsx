import * as React from 'react';
import './IntegrationIcon.css';

export interface IIntegrationIconProps {
  startConnectionIcon: string;
  finishConnectionIcon: string;
}
export const IntegrationIcon: React.FunctionComponent<
  IIntegrationIconProps
> = ({ startConnectionIcon, finishConnectionIcon }) => (
  <div className={'integration-icon'}>
    <img src={startConnectionIcon} className={'integration-icon__icon'} />
    <i className="fa fa-angle-right integration-icon__divider" />
    <img src={finishConnectionIcon} className={'integration-icon__icon'} />
  </div>
);
