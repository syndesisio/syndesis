import { Step } from '@syndesis/models';
import { IUIIntegrationStep } from './interfaces';
import { toUIIntegrationStepCollection } from './utils';

const json: Step = {
  action: {
    descriptor: {
      inputDataShape: {
        kind: 'json-instance',
        name: 'json',
        specification: '',
      },
      outputDataShape: {
        kind: 'json-instance',
        name: 'json',
        specification: '',
      },
    },
    name: 'json',
  },
  id: '1',
};

const xml: Step = {
  action: {
    descriptor: {
      inputDataShape: {
        kind: 'xml-instance',
        name: 'xml',
        specification: '',
      },
      outputDataShape: {
        kind: 'xml-instance',
        name: 'xml',
        specification: '',
      },
    },
    name: 'xml',
  },
  id: '2',
};

const noOutput: Step = {
  action: {
    descriptor: {
      inputDataShape: {
        kind: 'json-instance',
        name: 'json',
        specification: '',
      },
      outputDataShape: {
        kind: 'none',
        name: 'no-output',
      },
    },
    name: 'json',
  },
  id: '1',
};

const mapping: Step = {
  action: {
    descriptor: {
      outputDataShape: xml.action!.descriptor!.inputDataShape,
    },
    name: 'mapper',
  },
  configuredProperties: {
    atlasmapping: `{
      "AtlasMapping": {
        "dataSource": [
          { "id": "1", "dataSourceType": "SOURCE" },
          { "id": "2", "dataSourceType": "TARGET" }
        ]
      }
    }`,
  },
  stepKind: 'mapper',
};

const base = {
  isUnclosedSplit: false,
  notConfigurable: false,
  previousStepShouldDefineDataShape: false,
  previousStepShouldDefineDataShapePosition: undefined,
  restrictedDelete: false,
  shape: undefined,
  shouldAddDataMapper: false,
  shouldAddDefaultFlow: false,
  shouldEditDataMapper: false,
};

function changeSpecification(step: Step, to: string): Step {
  return {
    ...step,
    action: {
      ...step.action,
      descriptor: {
        ...step.action?.descriptor,
        inputDataShape: {
          ...step.action?.descriptor?.inputDataShape,
          specification: to,
        },
      },
    },
  };
}

describe('mapping warnings', () => {
  test.each`
    name                                                          | steps                                                            | result
    ${'empty flows nothing is shown'}                             | ${[]}                                                            | ${[]}
    ${"don't recommend adding mapping step when i/o matches"}     | ${[json, json]}                                                  | ${[json, json]}
    ${'recommend adding mapping step for i/o mismatch'}           | ${[json, xml]}                                                   | ${[json, { ...xml, shouldAddDataMapper: true }]}
    ${'recommend adding mapping step with multiple i/o mismatch'} | ${[xml, json, xml]}                                              | ${[xml, { ...json, shouldAddDataMapper: true }, { ...xml, shouldAddDataMapper: true }]}
    ${"don't recommend updating mapping step if all up to date"}  | ${[json, { ...mapping, metdata: { updatedAt: 1 } }, xml]}        | ${[json, { ...mapping, metdata: { updatedAt: 1 } }, xml]}
    ${'recommend updating mapping step on input changes'}         | ${[{ ...json, metadata: { outputUpdatedAt: 1 } }, mapping, xml]} | ${[{ ...json, metadata: { outputUpdatedAt: 1 } }, { ...mapping, shouldEditDataMapper: true }, xml]}
    ${'recommend updating mapping step on output changes'}        | ${[json, mapping, changeSpecification(xml, 'xyz')]}              | ${[json, { ...mapping, shouldEditDataMapper: true }, changeSpecification(xml, 'xyz')]}
    ${'recommend adding mapping step for previous none shape'}    | ${[noOutput, json]}                                              | ${[noOutput, { ...json, shouldAddDataMapper: true }]}
    ${'recommend updating mapping with missing target step'}      | ${[json, mapping, { ...xml, id: 'different' }]}                  | ${[json, { ...mapping, shouldEditDataMapper: true }, { ...xml, id: 'different' }]}
    ${'recommend updating mapping with missing source step'}      | ${[{ ...json, id: 'different' }, mapping, xml]}                  | ${[{ ...json, id: 'different' }, { ...mapping, shouldEditDataMapper: true }, xml]}
    ${"don't recommend updating mapping with next any shape"}     | ${[json, mapping, mapping, xml]}                                 | ${[json, mapping, mapping, xml]}
  `('$name', ({ steps, result }) => {
    const basedResult = (result as IUIIntegrationStep[]).map((r) => {
      return { ...base, ...r };
    });
    expect(toUIIntegrationStepCollection(steps)).toEqual(basedResult);
  });
});
