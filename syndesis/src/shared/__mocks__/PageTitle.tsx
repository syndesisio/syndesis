import * as React from 'react';
import { IPageTitleProps } from '../PageTitle';

export const PageTitle: React.FunctionComponent<IPageTitleProps> = ({
  title,
}) => <span data-testid="page-title">{title}</span>;
