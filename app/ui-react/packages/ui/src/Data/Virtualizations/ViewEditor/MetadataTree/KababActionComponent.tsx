import {
  Dropdown,
  DropdownItem,
  DropdownPosition,
  KebabToggle,
} from '@patternfly/react-core';
import * as React from 'react';
import './KababActionComponent.css';

export interface IKababActionComponentProps {
  textData: string;
}

export const KababActionComponent: React.FunctionComponent<IKababActionComponentProps> = props => {
  const [isOpen, setIsOpen] = React.useState<boolean>(false);

  const dropdownItems = [
    /* tslint:disable-next-line: no-console
         tslint:disable-next-line: jsx-no-lambda */
    <DropdownItem key="link" onClick={() => console.log(props.textData)}>
      Copy to Editor
    </DropdownItem>,
    <DropdownItem key="action" component="button">
      Get Query
    </DropdownItem>,
  ];

  // tslint:disable-next-line: no-shadowed-variable
  const onToggle = (isOpen: boolean) => {
    setIsOpen(isOpen);
  };
  const onSelect = (event: any) => {
    setIsOpen(!isOpen);
    onFocus();
  };
  const onFocus = () => {
    const element = document.getElementById('toggle-id-kabab-1');
    // tslint:disable-next-line: no-unused-expression
    element && element.focus();
  };
  return (
    <Dropdown
      onSelect={onSelect}
      toggle={<KebabToggle onToggle={onToggle} id="toggle-id-kabab-1" />}
      position={DropdownPosition.right}
      isOpen={isOpen}
      isPlain={true}
      dropdownItems={dropdownItems}
      className={'kabab-action-component'}
    />
  );
};
