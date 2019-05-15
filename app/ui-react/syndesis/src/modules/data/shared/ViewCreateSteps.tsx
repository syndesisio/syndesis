import { WizardStep, WizardSteps } from '@syndesis/ui';
import * as React from 'react';
import i18n from '../../../i18n';

export interface IViewCreateStepsProps {
  /**
   * The one-based active step number.
   */
  step: number;
}

export interface IViewCreateStepsState {
  /**
   * Indicates if the user clicked on a step. Used to show
   * sub-steps when browsing from a mobile device.
   */
  active: boolean;
}

/**
 * A component to display the PatternFly Wizard Steps specific to the create view wizard
 * @see [step]{@link IViewsCreateStepsProps#step}
 * @see [subStep]{@link IViewsCreateStepsProps#subStep}
 */
export class ViewCreateSteps extends React.Component<
  IViewCreateStepsProps,
  IViewCreateStepsState
> {
  public state = {
    active: false,
  };

  constructor(props: IViewCreateStepsProps) {
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
              title={i18n.t('data:virtualization.createViewWizardStep1')}
            />
            <WizardStep
              isActive={this.props.step === 2}
              isDisabled={this.props.step < 2}
              onClick={this.toggleActive}
              step={2}
              title={i18n.t('data:virtualization.createViewWizardStep2')}
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
              title={i18n.t('data:virtualization.createViewWizardStep1')}
            />
            <WizardStep
              isActive={this.props.step === 2}
              isDisabled={this.props.step < 2}
              isAlt={true}
              onClick={this.toggleActive}
              step={2}
              title={i18n.t('data:virtualization.createViewWizardStep2')}
            />
          </>
        }
      />
    );
  }
}
