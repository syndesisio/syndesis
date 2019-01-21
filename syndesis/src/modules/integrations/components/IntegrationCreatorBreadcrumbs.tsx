import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import * as H from 'history';
import * as React from 'react';
import { Link } from 'react-router-dom';
import resolvers from '../resolvers';

export interface IIntegrationCreatorBreadcrumbsProps {
  step: number;
  subStep?: number;
  finishActionId?: string;
  finishConnection?: ConnectionOverview;
  integration?: Integration;
  startAction?: Action;
  startConnection?: ConnectionOverview;
}

export interface IIntegrationCreatorBreadcrumbsState {
  active: boolean;
}

interface IWizardStepProps {
  isCurrentStep: boolean;
  onClick: () => any;
  labelOnly: boolean;
  stepLabel: any;
  getHref: () => H.LocationDescriptor;
}
const WizardStep: React.FunctionComponent<IWizardStepProps> = ({
  isCurrentStep,
  onClick,
  labelOnly,
  stepLabel,
  getHref,
}) => (
  <li
    className={`wizard-pf-step ${isCurrentStep ? 'active' : ''}`}
    onClick={onClick}
  >
    {labelOnly ? stepLabel : <Link to={getHref()}>{stepLabel}</Link>}
  </li>
);

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
    const {
      step,
      subStep = 0,
      integration,
      startAction,
      startConnection,
      finishActionId,
      finishConnection,
    } = this.props;
    const steps = [
      (index: number, isCurrentStep: boolean, labelOnly: boolean) => {
        const stepLabel = (
          <>
            <span className={'wizard-pf-step-number'}>1</span>
            <span className={'wizard-pf-step-title'}>Start connection</span>
            <span className={`wizard-pf-step-title-substep ${subStep === 1}`}>
              Select action
            </span>
            <span className={`wizard-pf-step-title-substep ${subStep === 2}`}>
              Configure action
            </span>
          </>
        );
        const getHref = () =>
          isCurrentStep
            ? resolvers.create.start.selectConnection()
            : resolvers.create.start.configureAction({
                actionId: startAction!.id!,
                connection: startConnection!,
              });
        return (
          <WizardStep
            isCurrentStep={isCurrentStep}
            onClick={this.toggleActive}
            labelOnly={labelOnly}
            stepLabel={stepLabel}
            getHref={getHref}
            key={1}
          />
        );
      },
      (index: number, isCurrentStep: boolean, labelOnly: boolean) => {
        const stepLabel = (
          <>
            <span className={'wizard-pf-step-number'}>2</span>
            <span className={'wizard-pf-step-title'}>Finish connection</span>
            <span className={`wizard-pf-step-title-substep ${subStep === 1}`}>
              Select action
            </span>
            <span className={`wizard-pf-step-title-substep ${subStep === 2}`}>
              Configure action
            </span>
          </>
        );
        const getHref = () =>
          isCurrentStep
            ? resolvers.create.finish.selectConnection({
                integration: integration!,
                startAction: startAction!,
                startConnection: startConnection!,
              })
            : resolvers.create.finish.configureAction({
                actionId: finishActionId!,
                finishConnection: finishConnection!,
                integration: integration!,
                startAction: startAction!,
                startConnection: startConnection!,
              });
        return (
          <WizardStep
            isCurrentStep={isCurrentStep}
            onClick={this.toggleActive}
            labelOnly={labelOnly}
            stepLabel={stepLabel}
            getHref={getHref}
            key={2}
          />
        );
      },
      (index: number, isCurrentStep: boolean, labelOnly: boolean) => {
        const stepLabel = (
          <>
            <span className={'wizard-pf-step-number'}>3</span>
            <span className={'wizard-pf-step-title'}>Add to integration</span>
          </>
        );
        const getHref = () =>
          resolvers.create.configure.index({
            integration: integration!,
          });
        return (
          <WizardStep
            isCurrentStep={isCurrentStep}
            onClick={this.toggleActive}
            labelOnly={labelOnly}
            stepLabel={stepLabel}
            getHref={getHref}
            key={3}
          />
        );
      },
    ];
    const subSteps = [
      [
        (
          active: boolean,
          labelOnly: boolean,
          label: string = '1A. Select action'
        ) => (
          <li
            className={`wizard-pf-step-alt-substep ${
              active ? 'active' : 'disabled'
            }`}
            key={1}
          >
            <Link
              to={
                labelOnly
                  ? '#'
                  : resolvers.create.start.selectAction({
                      connection: startConnection!,
                    })
              }
            >
              {label}
            </Link>
          </li>
        ),
        (
          active: boolean,
          labelOnly: boolean,
          label: string = '1B. Configure action'
        ) => (
          <li
            className={`wizard-pf-step-alt-substep ${
              active ? 'active' : 'disabled'
            }`}
            key={2}
          >
            <Link
              to={
                labelOnly
                  ? '#'
                  : resolvers.create.start.configureAction({
                      actionId: startAction!.id!,
                      connection: startConnection!,
                    })
              }
            >
              {label}
            </Link>
          </li>
        ),
      ],
      [
        (
          active: boolean,
          labelOnly: boolean,
          label: string = '2A. Select action'
        ) => (
          <li
            className={`wizard-pf-step-alt-substep ${
              active ? 'active' : 'disabled'
            }`}
            key={1}
          >
            <Link
              to={
                labelOnly
                  ? '#'
                  : resolvers.create.finish.selectAction({
                      finishConnection: finishConnection!,
                      integration: integration!,
                      startAction: startAction!,
                      startConnection: startConnection!,
                    })
              }
            >
              {label}
            </Link>
          </li>
        ),
        (
          active: boolean,
          labelOnly: boolean,
          label: string = '2B. Configure action'
        ) => (
          <li
            className={`wizard-pf-step-alt-substep ${
              active ? 'active' : 'disabled'
            }`}
            key={2}
          >
            <a>{label}</a>
          </li>
        ),
      ],
    ];
    const stepsAlt = [
      (index: number, isCurrentStep: boolean, labelOnly: boolean) => {
        const stepLabel = (
          <>
            <span className={'wizard-pf-step-alt-number'}>1</span>
            <span className={'wizard-pf-step-alt-title'}>Start connection</span>
          </>
        );
        return (
          <li
            className={`wizard-pf-step-alt ${isCurrentStep ? 'active' : ''}`}
            key={1}
          >
            {labelOnly ? (
              stepLabel
            ) : (
              <>
                <Link to={resolvers.create.start.selectConnection()} key={2}>
                  {stepLabel}
                </Link>
                <ul>
                  {subSteps[0].map((l, subIndex) =>
                    l(subIndex === subStep - 1, subIndex >= subStep - 1)
                  )}
                </ul>
              </>
            )}
          </li>
        );
      },
      (index: number, isCurrentStep: boolean, labelOnly: boolean) => {
        const stepLabel = (
          <>
            <span className={'wizard-pf-step-alt-number'}>2</span>
            <span className={'wizard-pf-step-alt-title'}>
              Finish connection
            </span>
          </>
        );
        return (
          <li
            className={`wizard-pf-step-alt ${isCurrentStep ? 'active' : ''}`}
            key={2}
          >
            {labelOnly ? (
              stepLabel
            ) : (
              <>
                <Link
                  to={resolvers.create.finish.selectConnection({
                    integration: integration!,
                    startAction: startAction!,
                    startConnection: startConnection!,
                  })}
                  key={5}
                >
                  {stepLabel}
                </Link>
                <ul>
                  {subSteps[1].map((l, subIndex) =>
                    l(subIndex === subStep - 1, subIndex >= subStep - 1)
                  )}
                </ul>
              </>
            )}
          </li>
        );
      },
      (index: number, isCurrentStep: boolean, labelOnly: boolean) => {
        const stepLabel = (
          <>
            <span className={'wizard-pf-step-alt-number'}>3</span>
            <span className={'wizard-pf-step-alt-title'}>
              Add to integration
            </span>
          </>
        );
        return (
          <li
            className={`wizard-pf-step-alt ${isCurrentStep ? 'active' : ''}`}
            key={3}
          >
            {!isCurrentStep ? (
              stepLabel
            ) : (
              <Link
                to={resolvers.create.configure.index({
                  integration: integration!,
                })}
              >
                {stepLabel}
              </Link>
            )}
          </li>
        );
      },
    ];

    return (
      <div className={'wizard-pf-steps'}>
        <ul
          className={`wizard-pf-steps-indicator wizard-pf-steps-alt-indicator ${
            this.state.active ? 'active' : ''
          }`}
        >
          {steps.map((l, index) =>
            l(index, index === step - 1, index > step - 1)
          )}
        </ul>
        <ul
          className={`wizard-pf-steps-alt ${this.state.active ? '' : 'hidden'}`}
        >
          {stepsAlt.map((l, index) =>
            l(index, index === step - 1, index > step - 1)
          )}
        </ul>
      </div>
    );
  }
}
