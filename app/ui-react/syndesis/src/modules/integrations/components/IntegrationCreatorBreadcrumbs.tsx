import { WizardStep, WizardSteps } from '@syndesis/ui';
import * as React from 'react';

export interface IIntegrationCreatorBreadcrumbsProps {
  /**
   * The one-based active step number.
   */
  step: number;
  /**
   * Optional. The one-based active sub-step number.
   */
  subStep?: number;
}

export interface IIntegrationCreatorBreadcrumbsState {
  /**
   * Indicates if the user clicked on a step. Used to show
   * sub-steps when browsing from a mobile device.
   */
  active: boolean;
}

/**
 * A component to display the PatternFly Wizard Steps specific to the integration
 * creator flow.
 * @see [step]{@link IIntegrationCreatorBreadcrumbsProps#step}
 * @see [subStep]{@link IIntegrationCreatorBreadcrumbsProps#subStep}
 */
export class IntegrationCreatorBreadcrumbs extends React.Component<
  IIntegrationCreatorBreadcrumbsProps,
  IIntegrationCreatorBreadcrumbsState
> {
  public state = {
    active: false,
  };

  constructor(props: IIntegrationCreatorBreadcrumbsProps) {
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
              title={'Start connection'}
              subSteps={[
                'Choose connection',
                'Choose action',
                'Configure action',
              ]}
              activeSubstep={this.props.subStep}
            />
            <WizardStep
              isActive={this.props.step === 2}
              isDisabled={this.props.step < 2}
              onClick={this.toggleActive}
              step={2}
              title={'Finish connection'}
              subSteps={[
                'Choose connection',
                'Choose action',
                'Configure action',
              ]}
              activeSubstep={this.props.subStep}
            />
            <WizardStep
              isActive={this.props.step === 3}
              isDisabled={this.props.step < 3}
              onClick={this.toggleActive}
              step={3}
              title={'Add to integration'}
            />
            <WizardStep
              isActive={this.props.step === 4}
              isDisabled={this.props.step < 4}
              onClick={this.toggleActive}
              step={4}
              title={'Create an integration'}
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
              title={'Start connection'}
              subSteps={[
                '1A. Choose connection',
                '1B. Choose action',
                '1C. Configure action',
              ]}
              activeSubstep={this.props.subStep}
            />
            <WizardStep
              isActive={this.props.step === 2}
              isDisabled={this.props.step < 2}
              isAlt={true}
              onClick={this.toggleActive}
              step={2}
              title={'Finish connection'}
              subSteps={[
                '2A. Choose connection',
                '2B. Choose action',
                '2C. Configure action',
              ]}
              activeSubstep={this.props.subStep}
            />
            <WizardStep
              isActive={this.props.step === 3}
              isDisabled={this.props.step < 3}
              isAlt={true}
              onClick={this.toggleActive}
              step={3}
              title={'Add to integration'}
            />
            <WizardStep
              isActive={this.props.step === 4}
              isDisabled={this.props.step < 4}
              isAlt={true}
              onClick={this.toggleActive}
              step={4}
              title={'Create an integration'}
            />
          </>
        }
      />
    );
  }
}
