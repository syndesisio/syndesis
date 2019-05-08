import { getStepIcon } from '@syndesis/api';
import * as H from '@syndesis/history';
import { Step } from '@syndesis/models';
import {
  ButtonLink,
  IntegrationEditorStepsList,
  IntegrationEditorStepsListItem,
  IntegrationFlowAddStep,
  PageSection,
} from '@syndesis/ui';
import * as React from 'react';
import { getDataShapeText } from './editor/utils';

export interface IIntegrationEditorStepAdderProps {
  /**
   * the list of steps to render.
   */
  steps: Step[];
  /**
   * a callback to get the `LocationDescriptor` that should be reached when
   * clicking the Add Connection button
   * @param idx - the zero-based index where a new connection should be added
   */
  addStepHref: (idx: number) => H.LocationDescriptor;
  /**
   * a callback to get the `LocationDescriptor` that should be reached when
   * clicking the Edit Step button
   * @param stepIdx - the zero-based index of the integration step that should
   * be edited
   * @param step - the integration step object that should be edited
   */
  // tslint:disable-next-line:react-unused-props-and-state
  configureStepHref: (stepIdx: number, step: Step) => H.LocationDescriptor;
}

/**
 * A component to render the steps of an integration with the required action
 * buttons to add a new step, edit an existing one, etc.
 *
 * @see [steps]{@link IIntegrationEditorStepAdderProps#steps}
 * @see [addStepHref]{@link IIntegrationEditorStepAdderProps#addStepHref}
 * @see [addStepHref]{@link IIntegrationEditorStepAdderProps#addStepHref}
 * @see [configureStepHref]{@link IIntegrationEditorStepAdderProps#configureStepHref}
 *
 * @todo add the delete step button
 */
export class IntegrationEditorStepAdder extends React.Component<
  IIntegrationEditorStepAdderProps
> {
  public render() {
    return (
      <PageSection>
        <IntegrationEditorStepsList>
          {this.props.steps.map((s, idx) => {
            const inputDataShape =
              s.action &&
              s.action.descriptor &&
              s.action.descriptor.inputDataShape;
            const outputDataShape =
              s.action &&
              s.action.descriptor &&
              s.action.descriptor.outputDataShape;
            const shape =
              idx === 0
                ? getDataShapeText(s.stepKind!, outputDataShape)
                : getDataShapeText(s.stepKind!, inputDataShape);

            return (
              <React.Fragment key={idx}>
                <IntegrationEditorStepsListItem
                  stepName={s.action ? s.action!.name : s.name!}
                  stepDescription={(s.action! && s.action!.description) || ''}
                  additionalInfo={[
                    <div
                      className={
                        'list-view-pf-additional-info-item-stacked list-view-pf-additional-info-item'
                      }
                      key={0}
                    >
                      <strong>{shape || 'n/a'}</strong>
                      <span>Data shape</span>
                    </div>,
                  ]}
                  icon={
                    <img
                      alt={s.action ? s.action!.name : s.name}
                      src={getStepIcon(process.env.PUBLIC_URL, s)}
                      width={24}
                      height={24}
                    />
                  }
                  actions={
                    <>
                      <ButtonLink href={this.props.configureStepHref(idx, s)}>
                        Configure
                      </ButtonLink>
                      <ButtonLink href={'#'} as={'danger'}>
                        <i className="fa fa-trash" />
                      </ButtonLink>
                    </>
                  }
                />
                {idx < this.props.steps.length - 1 && (
                  <IntegrationFlowAddStep
                    active={false}
                    showDetails={false}
                    addStepHref={this.props.addStepHref(idx + 1)}
                    i18nAddStep={'Add a step'}
                  />
                )}
              </React.Fragment>
            );
          })}
        </IntegrationEditorStepsList>
      </PageSection>
    );
  }
}
