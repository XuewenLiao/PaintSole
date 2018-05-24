package cn.hzw.graffitidemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import cn.hzw.graffiti.GraffitiActivity;

public class MainActivity extends Activity {

    public static final int REQ_CODE_SELECT_IMAGE = 100;
    public static final int REQ_CODE_GRAFFITI = 101;
    private TextView mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, GraffitiActivity.class);
        intent.setAction("cn.hzw.graffiti.act");
        startActivity(intent);

//        findViewById(R.id.btn_select_image).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, GraffitiActivity.class);
//        intent.setAction("cn.hzw.graffiti.act");
//                startActivity(intent);
//                System.out.println(1);
//            }
//        });


//        try {
//            Intent intent=new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            intent.setClassName("cn.hzw.graffitidemo","cn.hzw.graffiti.GraffitiActivity");
//            ComponentName cn = new ComponentName("cn.hzw.graffitidemo","cn.hzw.graffiti.GraffitiActivity");
//            intent.setComponent(cn);
////            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//        }catch (Exception e){
//            Log.i("异常","异常");
//        }

//        startActivity(MainActivity.this,GraffitiActivity.class);
//
//        findViewById(R.id.btn_select_image).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ImageSelectorActivity.startActivityForResult(REQ_CODE_SELECT_IMAGE, MainActivity.this, null, false);
//            }
//        });
//        mPath = (TextView) findViewById(R.id.img_path);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);


//        if (requestCode == REQ_CODE_SELECT_IMAGE) {
//            if (data == null) {
//                return;
//            }
//            ArrayList<String> list = data.getStringArrayListExtra(ImageSelectorActivity.KEY_PATH_LIST);
//            if (list != null && list.size() > 0) {
//                LogUtil.d("Graffiti", list.get(0));
//
//                // 涂鸦参数
//                GraffitiParams params = new GraffitiParams();
//                // 图片路径
//                params.mImagePath = list.get(0);
//                // 初始画笔大小
//                params.mPaintSize = 20;
//                // 启动涂鸦页面
//                GraffitiActivity.startActivityForResult(MainActivity.this, params, REQ_CODE_GRAFFITI);
//            }
//        } else if (requestCode == REQ_CODE_GRAFFITI) {
//            if (data == null) {
//                return;
//            }
//            if (resultCode == GraffitiActivity.RESULT_OK) {
//                String path = data.getStringExtra(GraffitiActivity.KEY_IMAGE_PATH);
//                if (TextUtils.isEmpty(path)) {
//                    return;
//                }
//                ImageLoader.getInstance(this).display(findViewById(R.id.img), path);
//                mPath.setText(path);
//            } else if (resultCode == GraffitiActivity.RESULT_ERROR) {
//                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}
