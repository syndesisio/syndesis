import { Select, SelectOption, SelectVariant } from '@patternfly/react-core';
import * as React from 'react';

interface IIntegrationEditorLabelsProps {
  labels?: { [key: string]: string };
  onSelect: (something?: any) => void;
}

export const IntegrationEditorLabels: React.FunctionComponent<IIntegrationEditorLabelsProps> =
  ({ labels }) => {
    const initialArray: string[] = [];

    if (labels) {
      Object.entries(labels).map((l, k) => {
        initialArray.push(l[0] + '=' + l[1]);
      });
    }

    const [customLabels, setCustomLabels] = React.useState(initialArray);
    const [isOpen, setIsOpen] = React.useState(false);

    const onCreateOption = (newValue: string) => {
      setCustomLabels([...customLabels, newValue]);
    };

    const onToggle = (isOpenNew: boolean) => {
      setIsOpen(isOpenNew);
    };

    const onSelect = (
      event: React.MouseEvent | React.ChangeEvent,
      value: any
    ) => {
      if (customLabels.includes(value)) {
        setCustomLabels(customLabels.filter((item) => item !== value));
      } else {
        setCustomLabels([...customLabels, value]);
      }
    };

    const clearSelection = () => {
      setCustomLabels([]);
      setIsOpen(false);
    };

    const titleId = 'integration-editor-select';
    const placeholderText = 'Specify a label in this format: key=value';

    return (
      <>
        <div>
          <span id={titleId} hidden={true}>
            {placeholderText}
          </span>
          <Select
            aria-labelledby={titleId}
            isCreatable={true}
            isOpen={isOpen}
            onClear={clearSelection}
            onCreateOption={onCreateOption}
            onSelect={onSelect}
            onToggle={onToggle}
            placeholderText={placeholderText}
            selections={customLabels}
            typeAheadAriaLabel={placeholderText}
            variant={SelectVariant.typeaheadMulti}
          >
            {initialArray.map((option, index) => (
              <SelectOption key={index} value={option} />
            ))}
          </Select>
        </div>
      </>
    );
  };
