package cn.hzw.graffiti;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import cn.forward.androids.utils.ImageUtils;
import cn.forward.androids.utils.LogUtil;
import cn.forward.androids.utils.StatusBarUtil;
import cn.forward.androids.utils.ThreadUtil;
import cn.hzw.graffiti.bean.imageMessage;
import cn.hzw.graffiti.imagepicker.ImageSelectorView;

/**
 *
 * Created by XuewenLiao on 2018/5/26.
 */
public class GraffitiActivity extends Activity {

    public static final String TAG = "Graffiti";

    public static final int RESULT_ERROR = -111; // 出现错误

    public static final String IP = "http://172.20.10.9:8000/gan/";

    private int type = 1;// 1 建筑；2 街景；3 包；4 鞋
    private String URL = "";
    /**
     * 启动涂鸦界面
     *
     * @param activity
     * @param params      涂鸦参数
     * @param requestCode startActivityForResult的请求码
     * @see GraffitiParams
     */
//    public static void startActivityForResult(Activity activity, GraffitiParams params, int requestCode) {
//        Intent intent = new Intent(activity, GraffitiActivity.class);
//        intent.putExtra(GraffitiActivity.KEY_PARAMS, params);
//        activity.startActivityForResult(intent, requestCode);
//    }

    /**
     * 启动涂鸦界面
     *
     * @param activity
     * @param imagePath   　图片路径
     * @param savePath    　保存路径
     * @param isDir       　保存路径是否为目录
     * @param requestCode 　startActivityForResult的请求码
     */
//    @Deprecated
//    public static void startActivityForResult(Activity activity, String imagePath, String savePath, boolean isDir, int requestCode) {
//        GraffitiParams params = new GraffitiParams();
//        params.mImagePath = imagePath;
//        params.mSavePath = savePath;
//        params.mSavePathIsDir = isDir;
//        startActivityForResult(activity, params, requestCode);
//    }

//    /**
//     * {@link GraffitiActivity#startActivityForResult(Activity, String, String, boolean, int)}
//     */
//    @Deprecated
//    public static void startActivityForResult(Activity activity, String imagePath, int requestCode) {
//        GraffitiParams params = new GraffitiParams();
//        params.mImagePath = imagePath;
//        startActivityForResult(activity, params, requestCode);
//    }

    public static final String KEY_PARAMS = "key_graffiti_params";
    public static final String KEY_IMAGE_PATH = "key_image_path";

    private String mImagePath;
    private Bitmap mBitmap;

    private FrameLayout mFrameLayout;
    private GraffitiView mGraffitiView;

    private View.OnClickListener mOnClickListener;

    private SeekBar mPaintSizeBar;
    private TextView mPaintSizeView;

    private View mBtnColor;

    private boolean mIsMovingPic = false; // 是否是平移缩放模式

    private boolean mIsScaling;
    private final float mMaxScale = 4f; // 最大缩放倍数
    private final float mMinScale = 0.25f; // 最小缩放倍数
    private final int TIME_SPAN = 40;

    private View mBtnMovePic, mBtnHidePanel, mSettingsPanel;
    private View mShapeModeContainer;
    private View mSelectedTextEditContainer;
    private View mEditContainer;

    private AlphaAnimation mViewShowAnimation, mViewHideAnimation; // view隐藏和显示时用到的渐变动画

    private GraffitiParams mGraffitiParams;

    // 触摸屏幕超过一定时间才判断为需要隐藏设置面板
    private Runnable mHideDelayRunnable;
    // 触摸屏幕超过一定时间才判断为需要显示设置面板
    private Runnable mShowDelayRunnable;


    private TouchGestureDetector mTouchGestureDetector;
    private ImageView iv_showImage;

    //更新界面，将获取到的图片放到iv_showImage上
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    iv_showImage.setImageBitmap(bitmap);
                    break;
                case 2:
                    Toast.makeText(GraffitiActivity.this,"联网失败",Toast.LENGTH_SHORT);
                case 3:
                    int painttype = (int) msg.obj;
                    if (painttype == 1){    //画建筑
                        URL = IP + "facades_B2A/";
                    }else if (painttype == 2){  //画街景
                        URL = IP + "cityscapes_B2A/";
                    }else if (painttype == 3){  //画包
                        URL = IP + "handbags_B2A/";
                    }else if (painttype == 4){  //画鞋
                        URL = IP + "shoes_B2A/";
                    }else {
                        Toast.makeText(GraffitiActivity.this,"您还什么都没画",Toast.LENGTH_SHORT);
                    }
            }
        }
    };
    private HorizontalScrollView hl_building;
    private Button btn_wall;
    private Button btn_door;
    private Button btn_window;
    private Button btn_trim;
    private Button btn_column;
    private Button btn_floor;
    private Button btn_grass;
    private Button btn_car;
    private Button btn_tree;
    private Button btn_lamp;


//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelable(KEY_PARAMS, mGraffitiParams);
//    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
//        super.onRestoreInstanceState(savedInstanceState, persistentState);
//        mGraffitiParams = savedInstanceState.getParcelable(KEY_PARAMS);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //初始化xutils3.5
        x.Ext.init(getApplication());
        x.Ext.setDebug(org.xutils.BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
        x.view().inject(this);

        StatusBarUtil.setStatusBarTranslucent(this, true, false);

        mGraffitiParams = new GraffitiParams();
        // 初始画笔大小
        mGraffitiParams.mPaintSize = 1;

//        if (mGraffitiParams == null) {
//            mGraffitiParams = getIntent().getExtras().getParcelable(KEY_PARAMS);
//        }
//        if (mGraffitiParams == null) {
//            LogUtil.e("TAG", "mGraffitiParams is null!");
//            this.finish();
//            return;
//        }
//
//        mImagePath = mGraffitiParams.mImagePath;
//        if (mImagePath == null) {
//            LogUtil.e("TAG", "mImagePath is null!");
//            this.finish();
//            return;
//        }
//        LogUtil.d("TAG", mImagePath);
//        if (mGraffitiParams.mIsFullScreen) {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        mBitmap = ImageUtils.createBitmapFromPath(mImagePath, this);
        Drawable drawable = getResources().getDrawable(R.drawable.canvas);
        BitmapDrawable bd = (BitmapDrawable) drawable;
        mBitmap = bd.getBitmap();
        if (mBitmap == null) {
            LogUtil.e("TAG", "bitmap is null!");
            this.finish();
            return;
        }

        setContentView(R.layout.layout_graffiti);
        mFrameLayout = (FrameLayout) findViewById(R.id.graffiti_container);

        // /storage/emulated/0/DCIM/Graffiti/1479369280029.jpg

        mGraffitiView = addCanvas(mBitmap);

//        mGraffitiView = new GraffitiView(this, mBitmap, mGraffitiParams.mEraserPath, mGraffitiParams.mEraserImageIsResizeable,
//                new GraffitiListener() {
//                    @Override
//                    public void onSaved(Bitmap bitmap, Bitmap bitmapEraser) { // 保存图片为jpg格式
//                        if (bitmapEraser != null) {
//                            bitmapEraser.recycle(); // 回收图片，不再涂鸦，避免内存溢出
//                        }
//                        File graffitiFile = null;
//                        File file = null;
//                        String savePath = mGraffitiParams.mSavePath;
//                        boolean isDir = mGraffitiParams.mSavePathIsDir;
//                        if (TextUtils.isEmpty(savePath)) {
//                            File dcimFile = new File(Environment.getExternalStorageDirectory(), "DCIM");
//                            graffitiFile = new File(dcimFile, "Graffiti");
//                            //　保存的路径
//                            file = new File(graffitiFile, System.currentTimeMillis() + ".jpg");
//                        } else {
//                            if (isDir) {
//                                graffitiFile = new File(savePath);
//                                //　保存的路径
//                                file = new File(graffitiFile, System.currentTimeMillis() + ".jpg");
//                            } else {
//                                file = new File(savePath);
//                                graffitiFile = file.getParentFile();
//                            }
//                        }
//                        graffitiFile.mkdirs();
//
//                        FileOutputStream outputStream = null;
//                        try {
//                            outputStream = new FileOutputStream(file);
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
//                            ImageUtils.addImage(getContentResolver(), file.getAbsolutePath());
//                            Intent intent = new Intent();
//                            intent.putExtra(KEY_IMAGE_PATH, file.getAbsolutePath());
//                            setResult(Activity.RESULT_OK, intent);
//                            finish();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            onError(GraffitiView.ERROR_SAVE, e.getMessage());
//                        } finally {
//                            if (outputStream != null) {
//                                try {
//                                    outputStream.close();
//                                } catch (IOException e) {
//                                }
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onError(int i, String msg) {
//                        setResult(RESULT_ERROR);
//                        finish();
//                    }
//
//                    @Override
//                    public void onReady() {
//                        // 设置初始值
//                        mGraffitiView.setPaintSize(mGraffitiParams.mPaintSize > 0 ? mGraffitiParams.mPaintSize
//                                : mGraffitiView.getPaintSize());
//                        mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
//                        mPaintSizeBar.setMax((int) (Math.min(mGraffitiView.getBitmapWidthOnView(),
//                                mGraffitiView.getBitmapHeightOnView()) / 3 * DrawUtil.GRAFFITI_PIXEL_UNIT));
//                        mPaintSizeView.setText("" + mPaintSizeBar.getProgress());
//                        // 选择画笔
//                        findViewById(R.id.btn_pen_hand).performClick();
//                        findViewById(R.id.btn_hand_write).performClick();
//                    }
//
//                    @Override
//                    public void onSelectedItem(GraffitiSelectableItem selectableItem, boolean selected) {
//                        if (selected) {
//                            mSelectedTextEditContainer.setVisibility(View.VISIBLE);
//                            if (mGraffitiView.getSelectedItemColor().getType() == GraffitiColor.Type.BITMAP) {
//                                mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getSelectedItemColor().getBitmap()));
//                            } else {
//                                mBtnColor.setBackgroundColor(mGraffitiView.getSelectedItemColor().getColor());
//                            }
//                            mPaintSizeBar.setProgress((int) (mGraffitiView.getSelectedItemSize() + 0.5f));
//                        } else {
//                            mSelectedTextEditContainer.setVisibility(View.GONE);
//                            mEditContainer.setVisibility(View.VISIBLE);
//                            if (mGraffitiView.getColor().getType() == GraffitiColor.Type.BITMAP) {
//                                mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getColor().getBitmap()));
//                            } else {
//                                mBtnColor.setBackgroundColor(mGraffitiView.getColor().getColor());
//                            }
//
//                            mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
//                        }
//                    }
//
//                    @Override
//                    public void onCreateSelectableItem(GraffitiView.Pen pen, float x, float y) {
//                        if (pen == GraffitiView.Pen.TEXT) {
//                            createGraffitiText(null, x, y);
//                        } else if (pen == GraffitiView.Pen.BITMAP) {
//                            createGraffitiBitmap(null, x, y);
//                        }
//                    }
//                });
//
////        mGraffitiView.setCor;
//        mGraffitiView.setIsDrawableOutside(mGraffitiParams.mIsDrawableOutside);
//        mFrameLayout.addView(mGraffitiView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
////        mFrameLayout.addView(mGraffitiView, 100, 100);


        mOnClickListener = new GraffitiOnClickListener();
//        mTouchGestureDetector = new TouchGestureDetector(this, new GraffitiGestureListener());
//
        initView();
    }

    //沉浸式状态栏
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    private GraffitiView addCanvas(Bitmap bitmap){

        GraffitiView addGraffitiView = new GraffitiView(this, bitmap, mGraffitiParams.mEraserPath, mGraffitiParams.mEraserImageIsResizeable,
                new GraffitiListener() {
                    @Override
                    public void onSaved(Bitmap bitmap, Bitmap bitmapEraser) { // 保存图片为jpg格式
                        if (bitmapEraser != null) {
                            bitmapEraser.recycle(); // 回收图片，不再涂鸦，避免内存溢出
                        }
                        File graffitiFile = null;
                        File file = null;
                        String savePath = mGraffitiParams.mSavePath;
                        boolean isDir = mGraffitiParams.mSavePathIsDir;
                        if (TextUtils.isEmpty(savePath)) {
                            File dcimFile = new File(Environment.getExternalStorageDirectory(), "DCIM");
                            graffitiFile = new File(dcimFile, "Graffiti");
                            //　保存的路径
//                            file = new File(graffitiFile, System.currentTimeMillis() + ".JPEG");
                            file = new File(graffitiFile, "draw" + ".JPEG");//文件保存为draw.JPEG，保证本地只有一张图片
                        } else {
                            if (isDir) {
                                graffitiFile = new File(savePath);
                                //　保存的路径
//                                file = new File(graffitiFile, System.currentTimeMillis() + ".JPEG");
                                file = new File(graffitiFile, "draw" + ".JPEG");
                            } else {
                                file = new File(savePath);
                                graffitiFile = file.getParentFile();
                            }
                        }
                        graffitiFile.mkdirs();

                        FileOutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                            ImageUtils.addImage(getContentResolver(), file.getAbsolutePath());
                            Intent intent = new Intent();
                            intent.putExtra(KEY_IMAGE_PATH, file.getAbsolutePath());
                            setResult(Activity.RESULT_OK, intent);
//                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            onError(GraffitiView.ERROR_SAVE, e.getMessage());
                        } finally {
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(int i, String msg) {
                        setResult(RESULT_ERROR);
                        finish();
                    }

                    @Override
                    public void onReady() {
                        // 设置初始值
                        mGraffitiView.setPaintSize(mGraffitiParams.mPaintSize > 0 ? mGraffitiParams.mPaintSize
                                : mGraffitiView.getPaintSize());
                        mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                        mPaintSizeBar.setMax((int) (Math.min(mGraffitiView.getBitmapWidthOnView(),
                                mGraffitiView.getBitmapHeightOnView()) / 3 * DrawUtil.GRAFFITI_PIXEL_UNIT));
                        mPaintSizeView.setText("" + mPaintSizeBar.getProgress());
                        // 选择画笔
                        findViewById(R.id.btn_pen_hand).performClick();
                        findViewById(R.id.btn_hand_write).performClick();
                    }

                    @Override
                    public void onSelectedItem(GraffitiSelectableItem selectableItem, boolean selected) {
                        if (selected) {
                            mSelectedTextEditContainer.setVisibility(View.VISIBLE);
                            if (mGraffitiView.getSelectedItemColor().getType() == GraffitiColor.Type.BITMAP) {
                                mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getSelectedItemColor().getBitmap()));
                            } else {
                                mBtnColor.setBackgroundColor(mGraffitiView.getSelectedItemColor().getColor());
                            }
                            mPaintSizeBar.setProgress((int) (mGraffitiView.getSelectedItemSize() + 0.5f));
                        } else {
                            mSelectedTextEditContainer.setVisibility(View.GONE);
                            mEditContainer.setVisibility(View.VISIBLE);
                            if (mGraffitiView.getColor().getType() == GraffitiColor.Type.BITMAP) {
                                mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getColor().getBitmap()));
                            } else {
                                mBtnColor.setBackgroundColor(mGraffitiView.getColor().getColor());
                            }

                            mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                        }
                    }

                    @Override
                    public void onCreateSelectableItem(GraffitiView.Pen pen, float x, float y) {
                        if (pen == GraffitiView.Pen.TEXT) {
                            createGraffitiText(null, x, y);
                        } else if (pen == GraffitiView.Pen.BITMAP) {
                            createGraffitiBitmap(null, x, y);
                        }
                    }
                });
        addGraffitiView.setIsDrawableOutside(mGraffitiParams.mIsDrawableOutside);
        mFrameLayout.addView(addGraffitiView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        mFrameLayout.addView(mGraffitiView, 100, 100);
//        mOnClickListener = new GraffitiOnClickListener();
//        mTouchGestureDetector = new TouchGestureDetector(this, new GraffitiGestureListener());

//        initView();
//        mOnClickListener = new GraffitiOnClickListener();
//        mTouchGestureDetector = new TouchGestureDetector(this, new GraffitiGestureListener());

//        initView();
        return addGraffitiView;

    }

    // 添加文字
    private void createGraffitiText(final GraffitiText graffitiText, final float x, final float y) {
        Activity activity = this;
        if (isFinishing()) {
            return;
        }

        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Dialog dialog = null;
        if (fullScreen) {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        final Dialog finalDialog1 = dialog;
        activity.getWindow().getDecorView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                finalDialog1.dismiss();
            }
        });

        ViewGroup container = (ViewGroup) View.inflate(getApplicationContext(), R.layout.graffiti_create_text, null);
        final Dialog finalDialog = dialog;
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });
        dialog.setContentView(container);

        final EditText textView = (EditText) container.findViewById(R.id.graffiti_selectable_edit);
        final View cancelBtn = container.findViewById(R.id.graffiti_text_cancel_btn);
        final TextView enterBtn = (TextView) container.findViewById(R.id.graffiti_text_enter_btn);

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = (textView.getText() + "").trim();
                if (TextUtils.isEmpty(text)) {
                    enterBtn.setEnabled(false);
                    enterBtn.setTextColor(0xffb3b3b3);
                } else {
                    enterBtn.setEnabled(true);
                    enterBtn.setTextColor(0xff232323);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        textView.setText(graffitiText == null ? "" : graffitiText.getText());

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBtn.setSelected(true);
                finalDialog.dismiss();
            }
        });

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (cancelBtn.isSelected()) {
                    mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                    return;
                }
                String text = (textView.getText() + "").trim();
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                if (graffitiText == null) {
                    mGraffitiView.addSelectableItem(new GraffitiText(mGraffitiView.getPen(), text, mGraffitiView.getPaintSize(), mGraffitiView.getColor().copy(),
                            0, mGraffitiView.getGraffitiRotateDegree(), x, y, mGraffitiView.getOriginalPivotX(), mGraffitiView.getOriginalPivotY()));
                } else {
                    graffitiText.setText(text);
                }
                mGraffitiView.invalidate();
            }
        });

        if (graffitiText == null) {
            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
        }
    }

    // 添加贴图
    private void createGraffitiBitmap(final GraffitiBitmap graffitiBitmap, final float x, final float y) {
        Activity activity = this;

        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Dialog dialog = null;
        if (fullScreen) {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        ViewGroup container = (ViewGroup) View.inflate(getApplicationContext(), R.layout.graffiti_create_bitmap, null);
        final Dialog finalDialog = dialog;
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });
        dialog.setContentView(container);

        ViewGroup selectorContainer = (ViewGroup) finalDialog.findViewById(R.id.graffiti_image_selector_container);
        ImageSelectorView selectorView = new ImageSelectorView(this, false, 1, null, new ImageSelectorView.ImageSelectorListener() {
            @Override
            public void onCancel() {
                finalDialog.dismiss();
            }

            @Override
            public void onEnter(List<String> pathList) {
                finalDialog.dismiss();
                Bitmap bitmap = ImageUtils.createBitmapFromPath(pathList.get(0), mGraffitiView.getWidth() / 4, mGraffitiView.getHeight() / 4);

                if (graffitiBitmap == null) {
                    mGraffitiView.addSelectableItem(new GraffitiBitmap(mGraffitiView.getPen(), bitmap, mGraffitiView.getPaintSize(), mGraffitiView.getColor().copy(),
                            0, mGraffitiView.getGraffitiRotateDegree(), x, y, mGraffitiView.getOriginalPivotX(), mGraffitiView.getOriginalPivotY()));
                } else {
                    graffitiBitmap.setBitmap(bitmap);
                }
                mGraffitiView.invalidate();
            }
        });
        selectorContainer.addView(selectorView);
    }

    private void initView() {

        //颜色按钮
        btn_wall = (Button) findViewById(R.id.btn_wall);
        btn_wall.setOnClickListener(mOnClickListener);

        btn_door = (Button) findViewById(R.id.btn_door);
        btn_door.setOnClickListener(mOnClickListener);

        btn_window = (Button) findViewById(R.id.btn_window);
        btn_window.setOnClickListener(mOnClickListener);

        btn_trim = (Button) findViewById(R.id.btn_trim);
        btn_trim.setOnClickListener(mOnClickListener);

        btn_column = (Button) findViewById(R.id.btn_column);
        btn_column.setOnClickListener(mOnClickListener);

        btn_floor = (Button) findViewById(R.id.btn_floor);
        btn_floor.setOnClickListener(mOnClickListener);

        btn_grass = (Button) findViewById(R.id.btn_grass);
        btn_grass.setOnClickListener(mOnClickListener);

        btn_car = (Button) findViewById(R.id.btn_car);
        btn_car.setOnClickListener(mOnClickListener);

        btn_tree = (Button) findViewById(R.id.btn_tree);
        btn_tree.setOnClickListener(mOnClickListener);

        btn_lamp = (Button) findViewById(R.id.btn_lamp);
        btn_lamp.setOnClickListener(mOnClickListener);

//        findViewById(R.id.btn_wall).setOnClickListener(mOnClickListener);
//        findViewById(R.id.btn_door).setOnClickListener(mOnClickListener);
//        findViewById(R.id.btn_window).setOnClickListener(mOnClickListener);
//        findViewById(R.id.btn_trim).setOnClickListener(mOnClickListener);
//        findViewById(R.id.btn_column).setOnClickListener(mOnClickListener);
//        findViewById(R.id.btn_floor).setOnClickListener(mOnClickListener);
//        findViewById(R.id.btn_grass).setOnClickListener(mOnClickListener);
//        findViewById(R.id.btn_car).setOnClickListener(mOnClickListener);
//        findViewById(R.id.btn_tree).setOnClickListener(mOnClickListener);
//        findViewById(R.id.btn_lamp).setOnClickListener(mOnClickListener);


        findViewById(R.id.btn_cityscapes).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_shoes).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_bag).setOnClickListener(mOnClickListener);



        findViewById(R.id.btn_test).setOnClickListener(mOnClickListener);
        iv_showImage = (ImageView) findViewById(R.id.iv_showimage);//生成图片区

        hl_building = (HorizontalScrollView) findViewById(R.id.hl_building);//显示画建筑的工具



        findViewById(R.id.btn_pen_hand).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_copy).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_eraser).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_text).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_pen_bitmap).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_hand_write).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_arrow).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_line).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_holl_circle).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_fill_circle).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_holl_rect).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_fill_rect).setOnClickListener(mOnClickListener);;


        findViewById(R.id.btn_clear).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_undo).setOnClickListener(mOnClickListener);
        findViewById(R.id.graffiti_selectable_edit).setOnClickListener(mOnClickListener);
        findViewById(R.id.graffiti_selectable_remove).setOnClickListener(mOnClickListener);
        findViewById(R.id.graffiti_selectable_top).setOnClickListener(mOnClickListener);
        mShapeModeContainer = findViewById(R.id.bar_shape_mode);
        mSelectedTextEditContainer = findViewById(R.id.graffiti_selectable_edit_container);
        mEditContainer = findViewById(R.id.graffiti_edit_container);
        mBtnHidePanel = findViewById(R.id.graffiti_btn_hide_panel);
        mBtnHidePanel.setOnClickListener(mOnClickListener);
        findViewById(R.id.graffiti_btn_finish).setOnClickListener(mOnClickListener);
        findViewById(R.id.graffiti_btn_back).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_centre_pic).setOnClickListener(mOnClickListener);
        mBtnMovePic = findViewById(R.id.btn_move_pic);
        mBtnMovePic.setOnClickListener(mOnClickListener);

        //选择颜色框按钮
        mBtnColor = findViewById(R.id.btn_set_color);
        mBtnColor.setOnClickListener(mOnClickListener);
        mSettingsPanel = findViewById(R.id.graffiti_panel);
        if (mGraffitiView.getGraffitiColor().getType() == GraffitiColor.Type.COLOR) {
            mBtnColor.setBackgroundColor(mGraffitiView.getGraffitiColor().getColor());
        } else if (mGraffitiView.getGraffitiColor().getType() == GraffitiColor.Type.BITMAP) {
            mBtnColor.setBackgroundDrawable(new BitmapDrawable(mGraffitiView.getGraffitiColor().getBitmap()));
        }

        mPaintSizeBar = (SeekBar) findViewById(R.id.paint_size);
        mPaintSizeView = (TextView) findViewById(R.id.paint_size_text);
        // 设置画笔的进度条
        mPaintSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    mPaintSizeBar.setProgress(1);
                    return;
                }
                mPaintSizeView.setText("" + progress);
                if (mGraffitiView.isSelectedItem()) {
                    mGraffitiView.setSelectedItemSize(progress);
                } else {
                    mGraffitiView.setPaintSize(progress);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //  放大缩小按钮监听
//        ScaleOnTouchListener onTouchListener = new ScaleOnTouchListener();
//        findViewById(R.id.btn_amplifier).setOnTouchListener(onTouchListener);
//        findViewById(R.id.btn_reduce).setOnTouchListener(onTouchListener);

        // 添加涂鸦的触摸监听器，移动图片位置
//        mGraffitiView.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // 隐藏设置面板
//                if (!mBtnHidePanel.isSelected()  // 设置面板没有被隐藏
//                        && mGraffitiParams.mChangePanelVisibilityDelay > 0) {
//                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                        case MotionEvent.ACTION_DOWN:
//                            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
//                            mSettingsPanel.removeCallbacks(mShowDelayRunnable);
//                            //触摸屏幕超过一定时间才判断为需要隐藏设置面板
//                            mSettingsPanel.postDelayed(mHideDelayRunnable, mGraffitiParams.mChangePanelVisibilityDelay);
//                            break;
//                        case MotionEvent.ACTION_CANCEL:
//                        case MotionEvent.ACTION_UP:
//                            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
//                            mSettingsPanel.removeCallbacks(mShowDelayRunnable);
//                            //离开屏幕超过一定时间才判断为需要显示设置面板
//                            mSettingsPanel.postDelayed(mShowDelayRunnable, mGraffitiParams.mChangePanelVisibilityDelay);
//                            break;
//                    }
//                } else if (mBtnHidePanel.isSelected() && mGraffitiView.getAmplifierScale() > 0) {
//                    mGraffitiView.setAmplifierScale(-1);
//                }
//
//                if (!mIsMovingPic) { // 非移动缩放模式
//                    return false;  // 交给下一层的涂鸦View处理
//                }
//                // 处理手势
//                mTouchGestureDetector.onTouchEvent(event);
//                return true;
//            }
//        });

        // 长按标题栏显示原图
        findViewById(R.id.graffiti_txt_title).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mGraffitiView.setJustDrawOriginal(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mGraffitiView.setJustDrawOriginal(false);
                        break;
                }
                return true;
            }
        });

        mViewShowAnimation = new AlphaAnimation(0, 1);
        mViewShowAnimation.setDuration(500);
        mViewHideAnimation = new AlphaAnimation(1, 0);
        mViewHideAnimation.setDuration(500);
        mHideDelayRunnable = new Runnable() {
            public void run() {
                hideView(mSettingsPanel);
            }

        };
        mShowDelayRunnable = new Runnable() {
            public void run() {
                showView(mSettingsPanel);
            }
        };

        // 旋转图片
        findViewById(R.id.graffiti_btn_rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGraffitiView.rotate(mGraffitiView.getGraffitiRotateDegree() + 90);
            }
        });
    }

    private class GraffitiOnClickListener implements View.OnClickListener {

        private View mLastPenView, mLastShapeView;
        private boolean mDone = false;

        @Override
        public void onClick(View v) {
            mDone = false;
            if (v.getId() == R.id.btn_pen_hand) {
                mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                mShapeModeContainer.setVisibility(View.VISIBLE);
                mGraffitiView.setPen(GraffitiView.Pen.HAND);
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_copy) {
                mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                mShapeModeContainer.setVisibility(View.VISIBLE);
                mGraffitiView.setPen(GraffitiView.Pen.COPY);
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_eraser) {
                mPaintSizeBar.setProgress((int) (mGraffitiView.getPaintSize() + 0.5f));
                mShapeModeContainer.setVisibility(View.VISIBLE);
                mGraffitiView.setPen(GraffitiView.Pen.ERASER);
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_text) {
                mShapeModeContainer.setVisibility(View.GONE);
                mGraffitiView.setPen(GraffitiView.Pen.TEXT);
                mDone = true;
            } else if (v.getId() == R.id.btn_pen_bitmap) {
                mShapeModeContainer.setVisibility(View.GONE);
                mGraffitiView.setPen(GraffitiView.Pen.BITMAP);
                mDone = true;
            }else if (v.getId() == R.id.btn_wall){
                mGraffitiView.setColor(Color.parseColor("#163BF7"));
                mDone = true;
            }else if (v.getId() == R.id.btn_door){
                mGraffitiView.setColor(Color.parseColor("#9B000F"));
                mDone = true;
            }else if (v.getId() == R.id.btn_window){
                mGraffitiView.setColor(Color.parseColor("#136DFB"));
                mDone = true;
            }else if (v.getId() == R.id.btn_trim){
                mGraffitiView.setColor(Color.parseColor("#FD8821"));
                mDone = true;
            }else if (v.getId() == R.id.btn_column){
                mGraffitiView.setColor(Color.parseColor("#F20017"));
                mDone = true;
            }else if (v.getId() == R.id.btn_floor){
                mGraffitiView.setColor(Color.parseColor("#8B397D"));
                mDone = true;
            }else if (v.getId() == R.id.btn_grass){
                mGraffitiView.setColor(Color.parseColor("#6CFF9A"));
                mDone = true;
            }else if (v.getId() == R.id.btn_car){
                mGraffitiView.setColor(Color.parseColor("#1E008E"));
                mDone = true;
            }else if (v.getId() == R.id.btn_tree){
                mGraffitiView.setColor(Color.parseColor("#5C912C"));
                mDone = true;
            }else if (v.getId() == R.id.btn_lamp){
                mGraffitiView.setColor(Color.parseColor("#DBDF21"));
                mDone = true;
            }


            if (mDone) {
                if (mLastPenView != null) {
                    mLastPenView.setSelected(false);
                }
                v.setSelected(true);
                mLastPenView = v;
                return;
            }

            if (v.getId() == R.id.btn_clear) {
                if (!(GraffitiParams.getDialogInterceptor() != null
                        && GraffitiParams.getDialogInterceptor().onShow(GraffitiActivity.this, mGraffitiView, GraffitiParams.DialogType.CLEAR_ALL))) {
                    DialogController.showEnterCancelDialog(GraffitiActivity.this,
                            getString(R.string.graffiti_clear_screen), getString(R.string.graffiti_cant_undo_after_clearing),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mGraffitiView.clear();
                                }
                            }, null
                    );
                }
                mDone = true;
            } else if (v.getId() == R.id.btn_undo) {
                mGraffitiView.undo();
                mDone = true;
            } else if (v.getId() == R.id.btn_set_color) {
                if (!(GraffitiParams.getDialogInterceptor() != null
                        && GraffitiParams.getDialogInterceptor().onShow(GraffitiActivity.this, mGraffitiView, GraffitiParams.DialogType.COLOR_PICKER))) {
                    new ColorPickerDialog(GraffitiActivity.this, mGraffitiView.getGraffitiColor().getColor(), "画笔颜色",
                            new ColorPickerDialog.OnColorChangedListener() {
                                public void colorChanged(int color) {
                                    mBtnColor.setBackgroundColor(color);
                                    if (mGraffitiView.isSelectedItem()) {
                                        mGraffitiView.setSelectedItemColor(color);
                                    } else {
                                        mGraffitiView.setColor(color);
                                    }
                                }
                                @Override
                                public void colorChanged(Drawable color) {
                                    mBtnColor.setBackgroundDrawable(color);
                                    if (mGraffitiView.isSelectedItem()) {
                                        mGraffitiView.setSelectedItemColor(ImageUtils.getBitmapFromDrawable(color));
                                    } else {
                                        mGraffitiView.setColor(ImageUtils.getBitmapFromDrawable(color));
                                    }
                                }
                            }).show();
                }
                mDone = true;
            }
            if (mDone) {
                return;
            }

            if (v.getId() == R.id.graffiti_btn_hide_panel) {
                mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                mSettingsPanel.removeCallbacks(mShowDelayRunnable);
                v.setSelected(!v.isSelected());
                if (!mBtnHidePanel.isSelected()) {
                    showView(mSettingsPanel);
                } else {
                    hideView(mSettingsPanel);
                }
                mDone = true;
            } else if (v.getId() == R.id.graffiti_btn_finish) {

                mGraffitiView.save();
                mDone = true;


                android.os.Process.killProcess(android.os.Process.myPid()); //获取PID
                System.exit(0); //常规java、c#的标准退出法，返回值为0代表正常退出

            } else if (v.getId() == R.id.graffiti_btn_back) {
                if (!mGraffitiView.isModified()) {
                    finish();
                    return;
                }
                if (!(GraffitiParams.getDialogInterceptor() != null
                        && GraffitiParams.getDialogInterceptor().onShow(GraffitiActivity.this, mGraffitiView, GraffitiParams.DialogType.SAVE))) {
                    DialogController.showEnterCancelDialog(GraffitiActivity.this, getString(R.string.graffiti_saving_picture), null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mGraffitiView.save();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            finish();
                            android.os.Process.killProcess(android.os.Process.myPid()); //获取PID
                            System.exit(0); //常规java、c#的标准退出法，返回值为0代表正常退出

                        }
                    });
                }
                mDone = true;
            } else if (v.getId() == R.id.btn_centre_pic) {
                mGraffitiView.centrePic();
                mDone = true;
            } else if (v.getId() == R.id.btn_move_pic) {
                v.setSelected(!v.isSelected());
                mIsMovingPic = v.isSelected();
                if (mIsMovingPic) {
                    Toast.makeText(getApplicationContext(), R.string.graffiti_moving_pic, Toast.LENGTH_SHORT).show();
                }
                mDone = true;
            }
            if (mDone) {
                return;
            }


            if (v.getId() == R.id.graffiti_selectable_edit) {
                if (mGraffitiView.getSelectedItem() instanceof GraffitiText) {
                    createGraffitiText((GraffitiText) mGraffitiView.getSelectedItem(), -1, -1);
                } else if (mGraffitiView.getSelectedItem() instanceof GraffitiBitmap) {
                    createGraffitiBitmap((GraffitiBitmap) mGraffitiView.getSelectedItem(), -1, -1);
                }
                mDone = true;
            } else if (v.getId() == R.id.graffiti_selectable_remove) {
                mGraffitiView.removeSelectedItem();
                mDone = true;
            } else if (v.getId() == R.id.graffiti_selectable_top) {
                mGraffitiView.topSelectedItem();
                mDone = true;
            }
            if (mDone) {
                return;
            }

            if (v.getId() == R.id.btn_hand_write) {//手绘
                mGraffitiView.setShape(GraffitiView.Shape.HAND_WRITE);
                mGraffitiView.setPaintSize(1);//设置画笔大小
                mGraffitiView.setColor(Color.parseColor("#000000"));
                sendBitmap(R.drawable.canvas);//背景换为白板
                //隐藏画建筑工具
                hl_building.setVisibility(View.GONE);
            } else if (v.getId() == R.id.btn_arrow) {
                mGraffitiView.setShape(GraffitiView.Shape.ARROW);
            } else if (v.getId() == R.id.btn_line) {
                mGraffitiView.setShape(GraffitiView.Shape.LINE);
            } else if (v.getId() == R.id.btn_holl_circle) {
                mGraffitiView.setShape(GraffitiView.Shape.HOLLOW_CIRCLE);
            } else if (v.getId() == R.id.btn_fill_circle) {
                mGraffitiView.setShape(GraffitiView.Shape.FILL_CIRCLE);
            } else if (v.getId() == R.id.btn_holl_rect) {
                mGraffitiView.setShape(GraffitiView.Shape.HOLLOW_RECT);
            } else if (v.getId() == R.id.btn_fill_rect) {//画建筑


//                mGraffitiView.setShape(GraffitiView.Shape.FILL_RECT);


//                Drawable drawable = getResources().getDrawable(R.drawable.test_shoes);
//                BitmapDrawable bd = (BitmapDrawable) drawable;
//                mBitmap = bd.getBitmap();

//                Paint paint = new Paint();
//                paint.setColor(Color.parseColor("#0B15D1"));
//                Bitmap newBitmap = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(),mBitmap.getConfig());
//                Canvas canvas = new Canvas(newBitmap);
////                canvas.drawRect(0,0,mBitmap.getWidth(),mBitmap.getHeight(),paint);
////                canvas.drawBitmap(mBitmap,0,0,paint);
//                canvas.drawColor(Color.parseColor("#0B15D1"));
//                mBitmap = newBitmap;

//                mGraffitiView.clear();//清屏
//                mDone = true;

                type = 1;
                Message msg = new Message();
                msg.what = 3;
                msg.obj = type;
                mHandler.sendMessage(msg);

                //点击“画建筑”换背景（把白板换成蓝色）
                Bitmap newBitmap = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(),mBitmap.getConfig());
                Canvas canvas = new Canvas(newBitmap);
                Paint paint = new Paint();
                paint.setColorFilter(new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(mBitmap,0,0,paint);
                mBitmap = newBitmap;

                mGraffitiView.setCavBitmap(mBitmap);
                //画的形状
                mGraffitiView.setShape(GraffitiView.Shape.FILL_RECT);

                //出现画建筑工具
                hl_building.setVisibility(View.VISIBLE);

                btn_wall.setVisibility(View.VISIBLE);
                btn_door.setVisibility(View.VISIBLE);
                btn_window.setVisibility(View.VISIBLE);
                btn_trim.setVisibility(View.VISIBLE);
                btn_column.setVisibility(View.VISIBLE);

                //隐藏街景工具栏
                btn_floor.setVisibility(View.GONE);
                btn_grass.setVisibility(View.GONE);
                btn_car.setVisibility(View.GONE);
                btn_tree.setVisibility(View.GONE);
                btn_lamp.setVisibility(View.GONE);


                mGraffitiView.clear();//清屏
                mDone = true;


            }else if (v.getId() == R.id.btn_cityscapes){//画街景

                type = 2;
                Message msg = new Message();
                msg.what = 3;
                msg.obj = type;
                mHandler.sendMessage(msg);

                sendBitmap(R.drawable.inputcityscapes);
                mGraffitiView.setShape(GraffitiView.Shape.HAND_WRITE);
                //出现画建筑工具
                hl_building.setVisibility(View.VISIBLE);

                btn_floor.setVisibility(View.VISIBLE);
                btn_grass.setVisibility(View.VISIBLE);
                btn_car.setVisibility(View.VISIBLE);
                btn_tree.setVisibility(View.VISIBLE);
                btn_lamp.setVisibility(View.VISIBLE);

                //隐藏建筑工具栏
                btn_wall.setVisibility(View.GONE);
                btn_door.setVisibility(View.GONE);
                btn_window.setVisibility(View.GONE);
                btn_trim.setVisibility(View.GONE);
                btn_column.setVisibility(View.GONE);

                mGraffitiView.setPaintSize(10.5f);
                mGraffitiView.clear();//清屏
                mDone = true;

            }else if (v.getId() == R.id.btn_shoes){//画鞋

                type = 3;
                Message msg = new Message();
                msg.what = 3;
                msg.obj = type;
                mHandler.sendMessage(msg);

                sendBitmap(R.drawable.inputshoes);
                //隐藏画建筑工具
                hl_building.setVisibility(View.GONE);
                mGraffitiView.setPaintSize(1);//设置画笔大小
                mGraffitiView.setColor(Color.parseColor("#000000"));
                mGraffitiView.clear();//清屏
                mDone = true;

            }else if (v.getId() == R.id.btn_bag){//画包

                type = 4;
                Message msg = new Message();
                msg.what = 3;
                msg.obj = type;
                mHandler.sendMessage(msg);

                sendBitmap(R.drawable.inputhandbags);
                //隐藏画建筑工具
                hl_building.setVisibility(View.GONE);
                mGraffitiView.setPaintSize(1);//设置画笔大小
                mGraffitiView.setColor(Color.parseColor("#000000"));
                mGraffitiView.clear();//清屏
                mDone = true;

            }else if (v.getId() == R.id.btn_test){//生成测试

                //保存现场
                mGraffitiView.save();

                // TODO: 2018/5/23 发送图片

                Thread thread = new Thread(new sendImageThread());
                thread.start();


//                String filePath = Environment.getExternalStorageDirectory() + "/DCIM" + "/Graffiti/draw.JPEG";//本地图片路径
//                File file = new File(filePath);
//                if (file.exists()){
//                    Bitmap bm = BitmapFactory.decodeFile(filePath);
//                    iv_showImage.setImageBitmap(bm);
//                }

            }

            if (mLastShapeView != null) {
                mLastShapeView.setSelected(false);
            }
            v.setSelected(true);
            mLastShapeView = v;
        }

        private void sendBitmap(int resources) {
            Drawable drawable = getResources().getDrawable(resources);
                BitmapDrawable bd = (BitmapDrawable) drawable;
                mBitmap = bd.getBitmap();

            mGraffitiView.setCavBitmap(mBitmap);
            mGraffitiView.setShape(GraffitiView.Shape.HAND_WRITE);

            mGraffitiView.clear();//清屏
            mDone = true;
        }
    }

    @Override
    public void onBackPressed() { // 返回键监听

        if (mBtnMovePic.isSelected()) { // 当前是移动缩放模式，则退出该模式
            mBtnMovePic.performClick();
            return;
        } else { // 退出涂鸦
            findViewById(R.id.graffiti_btn_back).performClick();
        }

    }

    /**
     * 放大缩小
     */
    private class ScaleOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (v.getId() == R.id.btn_amplifier) {
                        scalePic(0.05f);
                    } else if (v.getId() == R.id.btn_reduce) {
                        scalePic(-0.05f);
                    }
                    v.setSelected(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIsScaling = false;
                    v.setSelected(false);
                    break;
            }
            return true;
        }
    }

    /**
     * 缩放
     *
     * @param scaleStep 缩放步进
     */
    private void scalePic(final float scaleStep) {
        if (mIsScaling)
            return;
        mIsScaling = true;

        final float x = mGraffitiView.toX(mGraffitiView.getWidth() / 2);
        final float y = mGraffitiView.toY(mGraffitiView.getHeight() / 2);

        final Runnable task = new Runnable() {
            public void run() {
                if (!mIsScaling)
                    return;
                float scale = mGraffitiView.getScale();
                scale += scaleStep;
                if (scale > mMaxScale) {
                    scale = mMaxScale;
                    mIsScaling = false;
                } else if (scale < mMinScale) {
                    scale = mMinScale;
                    mIsScaling = false;
                }
                // 围绕屏幕中心缩放
                mGraffitiView.setScale(scale, x, y);

                if (mIsScaling) {
                    ThreadUtil.getInstance().runOnMainThread(this, TIME_SPAN);
                }
            }
        };
        task.run();
    }


    private void showView(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }

        view.clearAnimation();
        view.startAnimation(mViewShowAnimation);
        view.setVisibility(View.VISIBLE);
        if (view == mSettingsPanel || mBtnHidePanel.isSelected()) {
            mGraffitiView.setAmplifierScale(-1);
        }
    }

    private void hideView(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            if (view == mSettingsPanel && mGraffitiView.getAmplifierScale() > 0) {
                mGraffitiView.setAmplifierScale(-1);
            }
            return;
        }
        view.clearAnimation();
        view.startAnimation(mViewHideAnimation);
        view.setVisibility(View.GONE);

        if (view == mSettingsPanel
                && !mBtnHidePanel.isSelected() && !mBtnMovePic.isSelected()) {
            // 当设置面板隐藏时才显示放大器
            mGraffitiView.setAmplifierScale(mGraffitiParams.mAmplifierScale);
        } else if ((view == mSettingsPanel && mGraffitiView.getAmplifierScale() > 0)) {
            mGraffitiView.setAmplifierScale(-1);
        }
    }

    private class GraffitiGestureListener extends TouchGestureDetector.OnTouchGestureListener {

        private Float mLastFocusX;
        private Float mLastFocusY;
        // 手势操作相关
        private float mToucheCentreXOnGraffiti, mToucheCentreYOnGraffiti, mTouchCentreX, mTouchCentreY;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mGraffitiView.setTrans(mGraffitiView.getTransX() - distanceX, mGraffitiView.getTransY() - distanceY);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mLastFocusX = null;
            mLastFocusY = null;
            return true;
        }

        // 手势缩放
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            // 屏幕上的焦点
            mTouchCentreX = detector.getFocusX();
            mTouchCentreY = detector.getFocusY();
            // 对应的图片上的焦点
            mToucheCentreXOnGraffiti = mGraffitiView.toX(mTouchCentreX);
            mToucheCentreYOnGraffiti = mGraffitiView.toY(mTouchCentreY);

            if (mLastFocusX != null && mLastFocusY != null) { // 焦点改变
                final float dx = mTouchCentreX - mLastFocusX;
                final float dy = mTouchCentreY - mLastFocusY;
                // 移动图片
                mGraffitiView.setTrans(mGraffitiView.getTransX() + dx, mGraffitiView.getTransY() + dy);
            }

            // 缩放图片
            float scale = mGraffitiView.getScale() * detector.getScaleFactor();
            if (scale > mMaxScale) {
                scale = mMaxScale;
            } else if (scale < mMinScale) {
                scale = mMinScale;
            }
            mGraffitiView.setScale(scale, mToucheCentreXOnGraffiti, mToucheCentreYOnGraffiti);

            mLastFocusX = mTouchCentreX;
            mLastFocusY = mTouchCentreY;

            return true;
        }
    }

    //发送图片子线程
    private class sendImageThread implements Runnable {

        private Bitmap bm;

        @Override
        public void run() {

            Gson gson = new Gson();

            // TODO: 2018/5/23  对图片编码
            String filePath = Environment.getExternalStorageDirectory() + "/DCIM" + "/Graffiti/draw.JPEG";//本地图片路径
            File file = new File(filePath);
            if (file.exists()){
                bm = BitmapFactory.decodeFile(filePath);
//                iv_showImage.setImageBitmap(bm);
            }
            String base64 = BitmapToStrByBase64(bm);
            Log.i("644444",base64);

            String jsonSend = gson.toJson(new imageMessage("image", base64));
            RequestParams params = new RequestParams(URL);
            params.addHeader("Content-type", "application/json");
            params.setCharset("UTF-8");
            params.setAsJsonContent(true);
            params.setBodyContent(jsonSend);

            x.http().post(params, callback);

        }
    }
    //对图片编码
    public String BitmapToStrByBase64(Bitmap bit){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 95, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //对图片解码
    public Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    private Callback.CommonCallback<String> callback = new Callback.CommonCallback<String>() {
        @Override
        public void onSuccess(String result) {

            String jsonBack = result;
            imageMessage imageMessage = new Gson().fromJson(jsonBack, cn.hzw.graffiti.bean.imageMessage.class);
            String backbase64 = imageMessage.imageBase64;
            // TODO: 2018/5/23  对图片解码
            Bitmap newBitmap = base64ToBitmap(backbase64);

            Message msg = new Message();
            msg.what = 1;
            msg.obj = newBitmap;
            mHandler.sendMessage(msg);

        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            Log.i("cnt", "onError");
            Message msg = new Message();
            msg.what = 2;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onCancelled(CancelledException cex) {
            Log.i("cnt", "onCancelled");
        }

        @Override
        public void onFinished() {
            Log.i("cnt", "onFinished");
        }
    };
}
