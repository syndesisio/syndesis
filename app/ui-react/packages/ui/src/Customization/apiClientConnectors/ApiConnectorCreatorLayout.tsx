import * as React from 'react';

export interface IApiClientConnectorCreatorLayoutProps {
  content: React.ReactNode;
  footer?: React.ReactNode;
  navigation: React.ReactNode;
  toggle: React.ReactNode | React.ReactNode[];
}

/**
 * Provides the layout for the API Create Connection page.
 *
 */
export const ApiConnectorCreatorLayout: React.FunctionComponent<
  IApiClientConnectorCreatorLayoutProps
> = (
  {
    content,
    footer,
    navigation,
    toggle
  }: IApiClientConnectorCreatorLayoutProps) => {
  return (
    <section className={'pf-c-page__main-wizard'}>
      <div className={'pf-c-wizard pf-m-in-page'}>
        {toggle}
        <div className={'pf-c-wizard__outer-wrap'}>
          <div className={'pf-c-wizard__inner-wrap'}>
            <nav className={'pf-c-wizard__nav'}>{navigation}</nav>
            <main className={'pf-c-wizard__main'}>
              <div className={'pf-c-wizard__main-body'}>{content}</div>
            </main>
            {footer && (
              <footer className={'pf-c-wizard__footer'}>{footer}</footer>
            )}
          </div>
        </div>
      </div>
    </section>
  );
};
