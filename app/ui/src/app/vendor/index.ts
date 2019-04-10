// Vendor imports and aliases
import * as moment from 'moment';

export { moment };

import {
  FileItem,
  FileLikeObject,
  FileUploader,
  FileUploaderOptions,
  ParsedResponseHeaders
} from 'ng2-file-upload';

export { FileItem, FileLikeObject, FileUploader, FileUploaderOptions, ParsedResponseHeaders };

import 'codemirror/addon/display/placeholder.js';
import 'codemirror/addon/mode/overlay.js';
import 'codemirror/addon/lint/lint.js';
import 'codemirror/mode/velocity/velocity.js';
import * as CodeMirror from 'codemirror';
export { CodeMirror };

import * as Mustache from 'mustache';
export { Mustache };

import * as Velocity from 'velocityjs';
export { Velocity };

import { Parser as FreemarkerParser, Tokenizer as FreemarkerTokenizer } from 'freemarker-parser/dist';
export { FreemarkerParser, FreemarkerTokenizer };

export * from '@syndesis/ui/vendor/vendor.module';
