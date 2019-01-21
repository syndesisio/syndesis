import classnames from 'classnames';
import * as React from 'react';
import './IntegrationEditorLayout.css';

export interface IEditorLayoutProps {
  header: JSX.Element;
  sidebar?: JSX.Element;
  content: JSX.Element;
  footer?: JSX.Element;
  [key: string]: any;
}

export const IntegrationEditorLayout: React.FunctionComponent<
  IEditorLayoutProps
> = ({ header, sidebar, content, footer, ...props }: IEditorLayoutProps) => {
  return (
    <div
      className={classnames('wizard-pf-body integration-editor-layout', {
        'has-footer': !!footer,
      })}
      {...props}
    >
      {header}
      <div className="wizard-pf-row integration-editor-layout__body">
        <div className="wizard-pf-sidebar">{sidebar}</div>
        <div
          className={
            'wizard-pf-main cards-pf integration-editor-layout__contentWrapper'
          }
        >
          <div className="integration-editor-layout__content">{content}</div>
        </div>
      </div>
      {footer && (
        <div className="wizard-pf-footer integration-editor-layout__footer">
          {footer}
        </div>
      )}
    </div>
  );
};
