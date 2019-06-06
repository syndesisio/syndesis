import * as React from 'react';

export interface IApiClientConnectorCreatorLayoutProps {
  header: React.ReactNode;
  content: React.ReactNode;
}

/**
 * Provides the layout for the integration editor. It uses the PatternFly Wizard
 * component under the hood.
 * The footer is pre-defined and follows the PF Wizard pattern, with
 * Cancel/Previous/Next buttons.
 *
 * @todo in the CSS we use hardcoded values for the heights of various
 * elements of the page to be able to size the element to take all the available
 * height and show the right scrollbars.
 * We should really find a smarter way to handle this.
 */
export const ApiConnectorCreatorLayout: React.FunctionComponent<
  IApiClientConnectorCreatorLayoutProps
> = ({ header, content }: IApiClientConnectorCreatorLayoutProps) => {
  return (
    <div className={'integration-editor-layout'}>
      <div
        className={
          'integration-editor-layout__header api-connector-creator-layout__header'
        }
      >
        {header}
      </div>
      <div className={'integration-editor-layout__body'}>
        <div className={'integration-editor-layout__contentOuter'}>
          <div className={'integration-editor-layout__contentInner'}>
            {content}
          </div>
        </div>
      </div>
    </div>
  );
};
