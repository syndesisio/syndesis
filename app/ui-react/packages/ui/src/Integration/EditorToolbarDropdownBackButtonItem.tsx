import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink } from '../Layout';

import './EditorToolbarDropdownBackButtonItem.css';

export interface IEditorToolbarDropdownBackButtonItemProps {
  title: string;
  href: H.LocationDescriptor;
}
export const EditorToolbarDropdownBackButtonItem: React.FunctionComponent<
  IEditorToolbarDropdownBackButtonItemProps
> = ({ href, title }) => (
  <>
    <div className="editor-toolbar-dropdown-back-button-item__back-button-wrapper">
      <ButtonLink
        data-testid="editor-toolbar-dropdown-back-button-item-back-button"
        as="default"
        href={href}
      >
        {title}
      </ButtonLink>
    </div>
    <div className="pf-c-dropdown__separator" role="separator" />
  </>
);
