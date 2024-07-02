package io.capawesome.capacitorjs.plugins.mlkit.objectdetection;

import io.capawesome.capacitorjs.plugins.mlkit.objectdetection.classes.ProcessImageResult;

public interface ProcessImageResultCallback {
    void success(ProcessImageResult result);

    void cancel();

    void error(Exception exception);
}
