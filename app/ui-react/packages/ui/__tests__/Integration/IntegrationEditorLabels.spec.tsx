import '@testing-library/dom';
import '@testing-library/jest-dom/extend-expect';
import { render } from '@testing-library/react';
import * as React from 'react';
import { IntegrationEditorLabels } from '../../src/Integration';

function renderComponent(props) {
  const utils = render(<IntegrationEditorLabels {...props} />);
  const labelSelector = utils.getByTestId('integration-label-select');
  return { ...utils, labelSelector };
}

export default describe('IntegrationEditorLabels.tsx', () => {
  // when there are no pre-existing labels
  it('the selector should load without pre-existing labels', () => {
    const labels = [];
    const onSelectLabels = jest.fn();
    const { labelSelector } = renderComponent({
      initialLabels: labels,
      onSelectLabels,
    });

    // the component is initialized and loads without pre-existing labels
    expect(labelSelector).toBeInTheDocument();
  });
});
