// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
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
