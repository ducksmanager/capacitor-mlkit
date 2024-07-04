package io.capawesome.capacitorjs.plugins.mlkit.objectdetection;

import com.getcapacitor.Logger;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.mlkit.vision.common.InputImage;

import io.capawesome.capacitorjs.plugins.mlkit.objectdetection.classes.ProcessImageOptions;
import io.capawesome.capacitorjs.plugins.mlkit.objectdetection.classes.ProcessImageResult;

@CapacitorPlugin(name = "ObjectDetector")
public class ObjectDetectionPlugin extends Plugin {

    public static final String TAG = "ObjectDetection";

    public static final String ERROR_PROCESS_IMAGE_CANCELED = "processImage canceled.";
    public static final String ERROR_PATH_MISSING = "path must be provided.";
    public static final String ERROR_LOAD_IMAGE_FAILED = "The image could not be loaded.";

    public static final float CONFIDENCE = 0.9f;

    private MyObjectDetection implementation;

    @Override
    public void load() {
        try {
            implementation = new MyObjectDetection(this);
        } catch (Exception exception) {
            Logger.error(TAG, exception.getMessage(), exception);
        }
    }

    @PluginMethod
    public void processImage(PluginCall call) {
        try {
            String path = call.getString("path", null);
            if (path == null) {
                call.reject(ERROR_PATH_MISSING);
                return;
            }

            Integer width = call.getInt("width", null);
            Integer height = call.getInt("height", null);

            Float confidence = call.getFloat("confidence", CONFIDENCE);

            InputImage image = implementation.createInputImageFromFilePath(path);
            if (image == null) {
                call.reject(ERROR_LOAD_IMAGE_FAILED);
                return;
            }
            ProcessImageOptions options = new ProcessImageOptions(image, width, height, confidence);

            implementation.processImage(
                options,
                new ProcessImageResultCallback() {
                    @Override
                    public void success(ProcessImageResult result) {
                        try {
                            call.resolve(result.toJSObject());
                        } catch (Exception exception) {
                            String message = exception.getMessage();
                            Logger.error(TAG, message, exception);
                            call.reject(message);
                        }
                    }

                    @Override
                    public void cancel() {
                        call.reject(ERROR_PROCESS_IMAGE_CANCELED);
                    }

                    @Override
                    public void error(Exception exception) {
                        String message = exception.getMessage();
                        Logger.error(TAG, message, exception);
                        call.reject(message);
                    }
                }
            );
        } catch (Exception exception) {
            String message = exception.getMessage();
            Logger.error(TAG, message, exception);
            call.reject(message);
        }
    }
}