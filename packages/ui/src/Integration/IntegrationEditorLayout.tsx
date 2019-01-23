import classnames from 'classnames';
import * as H from 'history';
import * as React from 'react';
import { Link } from 'react-router-dom';
import './IntegrationEditorLayout.css';
import { Loader } from '../Shared';

interface IButtonOrLinkProps {
  onClick?: (e: React.MouseEvent<any>) => void;
  href?: H.LocationDescriptor;
  className: string;
  disabled?: boolean;
}
const ButtonOrLink: React.FunctionComponent<IButtonOrLinkProps> = ({
  onClick,
  href,
  className,
  disabled,
  children,
}) =>
  href ? (
    <Link to={href} onClick={onClick} className={`btn ${className}`}>
      {children}
    </Link>
  ) : (
    <button
      onClick={onClick}
      className={`btn ${className}`}
      disabled={disabled || (!onClick && !href)}
    >
      {children}
    </button>
  );

export interface IEditorLayoutProps {
  header: JSX.Element;
  sidebar?: JSX.Element;
  content: JSX.Element;
  onCancel?: (e: React.MouseEvent<any>) => void;
  onBack?: (e: React.MouseEvent<any>) => void;
  onNext?: (e: React.MouseEvent<any>) => void;
  cancelHref?: H.LocationDescriptor;
  backHref?: H.LocationDescriptor;
  nextHref?: H.LocationDescriptor;
  isNextLoading?: boolean;
  [key: string]: any;
}

export const IntegrationEditorLayout: React.FunctionComponent<
  IEditorLayoutProps
> = ({
  header,
  sidebar,
  content,
  onCancel,
  onBack,
  onNext,
  cancelHref,
  backHref,
  nextHref,
  isNextLoading,
  ...props
}: IEditorLayoutProps) => {
  return (
    <div
      className={classnames('wizard-pf-body integration-editor-layout', {
        'has-footer': true,
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
      <div className="wizard-pf-footer integration-editor-layout__footer">
        <ButtonOrLink
          onClick={onBack}
          href={backHref}
          className={'btn-default wizard-pf-back'}
        >
          <i className="fa fa-angle-left" /> Back
        </ButtonOrLink>
        <ButtonOrLink
          onClick={onNext}
          href={nextHref}
          className={'btn-primary wizard-pf-next'}
          disabled={isNextLoading}
        >
          {isNextLoading ? <Loader size={'xs'} inline={true} /> : null}
          Next <i className="fa fa-angle-right" />
        </ButtonOrLink>
        <ButtonOrLink
          onClick={onCancel}
          href={cancelHref}
          className={'btn-default wizard-pf-cancel'}
        >
          Cancel
        </ButtonOrLink>
      </div>
    </div>
  );
};
