import classNames from 'classnames';
import * as React from 'react';

import './HttpMethodColors.css';

export interface IHttpMethodColorsProps {
  httpMethod: string;
}

/**
 * Component that returns an HTTP method
 * with the properly formatted color.
 * Accepts a string for the HTTP method in question.
 */
export class HttpMethodColors extends React.Component<IHttpMethodColorsProps> {
  public render() {
    const httpMethodClass = classNames({
      'http-method--delete': this.props.httpMethod === 'DELETE',
      'http-method--get': this.props.httpMethod === 'GET',
      'http-method--post': this.props.httpMethod === 'POST',
      'http-method--put': this.props.httpMethod === 'PUT',
    });

    return <span className={httpMethodClass}>{this.props.httpMethod}</span>;
  }
}
