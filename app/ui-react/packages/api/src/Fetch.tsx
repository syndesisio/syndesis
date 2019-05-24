import deepmerge from 'deepmerge';
import * as React from 'react';
import equal from 'react-fast-compare';
import { callFetch, IFetch } from './callFetch';

export interface IFetchState<T> {
  data: T;
  error: boolean;
  errorMessage?: string;
  hasData: boolean;
  loading: boolean;
}

export interface IFetchRenderProps<T> {
  response: IFetchState<T>;
  read(): Promise<void>;
}

export interface IFetchProps<T> extends IFetch {
  baseUrl: string;
  url: string;
  defaultValue: T;
  initialValue?: T;
  children(props: IFetchRenderProps<T>): any;
}

export class Fetch<T> extends React.Component<IFetchProps<T>, IFetchState<T>> {
  public constructor(props: IFetchProps<T>) {
    super(props);
    this.read = this.read.bind(this);
    this.state = {
      data: this.props.initialValue || this.props.defaultValue,
      error: false,
      hasData: !!this.props.initialValue,
      loading: true,
    };
  }

  public async componentDidMount() {
    this.read();
  }

  public async componentDidUpdate(prevProps: IFetchProps<T>) {
    if (prevProps.url !== this.props.url) {
      this.read();
    }
  }

  public shouldComponentUpdate(
    nextProps: IFetchProps<T>,
    nextState: IFetchState<T>
  ): boolean {
    return !equal(this.props, nextProps) || !equal(this.state, nextState);
  }

  public render() {
    return this.props.children({
      read: this.read,
      response: this.state,
    });
  }

  public async read() {
    try {
      this.setState({ error: false, errorMessage: undefined, loading: true });
      const fetchProps = {
        ...this.props,
        url: `${this.props.baseUrl}${this.props.url}`,
      };
      const response = await callFetch(fetchProps);
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      let data;
      if (
        !this.props.contentType ||
        this.props.contentType.indexOf('application/json') === 0
      ) {
        data = await response.json();
        if (this.props.defaultValue) {
          data = deepmerge(this.props.defaultValue, data);
        }
      } else {
        data = await response.text();
      }
      this.setState({ data, hasData: true, loading: false });
    } catch (e) {
      this.setState({
        data: this.props.defaultValue,
        error: true,
        errorMessage: e.message,
        hasData: false,
        loading: false,
      });
    }
  }
}
