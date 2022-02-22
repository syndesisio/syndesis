import React from 'react';
import { render } from 'enzyme';
import { WithChoiceConfigurationForm } from './WithChoiceConfigurationForm';

export default describe('WithChoiceConfigurationForm', () => {
  test.each([
    { expression: 'abc > 123', outcome: 'invalid' },
    { expression: '${abc > 123', outcome: 'invalid' },
    { expression: '${abc} > 123', outcome: 'valid' },
    { expression: '    \t${abc} > 123', outcome: 'valid' },
  ])('$expression should be $invalid', ({ expression, outcome }) => {
    const form = render(
      <WithChoiceConfigurationForm
        configMode="advanced"
        filterOptions={{}}
        initialValue={{
          defaultFlowId: 'id',
          flowConditions: [{ flowId: 'flowId', condition: expression }],
          routingScheme: 'direct',
          useDefaultFlow: false,
        }}
        stepId="stepId"
        onUpdatedIntegration={(_) => new Promise<void>(() => {})}
      >
        {({ isValid }) => <span>{isValid ? 'valid' : 'invalid'}</span>}
      </WithChoiceConfigurationForm>
    );

    expect(form.text()).toEqual(outcome);
  });
});
