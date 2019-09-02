import { ActionGroup, Button, Form } from '@patternfly/react-core';
import * as React from 'react';

export interface IFormWrapperProps {
  isHorizontal?: boolean;
  className?: string;
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
        className={this.props.className || ''}
        onSubmit={this.props.onSubmit}
      >
        {this.props.fields}
        <ActionGroup className="form-array-action">
          <Button type={'submit'} variant={'primary'}>
            Submit
          </Button>
          <Button variant={'secondary'}>Cancel</Button>
        </ActionGroup>
      </Form>
    );
  }
}
