import * as React from 'react';
import Helmet from 'react-helmet';
import { AppContext } from '../app';

export interface IPageTitleProps {
  /**
   * The page title. Don't append or prepend the application name, the right one
   * will be added automatically.
   */
  title: string;
}

/**
 * A component to set the title in the document metadata. This title will be
 * visible in the browser's title bar, tab and history.
 * @see [title]{@link IPageTitleProps#title}
 */
export class PageTitle extends React.PureComponent<IPageTitleProps> {
  public render() {
    return (
      <AppContext.Consumer>
        {({ config }) => {
          const productName = config.branding.productBuild
            ? 'Fuse Online'
            : 'Syndesis';
          return (
            <Helmet>
              <title>
                {`${this.props.title} - ${config.title || productName}`}
              </title>
            </Helmet>
          );
        }}
      </AppContext.Consumer>
    );
  }
}
