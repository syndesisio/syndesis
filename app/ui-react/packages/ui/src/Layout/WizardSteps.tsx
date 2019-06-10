import * as React from 'react';
import './WizardSteps.css';

interface IWizardStepsProps {
  mainSteps: React.ReactNode;
  altSteps: React.ReactNode;
  /**
   * Indicates if the user clicked on a step. Used to show
   * sub-steps when browsing from a mobile device.
   */
  active: boolean;
}

export class WizardSteps extends React.Component<IWizardStepsProps> {
  public render() {
    return (
      <div className={'wizard-pf-steps'}>
        <ul
          className={`wizard-pf-steps-indicator wizard-pf-steps-alt-indicator ${
            this.props.active ? 'active' : ''
          }`}
          style={{
            borderTop: '0 none',
          }}
        >
          {this.props.mainSteps}
        </ul>
        <ul
          className={`wizard-pf-steps-alt ${this.props.active ? '' : 'hidden'}`}
        >
          {this.props.altSteps}
        </ul>
      </div>
    );
  }
}
