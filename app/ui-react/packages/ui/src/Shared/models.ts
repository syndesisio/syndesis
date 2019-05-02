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
