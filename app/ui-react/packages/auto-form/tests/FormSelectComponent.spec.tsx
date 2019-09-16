import * as React from 'react';
import { render } from 'react-testing-library';
import { AutoForm, toValidHtmlId } from '../src';

describe('FormSelectComponent: single', () => {
  const fieldId = 'test01Select';
  const definition = {
    [fieldId]: {
      defaultValue: 'INFO',
      description: 'Log Level.',
      displayName: 'log level',
      kind: 'parameter',
      required: true,
      secret: false,
      type: 'string',
      enum: [
        {
          label: 'ERROR',
          value: 'ERROR',
        },
        {
          label: 'WARN',
          value: 'WARN',
        },
        {
          label: 'INFO',
          value: 'INFO',
        },
        {
          label: 'DEBUG',
          value: 'DEBUG',
        },
        {
          label: 'TRACE',
          value: 'TRACE',
        },
        {
          label: 'OFF',
          value: 'OFF',
        },
      ],
    },
  };
  const form = (
    <AutoForm
      definition={definition}
      initialValue={{}}
      i18nRequiredProperty={'required'}
      onSave={() => false}
    >
      {({ fields }) => fields}
    </AutoForm>
  );

  it('Should use the definition key as an id', () => {
    const { getByTestId } = render(form);
    expect(getByTestId(toValidHtmlId(fieldId))).toBeDefined();
  });

  it('Should use the displayName as a label', () => {
    const { getByLabelText } = render(form);
    const displayName = definition[fieldId].displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should render set to the default value', () => {
    const { getByTestId } = render(form);
    expect(
      (getByTestId(toValidHtmlId(fieldId)) as HTMLSelectElement).value
    ).toEqual('INFO');
  });
});

describe('FormSelectComponent: multiple - string array input', () => {
  const fieldId = 'test01Select';
  const definition = {
    [fieldId]: {
      defaultValue: '["INFO", "TRACE"]',
      description: 'Log Level.',
      displayName: 'log level',
      kind: 'parameter',
      required: true,
      secret: false,
      type: 'string',
      enum: [
        {
          label: 'ERROR',
          value: 'ERROR',
        },
        {
          label: 'WARN',
          value: 'WARN',
        },
        {
          label: 'INFO',
          value: 'INFO',
        },
        {
          label: 'DEBUG',
          value: 'DEBUG',
        },
        {
          label: 'TRACE',
          value: 'TRACE',
        },
        {
          label: 'OFF',
          value: 'OFF',
        },
      ],
      fieldAttributes: {
        multiple: true,
      },
    },
  };
  const form = (
    <AutoForm
      definition={definition}
      initialValue={{}}
      i18nRequiredProperty={'required'}
      onSave={() => false}
    >
      {({ fields }) => fields}
    </AutoForm>
  );

  it('Should use the definition key as an id', () => {
    const { getByTestId } = render(form);
    expect(getByTestId(toValidHtmlId(fieldId))).toBeDefined();
  });

  it('Should use the displayName as a label', () => {
    const { getByLabelText } = render(form);
    const displayName = definition[fieldId].displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should render set to the default value', () => {
    const { getByTestId } = render(form);
    expect(
      Array.prototype.filter
        .call(
          (getByTestId(toValidHtmlId(fieldId)) as HTMLSelectElement).options,
          o => o.selected
        )
        .map(o => o.value)
    ).toEqual(['INFO', 'TRACE']);
  });
});

describe('FormSelectComponent: multiple - string input', () => {
  const fieldId = 'test01Select';
  const definition = {
    [fieldId]: {
      defaultValue: 'INFO',
      description: 'Log Level.',
      displayName: 'log level',
      kind: 'parameter',
      required: true,
      secret: false,
      type: 'string',
      enum: [
        {
          label: 'ERROR',
          value: 'ERROR',
        },
        {
          label: 'WARN',
          value: 'WARN',
        },
        {
          label: 'INFO',
          value: 'INFO',
        },
        {
          label: 'DEBUG',
          value: 'DEBUG',
        },
        {
          label: 'TRACE',
          value: 'TRACE',
        },
        {
          label: 'OFF',
          value: 'OFF',
        },
      ],
      fieldAttributes: {
        multiple: true,
      },
    },
  };
  const form = (
    <AutoForm
      definition={definition}
      initialValue={{}}
      i18nRequiredProperty={'required'}
      onSave={() => false}
    >
      {({ fields }) => fields}
    </AutoForm>
  );

  it('Should use the definition key as an id', () => {
    const { getByTestId } = render(form);
    expect(getByTestId(toValidHtmlId(fieldId))).toBeDefined();
  });

  it('Should use the displayName as a label', () => {
    const { getByLabelText } = render(form);
    const displayName = definition[fieldId].displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should render set to the default value', () => {
    const { getByTestId } = render(form);
    expect(
      Array.prototype.filter
        .call(
          (getByTestId(toValidHtmlId(fieldId)) as HTMLSelectElement).options,
          o => o.selected
        )
        .map(o => o.value)
    ).toEqual(['INFO']);
  });
});

describe('FormSelectComponent: multiple - empty string input', () => {
  const fieldId = 'test01Select';
  const definition = {
    [fieldId]: {
      defaultValue: '',
      description: 'Log Level.',
      displayName: 'log level',
      kind: 'parameter',
      required: false,
      secret: false,
      type: 'string',
      enum: [
        {
          label: 'ERROR',
          value: 'ERROR',
        },
        {
          label: 'WARN',
          value: 'WARN',
        },
        {
          label: 'INFO',
          value: 'INFO',
        },
        {
          label: 'DEBUG',
          value: 'DEBUG',
        },
        {
          label: 'TRACE',
          value: 'TRACE',
        },
        {
          label: 'OFF',
          value: 'OFF',
        },
      ],
      fieldAttributes: {
        multiple: true,
      },
    },
  };
  const form = (
    <AutoForm
      definition={definition}
      initialValue={{}}
      i18nRequiredProperty={'required'}
      onSave={() => false}
    >
      {({ fields }) => fields}
    </AutoForm>
  );

  it('Should use the definition key as an id', () => {
    const { getByTestId } = render(form);
    expect(getByTestId(toValidHtmlId(fieldId))).toBeDefined();
  });

  it('Should use the displayName as a label', () => {
    const { getByLabelText } = render(form);
    const displayName = definition[fieldId].displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should render set to empty array', () => {
    const { getByTestId } = render(form);
    expect(
      Array.prototype.filter
        .call(
          (getByTestId(toValidHtmlId(fieldId)) as HTMLSelectElement).options,
          o => o.selected
        )
        .map(o => o.value)
    ).toEqual([]);
  });
});

describe('FormSelectComponent: multiple - array input', () => {
  const fieldId = 'test01Select';
  const definition = {
    [fieldId]: {
      defaultValue: ["INFO", "TRACE"] as any, // TODO interface is typed as string
      description: 'Log Level.',
      displayName: 'log level',
      kind: 'parameter',
      required: true,
      secret: false,
      type: 'string',
      enum: [
        {
          label: 'ERROR',
          value: 'ERROR',
        },
        {
          label: 'WARN',
          value: 'WARN',
        },
        {
          label: 'INFO',
          value: 'INFO',
        },
        {
          label: 'DEBUG',
          value: 'DEBUG',
        },
        {
          label: 'TRACE',
          value: 'TRACE',
        },
        {
          label: 'OFF',
          value: 'OFF',
        },
      ],
      fieldAttributes: {
        multiple: true,
      },
    },
  };
  const form = (
    <AutoForm
      definition={definition}
      initialValue={{}}
      i18nRequiredProperty={'required'}
      onSave={() => false}
    >
      {({ fields }) => fields}
    </AutoForm>
  );

  it('Should use the definition key as an id', () => {
    const { getByTestId } = render(form);
    expect(getByTestId(toValidHtmlId(fieldId))).toBeDefined();
  });

  it('Should use the displayName as a label', () => {
    const { getByLabelText } = render(form);
    const displayName = definition[fieldId].displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should render set to the default value', () => {
    const { getByTestId } = render(form);
    expect(
      Array.prototype.filter
        .call(
          (getByTestId(toValidHtmlId(fieldId)) as HTMLSelectElement).options,
          o => o.selected
        )
        .map(o => o.value)
    ).toEqual(['INFO', 'TRACE']);
  });
});
