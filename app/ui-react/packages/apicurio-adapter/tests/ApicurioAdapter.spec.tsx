import * as React from 'react';
import { render } from 'react-testing-library';
import { ApicurioAdapter } from '../src';

(window as any).MessageChannel = jest.fn().mockImplementation(() => {
  return {
    port1: {
      onmessage: jest.fn(),
      postMessage: jest.fn(),
    },
  };
});

export default describe('DataMapperAdapter', () => {
  const onSpecification = jest.fn();
  const testComponent = (
    <ApicurioAdapter specification={''} onSpecification={onSpecification} />
  );

  it('Should render', () => {
    const { container } = render(testComponent);
    expect((window as any).MessageChannel).toBeCalledTimes(1);
    expect(container).toBeDefined();
  });
});
