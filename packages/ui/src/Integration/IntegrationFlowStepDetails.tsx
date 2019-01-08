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
  action: string;
  dataTypeI18nLabel: string;
  dataType: string;
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
    <dl className={'integration-flow-step-details__overview'}>
      <dt>{actionI18nLabel}</dt>
      <dd>{action}</dd>
    </dl>
    <dl className={'integration-flow-step-details__overview'}>
      <dt>{dataTypeI18nLabel}</dt>
      <dd>{dataType}</dd>
    </dl>
  </div>
);

export interface IntegrationFlowStepDetailsChildrenProps {
  Title: React.FunctionComponent;
  GenericDescription: React.FunctionComponent;
  StepOverview: React.FunctionComponent<IStepOverviewProps>;
}

export interface IntegrationFlowStepDetailsProps {
  active?: boolean;
  children(props: IntegrationFlowStepDetailsChildrenProps): any;
}

export class IntegrationFlowStepDetails extends React.Component<
  IntegrationFlowStepDetailsProps
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
