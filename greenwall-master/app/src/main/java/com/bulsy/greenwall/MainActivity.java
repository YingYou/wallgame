package com.bulsy.greenwall;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wandoujia.ads.sdk.Ads;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity {
    static final String LOG_ID = "Greenie";
    static final float EXPECTED_DENSITY = 315.0f;  // original target density of runtime device
    static final float EXPECTED_WIDTH = 720.0f;  // original target width of runtime device
    int TS_NORMAL; // normal text size
    int TS_BIG; // large text size
    float densityscalefactor;
    float sizescalefactor;
    DisplayMetrics dm;
    Screen entryScreen;
    PlayScreen playScreen;
    Screen currentScreen;
    FullScreenView mainView;
    Typeface gamefont;
    public SoundPool soundpool = null;
    Map<Sound, Integer> soundMap = null;
    ViewGroup container;
    ViewGroup container1;
    ViewGroup container2;

    private static final String APP_ID = "100046298";
    private static final String SECRET_KEY = "36bc9f8a88d3d413ca1f077ae939d5f9";
    private static final String BANNER = "1ea446d52f8b694fcf3a859eb1c3af40";
    private static final String INTERSTITIAL = "2d1ccfaaab9a09d4be8eec7d86ccca77";
    private static final String APP_WALL = "66caff24c98802b40dbb014bbf39f0be";

    /**
     * Initialize the activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {



        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            super.onCreate(savedInstanceState);
            /**
             * umeng
             * */

            MobclickAgent.setDebugMode(true);
            MobclickAgent.openActivityDurationTrack(false);
            MobclickAgent.setScenarioType(getApplicationContext(), MobclickAgent.EScenarioType. E_UM_NORMAL);


            dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            gamefont = Typeface.createFromAsset(getAssets(), "comics.ttf");
            densityscalefactor = (float)dm.densityDpi / EXPECTED_DENSITY;
            if (densityscalefactor > 1.5f)
                densityscalefactor = 1.5f;
            else if (densityscalefactor < 0.5f)
                densityscalefactor = 0.5f;
            sizescalefactor = (float)dm.widthPixels / EXPECTED_WIDTH;
            if (sizescalefactor > 2f)
                sizescalefactor = 2f;
            else if (sizescalefactor < 0.4f)
                sizescalefactor = 0.4f;
            TS_NORMAL = (int)(38 * sizescalefactor);
            TS_BIG = (int)(70 * sizescalefactor);

            setContentView(R.layout.activity_main);

            container = (ViewGroup) findViewById(R.id.banner_container);
            container1 = (ViewGroup) findViewById(R.id.test1);
            DisplayMetrics displayMetrics = MainActivity.this.getResources().getDisplayMetrics();
            container1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, displayMetrics.heightPixels -130));
            Log.d("yw","displayMetrics.heightPixels"+displayMetrics.heightPixels);
            container2 = (ViewGroup) findViewById(R.id.test2);

//            setContentView(mainView);

            new AsyncTask<Void,Void,Boolean>(){

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    // create screens



                }

                @Override
                protected void onProgressUpdate(Void... values) {
                    super.onProgressUpdate(values);


                }

                @Override
                protected Boolean doInBackground(Void... params) {


                    entryScreen = new EntryScreen(MainActivity.this);
                    playScreen = new PlayScreen(MainActivity.this);


                    setVolumeControlStream(AudioManager.STREAM_MUSIC);
                    soundpool = new SoundPool(15, AudioManager.STREAM_MUSIC, 0);
                    soundMap = new HashMap();
                    AssetFileDescriptor descriptor = null;
                    try {
                        descriptor = getAssets().openFd("combo2.mp3");

                    soundMap.put(Sound.COMBO, soundpool.load(descriptor, 1));
                    descriptor = getAssets().openFd("splat.mp3");
                    soundMap.put(Sound.SPLAT, soundpool.load(descriptor, 1));
                    descriptor = getAssets().openFd("wetsplat.mp3");
                    soundMap.put(Sound.WETSPLAT, soundpool.load(descriptor, 1));
                    descriptor = getAssets().openFd("wetsplat2.mp3");
                    soundMap.put(Sound.KSPLAT, soundpool.load(descriptor, 1));
                    descriptor = getAssets().openFd("whoosh.mp3");
                    soundMap.put(Sound.THROW, soundpool.load(descriptor, 1));
                    descriptor = getAssets().openFd("spaz.mp3");
                    soundMap.put(Sound.PASSLEVEL, soundpool.load(descriptor, 1));
                    descriptor = getAssets().openFd("aww.mp3");
                    soundMap.put(Sound.DEATH, soundpool.load(descriptor, 1));

                        try {
                            Ads.init(MainActivity.this, APP_ID, SECRET_KEY);
                            return true;
                        } catch (Exception e) {
                            Log.e("ads-sample", "error", e);
                            return false;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Boolean aVoid) {
                    super.onPostExecute(aVoid);
                    initWandJiaView(aVoid);
                    mainView = new FullScreenView(MainActivity.this);
                    container1.addView(mainView);
                    mainView.resume();

                }
            }.execute();




        } catch (Exception e) {
            // panic, crash, fine -- but let me know what happened.
            Log.d(LOG_ID, "onCreate", e);
        }


    }

    private void initWandJiaView(boolean success){
        if (success) {
            /**
             * pre load
             */
            Ads.preLoad(BANNER, Ads.AdFormat.banner);
//                    Ads.preLoad(INTERSTITIAL, Ads.AdFormat.interstitial);
//                    Ads.preLoad(APP_WALL, Ads.AdFormat.appwall);

            /**
             * add ad views
             */
            View bannerView = Ads.createBannerView(MainActivity.this, BANNER);
            container2.addView(bannerView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            Button btI = new Button(MainActivity.this);
            btI.setText("interstitial");
            btI.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Ads.showInterstitial(MainActivity.this, INTERSTITIAL);
                }
            });
//                    container.addView(btI);

            Button btW = new Button(MainActivity.this);
            btW.setText("app wall");
            btW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Ads.showAppWall(MainActivity.this, APP_WALL);
                }
            });
//                    container.addView(btW);
        } else {
            TextView errorMsg = new TextView(MainActivity.this);
            errorMsg.setText("init failed");
            container2.addView(errorMsg);
        }

    }

    BitmapFactory.Options sboptions = new BitmapFactory.Options();
    /**
     * load and scale bitmap according to the apps scale factors.
     *
     * @param fname
     * @return
     */
    Bitmap getScaledBitmap(String fname) throws IOException
    {
        sboptions.inScreenDensity = dm.densityDpi;
        sboptions.inTargetDensity =  dm.densityDpi;
        sboptions.inDensity = (int)(dm.densityDpi / sizescalefactor); // hack: want to load bitmap scaled for width, abusing density scaling options to do it
        InputStream inputStream = getAssets().open(fname);
        Bitmap btm = BitmapFactory.decodeStream(inputStream, null, sboptions);
        inputStream.close();
        return btm;

//        InputStream inputStream = getAssets().open(fname);
//        Bitmap btm = BitmapFactory.decodeStream(inputStream);
//        inputStream.close();
//        return Bitmap.createScaledBitmap(btm, (int)(btm.getWidth()*sizescalefactor), (int)(btm.getHeight()*sizescalefactor), false);
    }

    public void playSound(Sound s, float vol, float rate) {
        soundpool.play(soundMap.get(s), vol, vol, 0, 0, rate);
    }
    public void playSound(Sound s) {
        playSound(s, 0.9f, 1);
    }

    DisplayMetrics getDisplayMetrics() { return dm; }

    /**
     * Handle resuming of the game,
     */
    @Override
    protected void onResume() {
        super.onResume();
//        mainView.resume();
        MobclickAgent.onResume(this);
    }

    /**
     * Handle pausing of the game.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mainView.pause();
        MobclickAgent.onPause(this);
    }

    /**
     * Activity onKeyDown
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        finish();
        MobclickAgent.onKillProcess(this);
        System.exit(0);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Typeface getGameFont() {
        return gamefont;
    }

    // screen transitions

    /**
     * Start a new game.
     */
    public void startGame() {
        this.playScreen.initGame();
        currentScreen = this.playScreen;
    }

    /**
     * Leave game and return to title screen.
     */
    public void leaveGame() {
        currentScreen = this.entryScreen;
    }

    /**
     * completely exit the game.
     */
    public void exit() {
        finish();
        MobclickAgent.onKillProcess(this);
        System.exit(0);
    }

    /**
     * This inner class handles the main render loop, and delegates drawing and event handling to
     * the individual screens.
     */
    private class FullScreenView extends SurfaceView implements Runnable, View.OnTouchListener {
        private volatile boolean isRendering = false;
        Thread renderThread = null;
        SurfaceHolder holder;

        public FullScreenView(Context context) {
            super(context);
            holder = getHolder();
            currentScreen = entryScreen;
            setOnTouchListener(this);
        }

        public void resume() {
            isRendering = true;
            renderThread = new Thread(this);
            renderThread.start();
        }

        @Override
        public void run() {
            try {
                while(isRendering){
                    while(!holder.getSurface().isValid()) {
                        Log.d("yw","--------Thread:");
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) { /* we don't care */

                            Thread.currentThread().interrupt();
                        }
                    }

                    // update screen's context
                    currentScreen.update(this);

                    // draw screen
                    Canvas c = holder.lockCanvas();
                    currentScreen.draw(c, this);
                    holder.unlockCanvasAndPost(c);
//                    Log.d("yw","isRendering:"+isRendering);
                    postInvalidate();
                }
            } catch (Exception e) {
                // arguably overzealous to grab all exceptions here...but i want to know.
                Log.d(LOG_ID, "View", e);
                e.printStackTrace();
            }
        }

        public void pause() {
            isRendering = false;
            while(true) {
                try {
                    renderThread.join();
                    return;
                } catch (InterruptedException e) {
                    // retry
                }
            }
        }

        public boolean onTouch(View v, MotionEvent event) {

            Log.d("yw","---onTouch---");
            try {
                return currentScreen.onTouch(event);
            }
            catch (Exception e) {
                // arguably overzealous to grab all exceptions here...but i want to know.
                Log.d(LOG_ID, "onTouch", e);
            }
            return false;
        }
    }

//    private void initWandoujiaAsy(){
//
//        new AsyncTask<Void, Void, Boolean>() {
//            @Override
//            protected Boolean doInBackground(Void... params) {
//                try {
//                    Ads.init(MainActivity.this, APP_ID, SECRET_KEY);
//                    return true;
//                } catch (Exception e) {
//                    Log.e("ads-sample", "error", e);
//                    return false;
//                }
//            }
//
//            @Override
//            protected void onPostExecute(Boolean success) {
////                final ViewGroup container = (ViewGroup) findViewById(R.id.banner_container);
//
//
//            }
//        }.execute();
//    }
}
