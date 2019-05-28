import classNames from 'classnames';
import * as React from 'react';

import './HttpMethodColors.css';

export interface IHttpMethodColorsProps {
  method: string;
}

/**
 * Component that returns an HTTP method
 * with the properly formatted color.
 * Accepts a string for the HTTP method in question.
 */
export class HttpMethodColors extends React.Component<IHttpMethodColorsProps> {
  public render() {
    const httpMethodClass = classNames({
      'http-method--delete': this.props.method === 'DELETE',
      'http-method--get': this.props.method === 'GET',
      'http-method--post': this.props.method === 'POST',
      'http-method--put': this.props.method === 'PUT',
    });

    return <span className={httpMethodClass}>{this.props.method}</span>;
  }
}
