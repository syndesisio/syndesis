import * as React from 'react';
import './StoryHelper.css';

export const StoryHelper: React.SFC = ({ children }) => (
  <div className={'container-fluid'}>{children}</div>
);
