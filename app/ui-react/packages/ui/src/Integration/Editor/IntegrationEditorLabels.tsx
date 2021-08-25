import { Select, SelectOption, SelectVariant } from '@patternfly/react-core';
import * as React from 'react';

interface IIntegrationEditorLabelsProps {
  initialLabels: string[];
  onSelectLabels: (labels: string[]) => void;
}

/**
 * Valid format: alphanumeric=alphanumeric
 * e.g. Rachel=pizza, lex=hotdogs123
 * @param input
 */
const validateLabel = (input: string): boolean => {
  const regexIncludeEqual = /(^\w+)(=)(\w+$)/g;
  return regexIncludeEqual.test(input);
};

export const IntegrationEditorLabels: React.FunctionComponent<IIntegrationEditorLabelsProps> =
  ({ initialLabels, onSelectLabels }) => {
    const [labels, setLabels] = React.useState(initialLabels);
    const [isOpen, setIsOpen] = React.useState(false);
    const labelRef = React.useRef(labels);
    const isValid = React.useRef(true);

    React.useEffect(() => {
      if (labelRef.current === labels) {
        return;
      } else {
        labelRef.current = labels;
        onSelectLabels(labelRef.current);
      }
    }, [labels]);

    const onCreateOption = (newValue: string) => {
      // don't create if it's invalid
      if (!validateLabel(newValue)) {
        isValid.current = false;
        return;
      }
    };

    const onToggle = (isOpenNew: boolean) => {
      setIsOpen(isOpenNew);
    };

    const onSelect = (
      event: React.MouseEvent | React.ChangeEvent,
      value: any
    ) => {
      if (labels.includes(value)) {
        setLabels(labels.filter((item) => item !== value));
      } else if (validateLabel(value)) {
        setLabels([...labels, value]);
      }

      setIsOpen(false);
    };

    const clearSelection = () => {
      setLabels([]);
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
            selections={labels}
            typeAheadAriaLabel={placeholderText}
            validated={isValid.current ? 'default' : 'error'}
            variant={SelectVariant.typeaheadMulti}
          >
            {initialLabels &&
              initialLabels.map((option, index) => (
                <SelectOption key={index} value={option} />
              ))}
          </Select>
          {!isValid && <p>Please use the following format: key=value</p>}
        </div>
      </>
    );
  };
