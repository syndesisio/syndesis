// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import { Card, CardBody, CardFooter } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Loader, PageSection } from '../../../Layout';
import './ViewsImportLayout.css';

/**
 * @param header - a PatternFly Wizard Steps component.
 * @param content - the main content of the wizard. In case of overflow, only
 * the body will scroll.
 * @param onCancel - if passed, the Cancel button will be render as a `button`
 * and this callback will be used as its `onClick` handler.
 * @param onBack - if passed, the Back button will be render as a `button`
 * and this callback will be used as its `onClick` handler.
 * @param onNext - if passed, the Next button will be render as a `button`
 * and this callback will be used as its `onClick` handler.
 * @param onCreateViews - Create views when complete.
 * @param cancelHref - if passed, the Cancel button will be render as a `Link`
 * using this as its `to` parameter.
 * @param backHref - if passed, the Back button will be render as a `Link`
 * using this as its `to` parameter.
 * @param nextHref - if passed, the Next button will be render as a `Link`
 * using this as its `to` parameter.
 * @param isNextLoading - if set to true, a `Loading` component will be shown
 * inside the Next button before its label. The button will also be disabled.
 * @param isNextDisabled - if set to true, the Next button will be disabled.
 * @param isLastStep - if set to true, it changes the Next button label to
 * 'Done'.
 */
export interface IViewsImportLayoutProps {
  header: JSX.Element;
  content: JSX.Element;
  onCancel?: (e: React.MouseEvent<any>) => void;
  onBack?: (e: React.MouseEvent<any>) => void;
  onNext?: (e: React.MouseEvent<any>) => void;
  onCreateViews?: () => Promise<any>;
  cancelHref?: H.LocationDescriptor;
  backHref?: H.LocationDescriptor;
  nextHref?: H.LocationDescriptor;
  isNextDisabled?: boolean;
  isNextLoading?: boolean;
  isLastStep?: boolean;
}

/**
 * Provides the layout for the views import wizard. It uses the PatternFly Wizard
 * component under the hood.
 * The footer is pre-defined and follows the PF Wizard pattern, with
 * Cancel/Previous/Next buttons.
 *
 * @todo in the CSS we use hardcoded values for the heights of various
 * elements of the page to be able to size the element to take all the available
 * height and show the right scrollbars.
 * We should really find a smarter way to handle this.
 */
export const ViewsImportLayout: React.FunctionComponent<
  IViewsImportLayoutProps
> = ({
  header,
  content,
  onCancel,
  onBack,
  onNext,
  onCreateViews,
  cancelHref,
  backHref,
  nextHref,
  isNextLoading,
  isNextDisabled,
  isLastStep = false,
}: IViewsImportLayoutProps) => {
  return (
    <div className={'integration-editor-layout'}>
      <div className={'integration-editor-layout__header'}>{header}</div>
      <div className={'integration-editor-layout__body'}>
        <div className={'integration-editor-layout__contentOuter'}>
          <div className={'integration-editor-layout__contentInner'}>
            <PageSection>
              <Card>
                <CardBody className="views-import-layout-card-body">
                  {content}
                </CardBody>
                <CardFooter className="views-import-layout-card-footer">
                  <ButtonLink
                    data-testid={'views-import-layout-back-button'}
                    onClick={onBack}
                    href={backHref}
                    className={'wizard-pf-back'}
                  >
                    <i className="fa fa-angle-left" /> Back
                  </ButtonLink>
                  &nbsp;
                  <ButtonLink
                    data-testid={'views-import-layout-next-button'}
                    onClick={isLastStep ? onCreateViews : onNext}
                    href={nextHref}
                    as={'primary'}
                    className={'wizard-pf-next'}
                    disabled={isNextLoading || isNextDisabled}
                  >
                    {isNextLoading ? <Loader size={'xs'} inline={true} /> : null}
                    {isLastStep ? (
                      'Done'
                    ) : (
                        <>
                          Next <i className="fa fa-angle-right" />
                        </>
                      )}
                  </ButtonLink>
                  &nbsp;
                  <ButtonLink
                    data-testid={'views-import-layout-cancel-button'}
                    onClick={onCancel}
                    href={cancelHref}
                    className={'wizard-pf-cancel'}
                  >
                    Cancel
                  </ButtonLink>
                </CardFooter>
              </Card>
            </PageSection>
          </div>
        </div>
      </div>
    </div>
  );
};
