package jp.co.crowdworks.google_play_capture;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;

public class ApplicationTest extends InstrumentationTestCase {

    private UiDevice mDevice;
    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mContext = getInstrumentation().getTargetContext();
    }

    private File ensureDir(File d){
        if(!d.exists()){
            d.mkdir();
        }
        return d;
    }

    private void poke() throws RemoteException, InterruptedException {
        mDevice.wakeUp();
        Thread.sleep(4000);
    }

    public void test1PlayStoreScreenshot() throws RemoteException, InterruptedException {
        mDevice.wakeUp();
        mDevice.waitForIdle();

        // Start from the home screen
        mDevice.pressHome();
        mDevice.waitForIdle();


        Intent intent0 = new Intent(mContext, MainActivity.class);
        intent0.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent0);

        poke();

        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=jp.co.crowdworks.androidapp"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);


        poke();

        File screenshotDir = ensureDir(new File(mContext.getFilesDir(),"screenshots"));
        File fileA = new File(screenshotDir,"/"+Build.MODEL+"_a.png");
        File fileB = new File(screenshotDir,"/"+ Build.MODEL+"_b.png");

        mDevice.takeScreenshot(fileA);

        poke();

        mDevice.setOrientationLeft();

        poke();

        mDevice.takeScreenshot(fileB);

        poke();

        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
    }

    public void test2UploadScreenshotFile() {
        File screenshotDir = ensureDir(new File(mContext.getFilesDir(),"screenshots"));
        File fileA = new File(screenshotDir,"/"+Build.MODEL+"_a.png");
        File fileB = new File(screenshotDir,"/"+Build.MODEL+"_b.png");

        assertTrue(fileA.exists());
        assertTrue(fileB.exists());

        upload(fileA);
        upload(fileB);
    }

    private void upload(File file){
        System.setProperty("http.keeyAlive", "false");
        final OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder()
                .url("https://api-content.dropbox.com/1/files_put/auto/"+ Build.MODEL+"_"+file.getName())
                .addHeader("Authorization", "Bearer "+BuildConfig.DROPBOX_API_TOKEN)
                .addHeader("connection","close")
                .post(RequestBody.create(MediaType.parse("image/png"), file))
                .build();

        try {
            Response response = client.newCall(req).execute();
            assertTrue(response.isSuccessful());
        }
        catch(IOException e){
            Log.e("hoge", "error", e);
            assertTrue(false);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        mDevice.unfreezeRotation();
        super.tearDown();
    }
}