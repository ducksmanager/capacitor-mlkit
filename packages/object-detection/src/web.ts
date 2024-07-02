import { CapacitorException, ExceptionCode, WebPlugin } from '@capacitor/core';

import type {
  ObjectDetectionPlugin,
  ProcessImageOptions,
  ProcessImageResult,
} from './definitions';

export class ObjectDetectionWeb
  extends WebPlugin
  implements ObjectDetectionPlugin
{
  public async processImage(
    _options: ProcessImageOptions,
  ): Promise<ProcessImageResult> {
    throw this.createUnavailableException();
  }

  private createUnavailableException(): CapacitorException {
    return new CapacitorException(
      'This Object Detection plugin method is not available on this platform.',
      ExceptionCode.Unavailable,
    );
  }
}
