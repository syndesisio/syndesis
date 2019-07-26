// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import { Expandable } from '@patternfly/react-core';
import * as React from 'react';

/**
 * @param i18nHidePreview - text for hide preview toggle control
 * @param i18nShowPreview - text for show preview toggle control
 * @param initialExpanded - 'true' if preview is to be expanded initially
 * @param onPreviewExpandedChanged - handle changes in expansion
 */
export interface IExpandablePreviewProps {
  i18nHidePreview: string;
  i18nShowPreview: string;
  initialExpanded?: boolean;
  onPreviewExpandedChanged: (
    previewExpanded: boolean
  ) => void;
}

/**
 * Expandable component for display of preview data
 */
export const ExpandablePreview: React.FunctionComponent<
  IExpandablePreviewProps
> = ({
  i18nHidePreview,
  i18nShowPreview,
  initialExpanded = true,
  onPreviewExpandedChanged,
}: IExpandablePreviewProps) => {

  const [expanded, setExpanded] = React.useState(initialExpanded);

  const toggleExpanded = () => {
    setExpanded(!expanded);
    onPreviewExpandedChanged(!expanded);
  };

  return (
    <Expandable toggleText={expanded ? i18nHidePreview : i18nShowPreview} onToggle={toggleExpanded} isExpanded={expanded}>
      Preview Results Shown Here
    </Expandable>
  );
};
