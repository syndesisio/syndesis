import * as React from 'react';
import { render } from 'react-testing-library';
import { ExtensionSupports, IAction } from '../../src/Customization';

export default describe('ExtensionSupports', () => {
  const actions = [
    {
      name: 'Action 1',
      description: 'The description for action 1',
    } as IAction,
    {
      name: 'Action 2',
      description: 'The description for action 2',
    } as IAction,
    {
      name: 'Action 3',
      description: 'The description for action 3',
    } as IAction,
    {
      name: 'Action 4',
      description: 'The description for action 4',
    } as IAction,
  ] as IAction[];

  it('Should render correctly with actions', () => {
    const componentWithActions = (
      <ExtensionSupports extensionActions={actions} />
    );
    const { queryByText } = render(componentWithActions);
    actions.map(a => {
      expect(queryByText(a.name)).toBeDefined();
      expect(queryByText(a.description)).toBeDefined();
    });
  });

  it('Should render correctly without actions', () => {
    const comp = <ExtensionSupports extensionActions={[]} />;
    const { baseElement } = render(comp);
    expect(baseElement).toMatchInlineSnapshot(`
<body>
  <div>
    <div
      class="extension-group"
    />
  </div>
</body>
`);
  });
});
