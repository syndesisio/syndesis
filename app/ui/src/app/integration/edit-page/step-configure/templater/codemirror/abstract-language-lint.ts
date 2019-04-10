import {
  Subject,
  Observable
} from 'rxjs';
import {
  I18NService
} from '@syndesis/ui/platform';
import {
  CodeMirror
} from '@syndesis/ui/vendor';
import { TemplateSymbol } from './template-symbol';

export abstract class AbstractLanguageLint {

  public validationChanged$: Observable<any[]>;

  protected validationChangeSource = new Subject<any[]>();

  private _errors: any[] = [];

  constructor(private _name: string, protected i18NService: I18NService) {
    this.validationChanged$ = this.validationChangeSource.asObservable();
    this.define();
  }

  public abstract parse(content: string): TemplateSymbol[];

  public name(): string {
    return this._name;
  }

  protected define(): void {
    CodeMirror.registerHelper('lint', this.name(), (text, options) => this.validator(text, options));
  }

  protected abstract validate(text: string, errors: any[]): void;

  protected validator(text: string, options: any): any[] {
    this._errors = [];

    if (text.length === 0) {
      const msg = this.i18NService.localize('integrations.steps.templater-no-content');
      this._errors.push({
        message: msg,
        severity: 'warning',
        from: CodeMirror.Pos(0, 0),
        to: CodeMirror.Pos(0, 0)
      });
      return this._errors;
    }

    this.validate(text, this._errors);

    this.validationChangeSource.next(this._errors);
    return this._errors;
  }
}
