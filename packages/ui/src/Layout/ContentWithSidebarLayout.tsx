import * as React from 'react';
import './ContentWithSidebarLayout.css';

export interface IEditorLayoutProps {
  sidebar?: JSX.Element;
  content: JSX.Element;
}

export const ContentWithSidebarLayout: React.FunctionComponent<
  IEditorLayoutProps
> = ({ sidebar, content }: IEditorLayoutProps) => {
  return (
    <div className={'content-with-sidebar'}>
      <div className={'content-with-sidebar__sidebar'}>{sidebar}</div>
      <div className={'content-with-sidebar__content'}>{content}</div>
    </div>
  );
};
