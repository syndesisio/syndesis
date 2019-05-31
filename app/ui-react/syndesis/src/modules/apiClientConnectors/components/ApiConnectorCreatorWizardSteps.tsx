import { WizardStep, WizardSteps } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';

export interface IApiConnectorWizardStepsProps {
  /**
   * The one-based active step number.
   */
  step: number;
}

export interface IApiConnectorWizardStepsState {
  /**
   * Indicates if the user clicked on a step. Used to show
   * sub-steps when browsing from a mobile device.
   */
  active: boolean;
}

/**
 * A component to display the PatternFly Wizard Steps specific to the integration
 * creator flow.
 * @see [step]{@link IApiConnectorWizardStepsProps#step}
 * @see [subStep]{@link IApiConnectorWizardStepsProps#subStep}
 */
export class ApiConnectorCreatorWizardSteps extends React.Component<
  IApiConnectorWizardStepsProps,
  IApiConnectorWizardStepsState
> {
  public state = {
    active: false,
  };

  constructor(props: IApiConnectorWizardStepsProps) {
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
      <Translation ns={['apiClientConnectors', 'shared']}>
        {t => (
          <WizardSteps
            active={this.state.active}
            mainSteps={
              <>
                <WizardStep
                  isActive={this.props.step === 1}
                  isDisabled={this.props.step < 1}
                  onClick={this.toggleActive}
                  step={1}
                  title={t('apiClientConnectors:create:selectMethod:title')}
                />
                <WizardStep
                  isActive={this.props.step === 2}
                  isDisabled={this.props.step < 2}
                  onClick={this.toggleActive}
                  step={2}
                  title={t('apiClientConnectors:create:review:title')}
                />
                <WizardStep
                  isActive={this.props.step === 3}
                  isDisabled={this.props.step < 3}
                  onClick={this.toggleActive}
                  step={3}
                  title={t('apiClientConnectors:create:security:title')}
                />
                <WizardStep
                  isActive={this.props.step === 4}
                  isDisabled={this.props.step < 4}
                  onClick={this.toggleActive}
                  step={4}
                  title={t('apiClientConnectors:create:details:title')}
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
                  title={t('apiClientConnectors:create:selectMethod:title')}
                />
                <WizardStep
                  isActive={this.props.step === 2}
                  isDisabled={this.props.step < 2}
                  isAlt={true}
                  onClick={this.toggleActive}
                  step={2}
                  title={t('apiClientConnectors:create:review:title')}
                />
                <WizardStep
                  isActive={this.props.step === 3}
                  isDisabled={this.props.step < 3}
                  isAlt={true}
                  onClick={this.toggleActive}
                  step={3}
                  title={t('apiClientConnectors:create:security:title')}
                />
                <WizardStep
                  isActive={this.props.step === 4}
                  isDisabled={this.props.step < 4}
                  isAlt={true}
                  onClick={this.toggleActive}
                  step={4}
                  title={t('apiClientConnectors:create:details:title')}
                />
              </>
            }
          />
        )}
      </Translation>
    );
  }
}
