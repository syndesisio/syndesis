import { Step } from '@syndesis/models';
import { IntegrationFlowAddStep } from '@syndesis/ui';
import * as H from 'history';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IIntegrationEditorStepAdderProps {
  steps: Step[];
  addConnectionHref(idx: number): H.LocationDescriptor;
  addStepHref(idx: number): H.LocationDescriptor;
  configureConnectionHref(stepIdx: number, step: Step): H.LocationDescriptor;
  configureStepHref(stepIdx: number, step: Step): H.LocationDescriptor;
}

export class IntegrationEditorStepAdder extends React.Component<
  IIntegrationEditorStepAdderProps
> {
  public render() {
    return (
      <ListView style={{ background: 'transparent' }}>
        {this.props.steps.map((s, idx) => {
          return (
            <React.Fragment key={idx}>
              <ListView.Item
                heading={s.connection!.connector!.name}
                description={s.action!.name}
                hideCloseIcon={true}
                leftContent={
                  <img src={s.connection!.icon} width={24} height={24} />
                }
                stacked={true}
                actions={
                  <>
                    <Link
                      to={this.props.configureConnectionHref(idx, s)}
                      className={'btn btn-default'}
                    >
                      Configure
                    </Link>
                    <Link to={'#'} className={'btn btn-danger'}>
                      <i className="fa fa-trash" />
                    </Link>
                  </>
                }
              />
              {idx < this.props.steps.length - 1 && (
                <IntegrationFlowAddStep
                  active={false}
                  showDetails={false}
                  addStepHref={this.props.addStepHref(idx + 1)}
                  i18nAddStep={'Add a step'}
                  addConnectionHref={this.props.addConnectionHref(idx + 1)}
                  i18nAddConnection={'Add a connection'}
                />
              )}
            </React.Fragment>
          );
        })}
      </ListView>
    );
  }
}
