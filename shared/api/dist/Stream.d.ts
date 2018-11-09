import { IRestProps, Rest } from './Rest';
export declare class Stream extends Rest<string[]> {
    protected reader: ReadableStreamReader | undefined;
    componentDidUpdate(prevProps: IRestProps<string[]>): Promise<void>;
    componentWillUnmount(): void;
    read: () => Promise<void>;
    onSave: () => Promise<never>;
}
