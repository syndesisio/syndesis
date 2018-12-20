import deepmerge from 'deepmerge';
import * as React from 'react';
import equal from 'react-fast-compare';
import { callFetch, IFetchHeaders } from './callFetch';

export interface ISaveProps {
  url: string;
  data: any;
}

export interface IRestState<T> {
  data: T;
  error: boolean;
  errorMessage?: string;
  hasData: boolean;
  loading: boolean;
}

export interface IRestRenderProps<T> {
  response: IRestState<T>;
  read(): Promise<void>;
  save(props: ISaveProps): void;
}

export interface IRestProps<T> {
  autoload?: boolean;
  baseUrl: string;
  url: string;
  headers?: IFetchHeaders;
  contentType?: string;
  defaultValue: T;
  children(props: IRestRenderProps<T>): any;
}

export class Rest<T> extends React.Component<IRestProps<T>, IRestState<T>> {
  public static defaultProps = { autoload: true };

  public constructor(props: IRestProps<T>) {
    super(props);
    this.read = this.read.bind(this);
    this.onSave = this.onSave.bind(this);
    this.state = {
      data: this.props.defaultValue,
      error: false,
      hasData: false,
      loading: true,
    };
  }

  public async componentDidMount() {
    if (this.props.autoload) {
      this.read();
    }
  }

  public async componentDidUpdate(prevProps: IRestProps<T>) {
    if (prevProps.url !== this.props.url) {
      this.read();
    }
  }

  public shouldComponentUpdate(
    nextProps: IRestProps<T>,
    nextState: IRestState<T>
  ): boolean {
    return !equal(this.props, nextProps) || !equal(this.state, nextState);
  }

  public render() {
    return this.props.children({
      read: this.read,
      response: this.state,
      save: this.onSave,
    });
  }

  public async read() {
    try {
      this.setState({ loading: true });
      const response = await callFetch({
        contentType: this.props.contentType,
        headers: this.props.headers,
        method: 'GET',
        url: `${this.props.baseUrl}${this.props.url}`,
      });
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

  public async onSave({ url, data }: ISaveProps) {
    this.setState({ loading: true });
    try {
      const response = await callFetch({
        body: data,
        contentType: this.props.contentType,
        headers: this.props.headers,
        method: 'PUT',
        url: `${this.props.baseUrl}${url || this.props.url}`,
      });
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      setTimeout(() => this.read(), 5000); // TODO: figure out why this is needed
    } catch (e) {
      this.setState({
        error: true,
        errorMessage: e.message,
        loading: false,
      });
    }
  }
}
