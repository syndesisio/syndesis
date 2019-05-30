import { WizardStep, WizardSteps } from '@syndesis/ui';
import * as React from 'react';

export interface IConnectionCreatorStepsProps {
  /**
   * The one-based active step number.
   */
  step: number;
}

export interface IConnectionCreatorStepsState {
  /**
   * Indicates if the user clicked on a step. Used to show
   * sub-steps when browsing from a mobile device.
   */
  active: boolean;
}

/**
 * A component to display the PatternFly Wizard Steps specific to the integration
 * creator flow.
 * @see [step]{@link IConnectionCreatorStepsProps#step}
 * @see [subStep]{@link IConnectionCreatorStepsProps#subStep}
 */
export class ConnectionCreatorBreadSteps extends React.Component<
  IConnectionCreatorStepsProps,
  IConnectionCreatorStepsState
> {
  public state = {
    active: false,
  };

  constructor(props: IConnectionCreatorStepsProps) {
    super(props);
    this.toggleActive = this.toggleActive.bind(this);
  }

  public toggleActive() {
    this.setState({
      active: !this.state.active,
    });
  }

  public render() {
    return (
      <WizardSteps
        active={this.state.active}
        mainSteps={
          <>
            <WizardStep
              isActive={this.props.step === 1}
              isDisabled={this.props.step < 1}
              onClick={this.toggleActive}
              step={1}
              title={'Select connector'}
            />
            <WizardStep
              isActive={this.props.step === 2}
              isDisabled={this.props.step < 2}
              onClick={this.toggleActive}
              step={2}
              title={'Configure connection'}
            />
            <WizardStep
              isActive={this.props.step === 3}
              isDisabled={this.props.step < 3}
              onClick={this.toggleActive}
              step={3}
              title={'Name connection'}
            />
          </>
        }
        altSteps={
          <>
            <WizardStep
              isActive={this.props.step === 1}
              isDisabled={this.props.step < 1}
              isAlt={true}
              onClick={this.toggleActive}
              step={1}
              title={'Select connector'}
            />
            <WizardStep
              isActive={this.props.step === 2}
              isDisabled={this.props.step < 2}
              isAlt={true}
              onClick={this.toggleActive}
              step={2}
              title={'Configure connection'}
            />
            <WizardStep
              isActive={this.props.step === 3}
              isDisabled={this.props.step < 3}
              isAlt={true}
              onClick={this.toggleActive}
              step={3}
              title={'Name connection'}
            />
          </>
        }
      />
    );
  }
}
