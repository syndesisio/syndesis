import {
  Connection,
  DataShape,
  Flow,
  Integration,
  Step,
} from '@syndesis/models';
import produce, { isDraft } from 'immer';
import { DataShapeKinds } from '../../constants';
import {
  _parametersDiffer,
  _shapesDiffer,
  applyUpdatedStep,
  getFlow,
  getSteps,
  hasDataShape,
  isIntegrationApiProvider,
  isPrimaryFlow,
  removeStepFromFlow,
} from '../integrationFunctions';

describe('integration functions', () => {
  test.each`
    kind                   | place       | expected
    ${undefined}           | ${'input'}  | ${'not present'}
    ${undefined}           | ${'output'} | ${'not present'}
    ${DataShapeKinds.NONE} | ${'input'}  | ${'present'}
    ${DataShapeKinds.NONE} | ${'output'} | ${'not present'}
    ${DataShapeKinds.ANY}  | ${'input'}  | ${'present'}
    ${DataShapeKinds.ANY}  | ${'output'} | ${'present'}
  `(
    '$kind $place data shape should be asserted as $expected',
    ({ kind, place, expected }) => {
      const step = {
        action: {
          descriptor: {},
        },
      } as Step;

      const dataShape = {
        kind: kind as DataShapeKinds,
      } as DataShape;

      const isInput = place === 'input';
      if (isInput) {
        step.action!.descriptor!.inputDataShape = dataShape;
      } else {
        step.action!.descriptor!.outputDataShape = dataShape;
      }

      expect(hasDataShape(step, isInput)).toBe(expected === 'present');
    }
  );

  it(`steps without actions don't have shapes`, () => {
    const step = {};
    expect(hasDataShape(step, true)).toBe(false);
  });

  it(`steps without action descriptors don't have shapes`, () => {
    const step = {
      action: {},
    } as Step;

    expect(hasDataShape(step, true)).toBe(false);
  });

  it(`should return a specified flow from an integration`, () => {
    const connections: Connection[] = [];

    const flows: Flow[] = [
      {
        connections,
        id: '123456',
        name: 'hello!',
      },
      {
        connections,
        id: '123457',
        name: 'goodbye!',
      },
    ];

    const integration: Integration = {
      flows,
      name: 'Tiny Integration',
    };

    expect(getFlow(integration, '123457')).toBe(flows[1]);
  });

  it(`should determine if the provided flow is primary`, () => {
    const primFlow: Flow = {
      name: 'Some super duper primary flow..',
      type: 'PRIMARY',
    };
    const nonPrimFlow: Flow = {
      name: 'An alternate flow',
      type: 'ALTERNATE',
    };

    expect(isPrimaryFlow(primFlow)).toBeTruthy();
    expect(isPrimaryFlow(nonPrimFlow)).toBeFalsy();

    nonPrimFlow.type = 'API_PROVIDER';

    expect(nonPrimFlow.type).toBe('API_PROVIDER');
    /**
     * isPrimaryFlow checks for BOTH Primary + API Provider types
     */
    expect(isPrimaryFlow(nonPrimFlow)).toBeTruthy();
  });

  it(`should determine if the provided integration is an API provider integration`, () => {
    /**
     * The way this is determined is by including `API_PROVIDER`
     * as a tag within the integration
     */
    const integration: Integration = {
      flows: [],
      name: 'Tiny Integration',
      tags: ['api-provider'],
    };

    expect(isIntegrationApiProvider(integration)).toBeTruthy();
  });

  it(`getSteps should return steps from given integration object`, () => {
    const steps: Step[] = [
      {
        connection: { name: 'peach' },
        id: '1234567',
      },
      {
        connection: { name: 'banana' },
        id: '1234567',
      },
      {
        connection: { name: 'apple' },
        id: '1234567',
      },
    ];

    const flows: Flow[] = [
      {
        id: '-MW_06XdNSe0O_IutbEY',
        name: 'My Fun Flow',
        steps,
      },
    ];

    const customIntegration: Integration = {
      flows,
      name: 'Tiny Integration',
    };

    const someSteps = getSteps(customIntegration, flows[0].id!);
    // Expect original length to be the same
    expect(someSteps).toHaveLength(3);
    expect(customIntegration.flows![0].steps).toHaveLength(3);
  });

  it(`removeStepFromFlow should remove a step from a defined flow`, async () => {
    const customSteps: Step[] = [
      {
        connection: { name: 'peach' },
        id: '1234567',
      },
      {
        connection: { name: 'banana' },
        id: '1234567',
      },
      {
        connection: { name: 'mango' },
        id: '1234567',
      },
    ];

    const customFlowId = '-MW_06XdNSe0O_IutbEY';

    const customFlows: Flow[] = [
      {
        id: customFlowId,
        name: 'My Fun Flow',
        steps: customSteps,
      },
    ];

    const customIntegration: Integration = {
      flows: customFlows,
      name: 'Tiny Integration',
    };

    const fetchStepDescriptors = jest.fn().mockImplementation(() => {
      return customIntegration.flows![0].steps;
    });

    const newInt = await produce(customIntegration, () => {
      return removeStepFromFlow(
        customIntegration,
        customFlowId,
        1,
        fetchStepDescriptors
      );
    });

    expect(customIntegration.flows![0].steps).toHaveLength(2);
    expect(newInt.flows![0].steps).toHaveLength(2);
    expect(isDraft(newInt)).toBeFalsy();
  });

  test.each`
    current           | updated           | expected
    ${undefined}      | ${undefined}      | ${false}
    ${undefined}      | ${{ a: 1 }}       | ${true}
    ${{ a: 1 }}       | ${undefined}      | ${true}
    ${{ a: 1 }}       | ${{ a: 1 }}       | ${false}
    ${{ a: 1 }}       | ${{ a: 2 }}       | ${true}
    ${{ a: 1 }}       | ${{ b: 1 }}       | ${true}
    ${{ a: 1, b: 2 }} | ${{ b: 2, a: 1 }} | ${false}
  `(
    '$current vs $updated data shape parameter difference should be asserted as $expected',
    ({ current, updated, expected }) => {
      expect(_parametersDiffer(current, updated)).toBe(expected);
    }
  );

  test.each`
    current                                          | updated                                          | expected
    ${undefined}                                     | ${undefined}                                     | ${false}
    ${undefined}                                     | ${{ kind: 'json-instance' }}                     | ${true}
    ${{ kind: 'json-instance' }}                     | ${undefined}                                     | ${true}
    ${{ kind: 'json-instance' }}                     | ${{ kind: 'json-instance' }}                     | ${false}
    ${{ kind: 'json-instance' }}                     | ${{ kind: 'json-schema' }}                       | ${true}
    ${{ kind: 'json-instance', specification: 'x' }} | ${{ kind: 'json-schema' }}                       | ${true}
    ${{ kind: 'json-instance', specification: 'x' }} | ${{ kind: 'json-instance', specification: 'y' }} | ${true}
    ${{ kind: 'json-instance' }}                     | ${{ kind: 'json-instance', specification: 'y' }} | ${true}
    ${{ kind: 'json-instance', specification: 'x' }} | ${{ kind: 'json-instance', specification: 'x' }} | ${false}
  `(
    '$current vs $updated data shape difference should be asserted as $expected',
    ({ current, updated, expected }) => {
      expect(_shapesDiffer(current, updated)).toBe(expected);
    }
  );

  it('should set outputUpdatedAt if the output datashape changed', () => {
    const flow: Flow = {
      name: 'flow1',
      steps: [
        {
          action: {
            descriptor: {
              outputDataShape: {
                kind: 'json-schema',
                name: 'output',
                specification: 'a',
              },
            },
            name: 'action',
          },
          metadata: {
            outputUpdatedAt: '123',
          },
        },
      ],
    };
    const step: Step = {
      action: {
        descriptor: {
          outputDataShape: {
            kind: 'json-schema',
            name: 'output',
            specification: 'b',
          },
        },
        name: 'action',
      },
    };
    const updated = applyUpdatedStep(flow, step, 0);
    expect(
      updated.steps![0].metadata!.outputUpdatedAt >
        flow.steps![0].metadata!.outputUpdatedAt
    ).toBeTruthy();
  });
});
