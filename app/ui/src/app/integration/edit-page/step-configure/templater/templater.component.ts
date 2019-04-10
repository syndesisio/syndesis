import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewEncapsulation,
  ViewChild,
  OnInit,
  AfterViewInit,
  OnDestroy,
  HostListener,
} from '@angular/core';
import { Subscription } from 'rxjs';
import {
  I18NService,
  DataShape,
  DataShapeKinds,
  ActionDescriptor,
  Action,
  IntegrationSupportService,
} from '@syndesis/ui/platform';
import {
  CurrentFlowService,
  INTEGRATION_SET_ACTION,
} from '@syndesis/ui/integration/edit-page';
import { FileLikeObject, FileUploader } from '@syndesis/ui/vendor';
import {
  TemplateSymbol,
  MustacheModeLint,
  VelocityLint,
  FreemarkerModeLint,
} from './codemirror';

@Component({
  selector: 'syndesis-templater',
  templateUrl: './templater.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./templater.component.scss'],
  providers: [MustacheModeLint, VelocityLint, FreemarkerModeLint],
})
export class TemplaterComponent implements OnInit, AfterViewInit, OnDestroy {
  @Input() configuredProperties: any;
  @Input() valid: boolean;
  @Input() dataShape: DataShape;
  @Input() position;

  @Output() configuredPropertiesChange = new EventEmitter<String>();
  @Output() validChange = new EventEmitter<boolean>();

  /*
   * The template content string
   */
  templateContent: string;
  templateLanguage = 'mustache';

  invalidFileMsg: string;
  uploader: FileUploader;
  editorFocused: boolean;
  validationErrors: any[] = [];
  dragEnter: boolean;

  /*
   * Variables for use with the create template editor
   */
  editorConfig = {
    mode: 'mustache',
    lineNumbers: false,
    lineWrapping: true,
    readOnly: false,
    styleActiveLine: true,
    tabSize: 2,
    showCursorWhenSelecting: true,
    gutters: ['CodeMirror-lint-markers'],
    lint: true,
  };

  //
  // The instance of the CodeMirror editor when initialised
  //
  @ViewChild('templateEditor') private templateEditor: any;

  private outShapeSpec: any;

  private validationSubscription: Subscription;

  private parseFunction: (text: string) => TemplateSymbol[];

  constructor(
    private i18NService: I18NService,
    public currentFlowService: CurrentFlowService,
    public integrationSupportService: IntegrationSupportService,
    private mustacheModeLint: MustacheModeLint,
    private velocityLint: VelocityLint,
    private freemarkerModeLint: FreemarkerModeLint
  ) {}

  @HostListener('document:dragenter', ['$event'])
  onDocumentDragEnter(event: Event) {
    //
    // Turn off the drag overlay
    //
    this.dragEnter = false;
  }

  ngOnInit() {
    //
    // Initialise the out data shape specification
    //
    this.outShapeSpec = this.createSpecification([
      new TemplateSymbol('message', 'string'),
    ]);

    //
    // Initialise the values
    //
    if (this.configuredProperties) {
      if (this.configuredProperties.template) {
        this.templateContent = this.configuredProperties.template;
      }
      if (this.configuredProperties.language) {
        this.templateLanguage = this.configuredProperties.language;
      }
    }

    this.initUploader();
  }

  ngAfterViewInit() {
    if (this.templateEditor && this.templateEditor.instance) {
      const instance = this.templateEditor.instance;

      //
      // Enable drag callback on coremirror instance. This
      // seems to work more effectively than adding it
      // through the ng2-codemirror directive
      //
      instance.on('dragenter', (cm, event) => {
        this.onEditorDragenter(event);
      });

      //
      // Enable drop callback on coremirror instance. This
      // seems to work more effectively than adding it
      // through the ng2-codemirror directive
      //
      instance.on('drop', (cm, event) => {
        this.onEditorDrop(event);
      });
    }

    // Set the editor language and validator
    this.changeEditorLanguage();
  }

  ngOnDestroy() {
    this.unsubscribeValidator();
  }

  onEditorFocus() {
    this.editorFocused = true;
    this.invalidFileMsg = null;
  }

  onEditorBlur() {
    this.editorFocused = false;
  }

  onLanguageChange() {
    this.changeEditorLanguage();
  }

  onChange() {
    let symbols: TemplateSymbol[] = [];

    if (this.templateContent) {
      try {
        //
        // The data mapper that precedes this template
        // requires the names of the template symbols in
        // the in-shape-specification. To do this, parse
        // the template content, extract the symbols then
        // apply them to the specification.
        //
        symbols = this.extractTemplateSymbols();
        if (symbols.length === 0) {
          this.validationErrors.push({ message: 'No symbols present' });
        }
      } catch (exception) {
        this.validationErrors.push({ message: exception.message });
      }

      this.valid = this.validationErrors.length === 0;
    } else {
      this.valid = false;
    }

    this.validChange.emit(this.valid);
    if (!this.valid) {
      return;
    }

    const inShapeSpec = this.createSpecification(symbols);
    //
    // Creates the action in the step
    // and only does this once since the id
    // will always match
    //
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_ACTION,
      position: this.position,
      stepKind: 'template',
      action: {
        actionType: 'step',
        name: 'Templater',
        descriptor: {
          inputDataShape: {
            kind: DataShapeKinds.JSON_SCHEMA,
            name: 'Template JSON Schema',
            specification: inShapeSpec,
          } as DataShape,
          outputDataShape: {
            kind: DataShapeKinds.JSON_SCHEMA,
            name: 'Template JSON Schema',
            specification: this.outShapeSpec,
          },
        } as ActionDescriptor,
      } as Action,
    });

    const formattedProperties: any = {
      template: this.templateContent,
      language: this.templateLanguage,
    };

    this.configuredPropertiesChange.emit(formattedProperties);
  }

  private initUploader() {
    this.uploader = new FileUploader({
      maxFileSize: 1024,
    });

    this.uploader.onAfterAddingFile = () => {
      // successfully added file so clear out failed message
      this.invalidFileMsg = null;

      // since more than one file may have been dropped, clear out all but last one
      if (this.uploader.queue.length > 1) {
        this.uploader.queue.splice(0, 1);
      }

      // pop off file from queue to set file and clear queue
      const fileToUpload = this.uploader.queue.pop()._file;

      const reader = new FileReader();
      reader.onload = () => {
        this.templateContent = reader.result as string;
      };

      reader.readAsText(fileToUpload);
    };

    this.uploader.onWhenAddingFileFailed = (file: FileLikeObject): any => {
      // occurs when not a *.json file
      this.invalidFileMsg = this.i18NService.localize(
        'integrations.steps.templater-upload-invalid-file',
        [file.name]
      );
      this.uploader.clearQueue();
    };
  }

  private onEditorDragenter(event) {
    //
    // Turn on the drag overlay
    //
    this.dragEnter = true;
    //
    // Stop the document handler from firing
    //
    event.preventDefault();
    event.stopPropagation();
  }

  private onEditorDrop(event) {
    //
    // Turn off the drag overlay
    //
    this.dragEnter = false;
  }

  private unsubscribeValidator() {
    if (this.validationSubscription) {
      this.validationSubscription.unsubscribe();
    }
  }

  private validationCallback(errors: any[]) {
    this.validationErrors = errors.slice(0);
    this.onChange();
  }

  /**
   * Updates the mode and linting language of the editor
   */
  private changeEditorLanguage() {
    if (!this.templateEditor) {
      return;
    }

    // Clear any validation errors
    this.validationErrors = [];

    // Unsubscribe the current validator
    this.unsubscribeValidator();

    // Get the instance of the codemirror editor
    const instance = this.templateEditor.instance;

    // Set the mode of the editor in accordance with the selected lanaguage
    instance.setOption('mode', this.templateLanguage);

    //
    // Based on the choice of language subscribe to the
    // relevant validator and parser
    //
    switch (this.templateLanguage) {
      case 'mustache': {
        this.validationSubscription = this.mustacheModeLint.validationChanged$.subscribe(
          errors => this.validationCallback(errors)
        );

        this.parseFunction = this.mustacheModeLint.parse;
        break;
      }
      case 'velocity': {
        this.validationSubscription = this.velocityLint.validationChanged$.subscribe(
          errors => this.validationCallback(errors)
        );

        this.parseFunction = this.velocityLint.parse;
        break;
      }
      case 'freemarker': {
        this.validationSubscription = this.freemarkerModeLint.validationChanged$.subscribe(
          errors => this.validationCallback(errors)
        );

        this.parseFunction = this.freemarkerModeLint.parse;
        break;
      }
      default: {
        // Cannot really be anything but these 3 choices
        // so nothing to do.
      }
    }

    // Perform a new linting of the text based on the updated language
    instance.performLint();
  }

  private extractTemplateSymbols(): TemplateSymbol[] {
    let symbols: TemplateSymbol[] = [];

    if (!this.templateContent) {
      return symbols;
    }

    symbols = this.parseFunction(this.templateContent);
    return symbols;
  }

  private createSpecification(symbols: TemplateSymbol[]): string {
    const spec: any = {
      type: 'object',
      $schema: 'http://json-schema.org/schema#',
      title: 'Template JSON Schema',
    };

    if (symbols.length === 0) {
      return spec;
    }

    const properties: any = {};
    for (const symbol of symbols) {
      properties[symbol.getId()] = {
        description: 'Identifier for the symbol ' + symbol.getId(),
        type: symbol.getType(),
      };
    }
    spec.properties = properties;

    return JSON.stringify(spec);
  }
}
