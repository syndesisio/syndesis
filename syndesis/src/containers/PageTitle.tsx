import * as React from 'react';
import Helmet from 'react-helmet';
import { AppContext } from '../app';

export interface IPageTitleProps {
  title: string;
}

export class PageTitle extends React.Component<IPageTitleProps> {
  public render() {
    return (
      <AppContext.Consumer>
        {({ config }) => (
          <Helmet>
            <title>{`${this.props.title} - ${config.title ||
              'Syndesis'}`}</title>
          </Helmet>
        )}
      </AppContext.Consumer>
    );
  }
}
