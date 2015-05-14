package com.dygame.nonuiandroidservice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.ads.*;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;

/**
 ����Intent�ǻ�
 Remote?AIDL?
 */
public class MainActivity extends ActionBarActivity
{
    protected MyService mBoundService ;
    protected boolean mIsBound = false ;
    protected AdView adView;//��T�s�i
    protected InterstitialAd adInterstitial;//�����s�i
    MyReceiver pReceiver ;
    protected static String TAG = "" ;
    //
    protected ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            // Tell the user about this.
            Toast.makeText(MainActivity.this, "Service Disconnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((MyService.LocalBinder) service).getService() ;
            // Tell the user about this.
            Toast.makeText(MainActivity.this, "Service Connected", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //UncaughtException�B�z��,��{�ǵo��Uncaught���`���ɭ�,�������ӱ��޵{��,�ðO���o�e���~���i.
        MyCrashHandler pCrashHandler = MyCrashHandler.getInstance();
        pCrashHandler.init(getApplicationContext());
        TAG = MyCrashHandler.getTag() ;
        //�b���U�s������:
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.dygame.nonuiandroidservice.broadcast");//��BroadcastReceiver���waction�A�Ϥ��Ω󱵦��Paction���s��
        pReceiver = new MyReceiver();
        registerReceiver(pReceiver ,intentFilter);
        // �إ߾�T�s�i AdView�C
        adView = new AdView(this);
        adView.setAdUnitId(this.getString(R.string.string_my_ad_unit_id));
        adView.setAdSize(AdSize.BANNER);
        // ���] LinearLayout �w��o android:id="@+id/mainLayout" �ݩʡA
        // �d�� LinearLayout�C
        LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
        // �b�䤤�[�J adView�C
        layout.addView(adView);
        // �Ұʤ@��ШD�C
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        // �H�s�i�ШD���J adView�C
        adView.loadAd(adRequest);
        // �إߴ������s�i InterstitialAd
        adInterstitial = new InterstitialAd(this);
        adInterstitial.setAdUnitId(this.getString(R.string.string_my_in_unit_id));
        // �إߴ����s�i�ШD�C
        AdRequest adRequestII = new AdRequest.Builder().build();
        // �}�l���J�������s�i�C
        adInterstitial.loadAd(adRequestII);
        //
        doBindService() ;
        //�I���Ҧ�
        HideActivity() ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void doBindService()
    {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(MainActivity.this, MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        // Tell the user about this.
        Toast.makeText(MainActivity.this, "Bind Service", Toast.LENGTH_SHORT).show();
    }

    void doUnbindService()
    {
        if (mIsBound)
        {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            // Tell the user about this.
            Toast.makeText(MainActivity.this, "Unbinding Service", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause()
    {
        adView.pause();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        adView.resume();
    }

    @Override
    public void onDestroy()
    {
        adView.destroy();
        doUnbindService();
        //���P
        unregisterReceiver(pReceiver);
    }

    /**
     *  �������è��x�A��������UHome�䪺�ĪG
     */
    protected void HideActivity()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    /**
     * �o��s��
     */
    protected void SendBroadcastIntent()
    {
        //create a intent with an action
        String sActionString = "com.dygame.nonuiandroidservice.broadcast" ;
        Intent broadcastIntent = new Intent(sActionString) ;
        broadcastIntent.putExtra(TAG , "Burning Love! Poi!") ;
        sendBroadcast(broadcastIntent);
    }

    /**
     *  �����s��
     */
    public class MyReceiver extends BroadcastReceiver
    {
        protected boolean IsCommonTag = false ;//it is a Tag , Log it and debug
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String sAction = intent.getAction();
            if ((sAction.equals("android.intent.action.BOOT_COMPLETED")) || (sAction.equals("Hello poi")))
            {
                  Log.i(TAG,"You've got mail") ;
            }
            // analyze broadcast by packagename
            if (sAction.equals("com.dygame.myandroidservice.broadcast")) { IsCommonTag = true ; }
            if (sAction.equals("com.dygame.nonuiandroidservice.broadcast")) { IsCommonTag = true ; }
            //
            if (IsCommonTag == true)
            {
                Bundle bundle = intent.getExtras();
                if(bundle != null)
                {
                    String sMessage = bundle.getString(TAG);
                    Log.i(TAG, "broadcast receiver action:" + sAction + "=" + sMessage);
                }
            }
        }
    }

    /**
     * �o�񴡭����s�i
     */
    public void displayInterstitial()
    {
        //�o�Ӥ�k�|�b�ˬd isLoaded() �ýT�{�������s�i���J������A�I�s show() ����ܴ������s�i�C
        if (adInterstitial.isLoaded())
        {
            // ��ǳƦn��ܴ������s�i�� (���O�{���ҰʮɡB�v������e�θ��J�C�����d��)�A�s�� displayInterstitial()�C
            adInterstitial.show();
        }
    }

    /**
     *  �P�_����O�_�s�W���� (ConnectivityManager)
     *  �ݭn�v�� <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
     */
    protected void CheckConnectActivity()
    {
        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = CM.getActiveNetworkInfo();
        info.getTypeName();             // �ثe�H��ؤ覡�s�u [WIFI]
        info.getState();                // �ثe�s�u���A [CONNECTED]
        info.isAvailable();             // �ثe�����O�_�i�ϥ� [true]
        info.isConnected();             // �����O�_�w�s�� [true]
        info.isConnectedOrConnecting(); // �����O�_�w�s�� �� �s�u�� [true]
        info.isFailover();              // �����ثe�O�_�����D [false]
        info.isRoaming();               // �����ثe�O�_�b���C�� [false]
    }

    /**
     *  �Ϊk:?
     *  iLaunchRtn = launchGameZone("com.dygame.gamezone2","com.dygame.waverider");
     */
    private int LaunchGameZone(Context c)
    {
/*
        int iRtn = 0;
        if (checkMeRunning())
        {
            LogManager.ErrorLog(getClass(), this.GAME_ZONE_PACKAGE_NAME + "is running, ingore");
            return 0;
        }
        Log.i("DYService", "restart GameZone, sPackageName = " + this.GAME_ZONE_PACKAGE_NAME);

        PackageManager pkgMgt = c.getPackageManager();

        boolean bFind = false;
        Intent it = new Intent("android.intent.action.MAIN");
        it.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> ra = pkgMgt.queryIntentActivities(it, 0);
        String sCurPackageName = "";
        bFind = false;
        String sClassName = "com.dygame.gamezone2.Logo";
        for (int j = 0; j < ra.size(); j++)
        {
            ActivityInfo ai = ((ResolveInfo)ra.get(j)).activityInfo;
            sCurPackageName = ai.applicationInfo.packageName;
            if (this.GAME_ZONE_PACKAGE_NAME.equalsIgnoreCase(sCurPackageName))
            {
                sClassName = ai.name;
                bFind = true;
                break;
            }
        }
        if (!bFind) {
            Log.e("DYService", "not find PackageName:" + this.GAME_ZONE_PACKAGE_NAME);
        }
        Log.i("DYService", "Try launch GameZone, sPackageName = " + this.GAME_ZONE_PACKAGE_NAME + " , Class Name = " + sClassName);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addFlags(268435456);
        intent.setComponent(new ComponentName(this.GAME_ZONE_PACKAGE_NAME, sClassName));
        c.startActivity(intent);
        return iRtn;
*/   return 0 ;
     }

    /*
    /**
* �����e�������
* /
    public String getLocalNumber()
    {
        TelephonyManager tManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        String number = tManager.getLine1Number();
        //��������IMSI�X/�����e�B���
        System.out.println("-----"+telManager.getSubscriberId()); //2.-----460007423945575
        System.out.println("-----"+telManager.getSimSerialNumber()); //1.-----89860089281174245575
        System.out.println("-----"+telManager.getSimOperator());
        System.out.println("-----"+telManager.getSimCountryIso());
        System.out.println("-----"+telManager.getSimOperatorName());
        System.out.println("-----"+telManager.getSimState());
        //SubscriberId == 46001 // �����p�q
        //SubscriberId == 46003 // ����q�H
        //
        return number;
    }
    /**
     *�ˬd�O�_�������s��
     * /
    public boolean checkInternet()
    {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected())
        {
            // ��s��Internet
            return true;
        }
        else
        {
            // ����s����
            return false;
        }
    }
    /**
     * �P�_��e�����s�����A
     * /
    public static boolean isNetworkConnected(Context context)
    {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getApplicationContext().getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null)
        {
            return networkInfo.isConnectedOrConnecting();
        }
        return false;
    }
    /**
     *get�ШD�����ƾ�
     * /
    public static String GetDate(String url)
    {
        HttpGet get = new HttpGet(url);
        HttpClient client = new DefaultHttpClient();
        try
        {
            HttpResponse response = client.execute(get);//
            return EntityUtils.toString(response.getEntity());
        }
        catch (Exception e)
        {
            return null;
        }
    }
    /**
     *���apk�]��ñ�W�H��
     * /
    private String getSign(Context context)
    {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
        Iterator<PackageInfo> iter = apps.iterator();
        while(iter.hasNext())
        {
            PackageInfo packageinfo = iter.next();
            String packageName = packageinfo.packageName;

            return packageinfo.signatures[0].toCharsString();
            if (packageName.equals(instance.getPackageName()))
            {
                MediaApplication.logD(DownloadApk.class, packageinfo.signatures[0].toCharsString());
                return packageinfo.signatures[0].toCharsString();
            }
        }
        return null;
    }
    /**
     *�եΨt���s����
     * /
    Intent intent= new Intent();
    intent.setAction("android.intent.action.VIEW");
    Uri content_url = Uri.parse(exitUrl);
    intent.setData(content_url);
    startActivity(intent);
    /**
     *���o�u�H�q�ܸ��X  ���e
     * /
    String number = actv_enter_number.getText().toString();
    String body = et_enter_msg_content.getText().toString();

    SmsManager smsManager = SmsManager.getDefault();
    ArrayList<String> parts = smsManager.divideMessage(body);

    for(String part : parts)
    {
        smsManager.sendTextMessage(number, null, part, null, null);

        Uri url = Sms.Sent.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put("address", number);
        values.put("body", part);
        getContentResolver().insert(url, values);
    }
/**
 * ������
 * /
    <activity ...
    android:screenOrientation="landscape"  <!-- ��� -->
/**
 * onkey����
 * /
    c:\adb shell shell@android:/ $ monkey -p com.ooxxzzuu.yourapp -v 2000
    /**
     * ��������v�A�O������I���`�G
     * /
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
/**
 * �o���e�t��sdk����/�n���e��������
 */
/**
 private int getSdkVersion()
 {
 return android.os.Build.VERSION.SDK_INT;
 }
 //
 PackageManager pm = getPackageManager();
 PackageInfo pkinfo=pm.getPackageInfo(getPackageName(),0);
 return pkinfo.versionName;
 /**
 *�P�_SD�d�O�_�i��
 * /
    if (Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED))
    /**
     *���SD/ROM�i�ΪŶ�
     * /
    private String getSize()
    {
        //�o��ROM�Ѿl�Ŷ�
        File path = Environment.getDataDirectory();
        //�o��SD�d�Ѿl�Ŷ�
        // Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        long availsize = availableBlocks * blockSize;
        String size = Formatter.formatFileSize(this, availsize);
        return size;
    }
    /**
     * ���������Ѿl�i��ROM
     * /
    public static long getAvailableMem(Context context)
    {
        ActivityManager  am  = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo outInfo = new MemoryInfo();
        am.getMemoryInfo(outInfo);
        long availMem = outInfo.availMem;
        return availMem;
    }
    /**
     *�q�����W�U�����
     * /
    URL url = new URL(path);
    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    conn.setRequestMethod("GET"); //
    conn.setConnectTimeout(5000); //�W�ɮɶ�
    int code = conn.getResponseCode(); // ���A�X
    if (code == 200)
    {
        InputStream is = conn.getInputStream();
    }
    /**
     * �w�ˤU��������APK
     * /
    private void installAPK(File savedFile)
    {
        //�եΨt�Ϊ��w�ˤ�k
        Intent intent=new Intent();
        intent.setAction(intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(savedFile), "application/vnd.android.package-archive");
        startActivity(intent);
        finish();
    }
    /**
     * Sharedpreference ���n�]�m
     * /
    private SharedPreferences sp;
    sp = getSharedPreferences("config", MODE_PRIVATE);
    // �V���n�]�m���K�[�ƾ�
    Editor editor = sp.edit();
    editor.putBoolean("update", false);
    editor.commit();
    // �q���n�]�m�������ƾ�
    sp.getBoolean ("update", false);
    /**
     *���e���Ѫ� ����pô�H�m�W�M�q�� ��^ infos�ﹳ
     * /
    public static List<ContactInfo> getContactInfo(Context context)
    {
        List<ContactInfo> infos = new ArrayList<ContactInfo>();
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri datauri = Uri.parse("content://com.android.contacts/data");
        Cursor cursor = context.getContentResolver().query(uri, null, null,null, null);
        while (cursor.moveToNext())
        {
            String id = cursor.getString((cursor.getColumnIndex("contact_id")));
            // �ھڤW����ID�d���pô�H�� �H��
            ContactInfo info = new ContactInfo();
            Cursor datacursor = context.getContentResolver().query(datauri,null, "raw_contact_id=?", new String[] { id }, null);
            while (datacursor.moveToNext())
            {
                String data1 = datacursor.getString(cursor.getColumnIndex("data1"));
                String mimetype = datacursor.getString(cursor.getColumnIndex("mimetype"));
                if ("vnd.android.cursor.item/phone_v2".equals(mimetype))
                {
                    info.setNumber(data1);
                }
                if ("vnd.android.cursor.item/name".equals(mimetype))
                {
                    info.setName(data1);
                }
            }
            datacursor.close();
            infos.add(info);
        }
        cursor.close();
        return infos;
    }
/**
 *���B����
 * /
    new AsyncTask<Void, Void, Void>()
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            // ���B���ȹB��ɰ���
            return null;
        }
        @Override
        protected void onPreExecute()
        {
            //���B���ȹB��e���� �i�H��m�i�ױ� progressbar��
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Void result)
        {
            //���B���ȹB�浲�������
            super.onPostExecute(result);
        }
    }.execute();
    /**
     *�i�ױ�
     * /
    ProgressDialog pd;
    pd = new ProgressDialog(this);
    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    pd.setTitle("���D");
    pd.setMessage("�i�ױ�����");
    pd.show();
    /**
     *�ͦ�6���H����
     * /
    int numcode = (int) ((Math.random() * 9 + 1) * 100000);
    String smstext = "�A�����ͦ���6��w�����ҽX���G" + numcode;
    /**
     *����̤j���s����k
     * /
    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    am.getMemoryClass();
    */
}
