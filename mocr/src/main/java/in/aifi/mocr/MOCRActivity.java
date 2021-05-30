package in.aifi.mocr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.common.internal.ImageConvertUtils;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.concurrent.ExecutionException;

import in.aifi.mocr.views.OverlayView;

public class MOCRActivity extends AppCompatActivity {
    private final Handler mHandler = new Handler();
    private PreviewView mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private ImageAnalysis imageAnalysis;
    private OverlayView overlayView;
    private FragmentContainerView sheetView;
    private Point position;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setup logging
        Logger.addLogAdapter(new AndroidLogAdapter());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        mContentView = findViewById(R.id.fs_cam_preview);
        overlayView = findViewById(R.id.fs_cam_overlay);
        sheetView = findViewById(R.id.sheet_fragment);
        mContentView.setOnTouchListener((v, event) -> {
            position.x = (int) event.getX();
            position.y = (int) event.getY();
            Logger.i("Clicked at: " + position.toString());
            return true;
        });
        mContentView.setScaleType(PreviewView.ScaleType.FILL_CENTER);

        if (!hasPermissions()) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 10);
        }

        imageAnalysis = new ImageAnalysis.Builder()
//                .setTargetResolution(new Size(480, 640))
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setImageQueueDepth(16)
                .build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),
                new MLKitAnalyzer());

        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider processCameraProvider = cameraProviderListenableFuture.get();
                bindPreview(processCameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Logger.e(e, "ERROR: ");
            }
        }, ContextCompat.getMainExecutor(this));

        hide();


        position = new Point(mContentView.getWidth() / 2, mContentView.getHeight() / 2);
        overlayView.setPosistion(position);
        overlayView.invalidate();
    }

    private void bindPreview(ProcessCameraProvider processCameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(mContentView.getSurfaceProvider());
        Camera camera = processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHandler.postDelayed(mHidePart2Runnable, 100);
    }

    private class MLKitAnalyzer implements ImageAnalysis.Analyzer {
        TextRecognizer textRecognizer;

        public MLKitAnalyzer() {
            textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            Logger.i("Loaded text recognition models!");
        }

        @Override
        public void analyze(@NonNull ImageProxy image) {
            @SuppressLint("UnsafeOptInUsageError") Image image1 = image.getImage();
            Logger.i(position.toString());
            Logger.i("Canvas size: " + overlayView.getWidth() + ", " + overlayView.getHeight());
            int width = overlayView.getWidth();
            int height = overlayView.getHeight();
            int mx = Math.min(width, height) / 4;
            int my = mx / 2;
            int left = position.x - mx;
            int top = position.y - my;
            int right = position.x + mx;
            int bottom = position.y + my;

            if (image1 != null) {
                ImageConvertUtils convertUtils = ImageConvertUtils.getInstance();
                InputImage inputImage = InputImage.fromMediaImage(image1, image.getImageInfo().getRotationDegrees());
                Bitmap bitmap;
                try {
                    bitmap = convertUtils.convertToUpRightBitmap(inputImage);
                } catch (MlKitException e) {
                    Logger.e(e, "ERROR");
                    return;
                }
                Rect rect = new Rect(Math.max(0, left * bitmap.getWidth() / width),
                        Math.max(0, top * bitmap.getHeight() / height),
                        right * bitmap.getWidth() / width,
                        bottom * bitmap.getHeight() / height);
                Bitmap crop = Bitmap.createBitmap(bitmap, rect.left, rect.top,
                        Math.min(bitmap.getWidth(), rect.right) - rect.left,
                        Math.min(bitmap.getHeight(), rect.bottom) - rect.top);
                inputImage = InputImage.fromBitmap(crop, 0);
                Logger.i("Image size: " + inputImage.getWidth() + ", " + inputImage.getHeight());
                Task<Text> results = textRecognizer.process(inputImage)
                        .addOnSuccessListener(text -> {
//                            overlayView.setLatestText(text);
                            overlayView.setPosistion(position);
                            Logger.i("Found text:" + text.getText());
                            overlayView.invalidate();
                            TextView txtDetected = sheetView.findViewById(R.id.txt_detected);
                            txtDetected.setText(text.getText());
                        })
                        .addOnFailureListener(e -> {
                            Logger.e(e, "ERROR");
                        })
                        .addOnCompleteListener(task -> image.close());
            }
        }
    }
}