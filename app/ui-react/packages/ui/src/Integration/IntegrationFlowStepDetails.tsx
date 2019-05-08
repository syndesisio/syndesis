import classnames from 'classnames';
import * as React from 'react';
import './IntegrationFlowStepDetails.css';

const Title: React.FunctionComponent = ({ children }) => (
  <div className={'integration-flow-step-details__title'}>{children}</div>
);

const GenericDescription: React.FunctionComponent = ({ children }) => (
  <div className={'integration-flow-step-details__body'}>{children}</div>
);

interface IStepOverviewProps {
  nameI18nLabel: string;
  name: string;
  actionI18nLabel: string;
  action?: string;
  dataTypeI18nLabel: string;
  dataType?: string;
}

const StepOverview: React.FunctionComponent<IStepOverviewProps> = ({
  nameI18nLabel,
  name,
  actionI18nLabel,
  action,
  dataTypeI18nLabel,
  dataType,
}) => (
  <div className={'integration-flow-step-details__body'}>
    <dl className={'integration-flow-step-details__overview'}>
      <dt>{nameI18nLabel}</dt>
      <dd>{name}</dd>
    </dl>
    {action && (
      <dl className={'integration-flow-step-details__overview'}>
        <dt>{actionI18nLabel}</dt>
        <dd>{action}</dd>
      </dl>
    )}
    {dataType && (
      <dl className={'integration-flow-step-details__overview'}>
        <dt>{dataTypeI18nLabel}</dt>
        <dd>{dataType}</dd>
      </dl>
    )}
  </div>
);

export interface IIntegrationFlowStepDetailsChildrenProps {
  Title: React.FunctionComponent;
  GenericDescription: React.FunctionComponent;
  StepOverview: React.FunctionComponent<IStepOverviewProps>;
}

export interface IIntegrationFlowStepDetailsProps {
  active?: boolean;
  children(props: IIntegrationFlowStepDetailsChildrenProps): any;
}

/**
 * A render prop component that provides the right components than can be used
 * inside a step element of the integration editor sidebar.
 */
export class IntegrationFlowStepDetails extends React.Component<
  IIntegrationFlowStepDetailsProps
> {
  public static defaultProps = {
    active: false,
  };

  public render() {
    return (
      <div
        className={classnames('integration-flow-step-details', {
          'is-active': this.props.active,
        })}
      >
        {this.props.children({
          GenericDescription,
          StepOverview,
          Title,
        })}
      </div>
    );
  }
}
