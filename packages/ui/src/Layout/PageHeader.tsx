import * as React from 'react';
import './PageHeader.css';

export const PageHeader: React.FunctionComponent = ({ children }) => (
  <div className="container-fluid page-header">{children}</div>
);
