import { ActionGroup, Button, Form } from '@patternfly/react-core';
import * as React from 'react';

export interface IFormWrapperProps {
  isHorizontal?: boolean;
  onSubmit: (event: React.FormEvent<HTMLFormElement>) => void;
  fields: JSX.Element;
}

export class FormWrapper extends React.Component<IFormWrapperProps> {
  public render() {
    return (
      <Form
        isHorizontal={
          typeof this.props.isHorizontal === 'boolean'
            ? this.props.isHorizontal
            : true
        }
        onSubmit={this.props.onSubmit}
      >
        {this.props.fields}
        <ActionGroup>
          <Button type={'submit'} variant={'primary'}>
            Submit
          </Button>
          <Button variant={'secondary'}>Cancel</Button>
        </ActionGroup>
      </Form>
    );
  }
}
