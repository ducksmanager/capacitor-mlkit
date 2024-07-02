package io.capawesome.capacitorjs.plugins.mlkit.objectdetection;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import io.capawesome.capacitorjs.plugins.mlkit.objectdetection.classes.ProcessImageOptions;
import io.capawesome.capacitorjs.plugins.mlkit.objectdetection.classes.ProcessImageResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MyObjectDetection {

    @NonNull
    private final ObjectDetectionPlugin plugin;

    public MyObjectDetection(@NonNull ObjectDetectionPlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public InputImage createInputImageFromFilePath(@NonNull String path) {
        try {
            return InputImage.fromFilePath(this.plugin.getContext(), Uri.parse(path));
        } catch (Exception exception) {
            return null;
        }
    }

    public void processImage(ProcessImageOptions options, ProcessImageResultCallback callback) {
        InputImage inputImage = options.getInputImage();
        Float threshold = options.getConfidence();

        ObjectDetectorOptions.Builder builder = new ObjectDetectorOptions.Builder();
        builder.setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE);
        ObjectDetectorOptions ObjectDetectorOptions = builder.build();

        final ObjectDetector detector = ObjectDetection.getClient(ObjectDetectorOptions);

        plugin
            .getActivity()
            .runOnUiThread(
                () ->
                    detector
                        .process(inputImage)
                        .addOnSuccessListener(
                            (List<DetectedObject> detectedObjects) -> {
                                detector.close();

                                Bitmap bitmap = inputImage.getBitmapInternal();
                                Objects.requireNonNull(bitmap).setHasAlpha(true);

                                Bitmap tempBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                                Canvas canvas = new Canvas(tempBitmap);
                                canvas.setBitmap(bitmap);
                                Paint p = new Paint();
                                p.setStyle(Paint.Style.FILL_AND_STROKE);
                                p.setAntiAlias(true);
                                p.setFilterBitmap(true);
                                p.setDither(true);
                                p.setColor(Color.RED);

                                for (DetectedObject detectedObject : detectedObjects) {
                                    int x1 = detectedObject.getBoundingBox().left;
                                    int x2 = detectedObject.getBoundingBox().right;
                                    int y1 = detectedObject.getBoundingBox().top;
                                    int y2 = detectedObject.getBoundingBox().bottom;
                                    canvas.drawLine(x1, y1, x2, y1, p);//up
                                    canvas.drawLine(x1, y1, x1, y2, p);//left
                                    canvas.drawLine(x1, y2, x2, y2, p);//down
                                    canvas.drawLine(x2, y1, x2, y2, p);
                                }

                                // Create an image file name
                                @SuppressLint("SimpleDateFormat")
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                String imageFileName = "PNG_" + timeStamp + "_";

                                try {
                                    File image = File.createTempFile(imageFileName, ".png");

                                    OutputStream stream = new FileOutputStream(image);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    stream.close();

                                    ProcessImageResult result = new ProcessImageResult(
                                        image.getAbsolutePath(),
                                        bitmap.getWidth(),
                                        bitmap.getHeight()
                                    );

                                    callback.success(result);
                                } catch (Exception exception) {
                                    callback.error(exception);
                                }
                            }
                        )
                        .addOnCanceledListener(
                            () -> {
                                detector.close();
                                callback.cancel();
                            }
                        )
                        .addOnFailureListener(
                            exception -> {
                                detector.close();
                                callback.error(exception);
                            }
                        )
            );
    }
}
