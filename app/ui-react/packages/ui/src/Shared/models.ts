import * as H from '@syndesis/history';
import * as React from 'react';

/**
 * Common interfaces across the UI
 */

export interface IMenuActions {
  href?: H.LocationDescriptor;
  onClick?: (e: React.MouseEvent<any>) => any;
  label: string | JSX.Element;
}

export const ERROR = 'error';
export const DANGER = 'danger';
export const WARNING = 'warning';
export const SUCCESS = 'success';
