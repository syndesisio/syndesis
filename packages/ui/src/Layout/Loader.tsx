import classnames from 'classnames';
import { Spinner } from 'patternfly-react';
import * as React from 'react';

import './Loader.css';

export interface ILoaderProps {
  inline?: boolean;
  inverse?: boolean;
  loading?: boolean;
  size?: 'lg' | 'md' | 'sm' | 'xs';
}

export class Loader extends React.PureComponent<ILoaderProps> {
  public static defaultProps = {
    inline: false,
    inverse: false,
    loading: true,
    size: 'lg',
  };

  public render() {
    return (
      <div
        className={classnames('Loader', {
          'is-block': !this.props.inline,
          'is-inline': this.props.inline,
        })}
      >
        <Spinner
          loading={this.props.loading}
          size={this.props.size}
          inline={this.props.inline}
          inverse={this.props.inverse}
        />
      </div>
    );
  }
}
