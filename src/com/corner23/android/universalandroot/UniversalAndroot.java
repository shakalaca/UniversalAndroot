package com.corner23.android.universalandroot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class UniversalAndroot extends Activity {

	private static final String FNAME_EXPLOIT = "getroot";
	private static final String FNAME_SU_BIN = "su";
	private static final String FNAME_SU_APK = "Superuser.apk";
	private static final String FNAME_INSTALL = "install_kit.sh";
	private static final String FNAME_REMOVE = "remove_kit.sh";
	private static final String FNAME_REMOUNT_SYS_RW = "remount_sys_rw.sh";
	private static final String FNAME_REMOUNT_SYS_RO = "remount_sys_ro.sh";
	private static final String FNAME_REMOUNT_DATA_RO = "remount_data.sh";
	private static final String FNAME_FSRW_MODULE_TATTOO_V1 = "tattoo_hack_gf922713.ko";
	private static final String FNAME_FSRW_MODULE_TATTOO_V2 = "tattoo_hack_g6561203.ko";
	
	private static final String SU_EXEC_PATH = "/system/bin/su";
	private static final String MOUNT_EXEC_PATH = "/system/bin/mount";
	private static final String ROOT_SHELL_PATH = "/data/local/tmp/rootshell";

	private static final int FILE_GEN_FAILED = 0;
	private static final int FILE_GEN_SUCCESS = 1;

	private static final String TAG = "UniversalAndroot";
	private static final int BUFFER_SIZE = 1024;
	
	private int su_bin_resid = 0;
	private int su_apk_resid = 0;
	
	private TextView tv_msg = null;
	private TextView tv_gen_exploit_msg = null;
	private TextView tv_su_bin_msg = null;
	private TextView tv_su_apk_msg = null;
	private TextView tv_script_msg = null;

	private Spinner spinner = null;
	
	private CheckBox chk_box_save_log = null;
	
	private Button btn_root = null;
	private Button btn_unroot = null;
	
	private WifiManager wifiManager;
	private boolean bDisableWifi = false;
	private boolean bSaveLog = false;
	
	private static final int MAX_LOG_SIZE = 500000;
	
	static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT
	static {
		int sdkInt;
		try {
			sdkInt = Integer.parseInt(Build.VERSION.SDK);
		} catch (NumberFormatException nfe) {
			// Just to be safe
			sdkInt = 10000;
		}
		SDK_INT = sdkInt;
	}

    // Exception
	private String exceptionToString(Exception e) {
		String log = null;

		log = e.getMessage() + "\r\n";
		for (StackTraceElement s : e.getStackTrace()) {
			log += s.toString() + "\r\n";
		}
		log += "\r\n";

		return log;
	}
	
	public void logException(Exception exp) {
		String msg = exceptionToString(exp);
		sendLogToSDCARD(msg);
	}

	private boolean sendLogToSDCARD(String msg) {
		if (!bSaveLog) {
			return false;
		}
		
		Log.d(TAG, msg);
		try {
			File root = Environment.getExternalStorageDirectory();
			if (root.canWrite()){
				boolean appendfile = true;
				File logfile = new File(root, "UniversalAndroot.log");
				if (logfile.length() > MAX_LOG_SIZE) {
					appendfile = false;
				}
				FileWriter logwriter = new FileWriter(logfile, appendfile);
				BufferedWriter out = new BufferedWriter(logwriter);
				out.write(msg + "\r\n");
				out.close();
			}
		} catch (IOException e) {
			return false;
		}

		return true;
	}
	  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
 
    	wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		tv_msg = (TextView) findViewById(R.id.tv_tip_msg);
		tv_gen_exploit_msg = (TextView) findViewById(R.id.tv_gen_exploit_msg);
		tv_su_bin_msg = (TextView) findViewById(R.id.tv_gen_su_bin_msg);
		tv_su_apk_msg = (TextView) findViewById(R.id.tv_gen_su_apk_msg);
		tv_script_msg =	(TextView) findViewById(R.id.tv_gen_script_msg); 
		
		chk_box_save_log = (CheckBox) findViewById(R.id.chk_box_save_log);
		
		btn_root = (Button) findViewById(R.id.btn_root);
        btn_root.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				File su = new File(SU_EXEC_PATH);
				if (su.exists()) {
					new AlertDialog.Builder(UniversalAndroot.this)
					.setTitle(R.string.str_root_already_rooted)
					.setMessage(R.string.str_root_already_rooted_again)
					.setNegativeButton(R.string.str_root_already_rooted_again_no, null)
					.setPositiveButton(R.string.str_root_already_rooted_again_yes, mOnRootMePleaseDialogClickListener)
					.show();					

					return;
				}
				
				go4root();
			}
        });        

		btn_unroot = (Button) findViewById(R.id.btn_unroot);
		btn_unroot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				File su = new File(SU_EXEC_PATH);
				if (!su.exists()) {
					if (tv_msg != null) {
						tv_msg.setText(R.string.str_unroot_not_rooted);
						return;
					}
				}

				go4unroot();
			}
        });
		
		spinner = (Spinner) findViewById(R.id.spinner_su);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.su_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

	        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	        	setSuResID(pos);
	        }

	        public void onNothingSelected(AdapterView<?> parent) {
	        	// Do nothing.
	        }
	    });
		
	    setDefaultSpinnerItem();
		setTitleVersion();
    }
    
    private void setDefaultSpinnerItem() {
		if (SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			spinner.setSelection(0);
		} else if (SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
			spinner.setSelection(1);
		} else {
			spinner.setSelection(2);
		}				
    }
    
	private void setSuResID(int pos) {
    	switch (pos) {
    	case 0:
			sendLogToSDCARD("User selected: Eclair");
			Log.d(TAG, "> ECLAIR");
			su_bin_resid = R.raw.superuser_su_ef;
			su_apk_resid = R.raw.superuser_apk_ef;
			break;
    	case 1:
			sendLogToSDCARD("User selected: Cupcake");
			Log.d(TAG, "> CUPCAKE");
			su_bin_resid = R.raw.superuser_su_cd;
			su_apk_resid = R.raw.superuser_apk_cd;
			break;
		default:
			sendLogToSDCARD("User selected: Not install");
			Log.d(TAG, "NO install");
			su_bin_resid = R.raw.su;
			su_apk_resid = 0;
			break;
    	}
	}
	
    private void setTitleVersion() {
        try {
        	PackageManager manager = getPackageManager();
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			
			this.setTitle(getResources().getString(R.string.app_name) + " - v" + info.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			logException(e);
		}    
    }
    
    private void go4unroot() {
    	bSaveLog = chk_box_save_log.isChecked();
		sendLogToSDCARD("Version: " + this.getTitle().toString());
		sendLogToSDCARD("Detected OS version:" + SDK_INT);		
		sendLogToSDCARD("Go for unroot !");
		if (tv_msg != null) {
			tv_msg.setText(R.string.str_unrooting);
		}
		disableButtons();
        prepareExploidTask pet = new prepareExploidTask();
        pet.execute(false);
    }

    private void go4root() {
    	bSaveLog = chk_box_save_log.isChecked();
		sendLogToSDCARD("Version: " + this.getTitle().toString());
		sendLogToSDCARD("Detected OS version:" + SDK_INT);		
		sendLogToSDCARD("Go for root !");
		if (tv_msg != null) {
			tv_msg.setText(R.string.str_rooting);
		}
		setSuResID(spinner.getSelectedItemPosition());
		disableButtons();
        prepareExploidTask pet = new prepareExploidTask();
        pet.execute(true);
    }
    
	private DialogInterface.OnClickListener mOnRootMePleaseDialogClickListener = new DialogInterface.OnClickListener(){

		public void onClick(DialogInterface dialog, int which) {
			go4root();
		}
	};
	
	private void cleanGenMsgs() {		
		if (tv_gen_exploit_msg != null) tv_gen_exploit_msg.setText(null);
		if (tv_su_bin_msg != null) tv_su_bin_msg.setText(null);
		if (tv_su_apk_msg != null) tv_su_apk_msg.setText(null);
		if (tv_script_msg != null) tv_script_msg.setText(null);
	}
    
    private void disableButtons() {
    	if (btn_root != null) {
			btn_root.setClickable(false);
			btn_root.setEnabled(false);
    	}
    	if (btn_unroot != null) {
			btn_unroot.setClickable(false);
			btn_unroot.setEnabled(false);
    	}
    	if (spinner != null) {
    		spinner.setClickable(false);
    		spinner.setEnabled(false);
    	}
    }

    private void enableButtons() {
    	if (btn_root != null) {
			btn_root.setClickable(true);
			btn_root.setEnabled(true);
    	}
    	if (btn_unroot != null) {
			btn_unroot.setClickable(true);
			btn_unroot.setEnabled(true);
    	}
    	if (spinner != null) {
    		spinner.setClickable(true);
    		spinner.setEnabled(true);
    	}
    }
    
    private void removeExploit() {
		File Exploit = new File(getFilesDir(), FNAME_EXPLOIT);
		if (Exploit.exists()) {
			Boolean bDeleted = Exploit.delete();
			if (bDeleted) {
				sendLogToSDCARD("Exploit delete success");
			} else {
				sendLogToSDCARD("Exploit delete failed");
			}
		}
    }
    
    private void checkProcErrorMsg(InputStream is) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is), BUFFER_SIZE);

			String line;
			while ((line = reader.readLine()) != null) {
				Log.d(TAG, line);
				sendLogToSDCARD(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			logException(e);
		} finally {
			if (reader != null) {
				try { 
					reader.close(); 
				} catch (IOException e) {
				}
			}
		}
	}
    
    private String getSystemMountPoint(InputStream is) {
    	return getMountPoint(is, "/system");
    }
    
    private String getDataMountPoint(InputStream is) {
    	return getMountPoint(is, "/data");
    }
    
    private String getMountPoint(InputStream is, String mp_name) {
		BufferedReader reader = null;
		String mp = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is), BUFFER_SIZE);

			String line;
			sendLogToSDCARD("Trying to get mount point:" + mp_name);
			while ((line = reader.readLine()) != null) {
				if (line.contains(mp_name)) {
					sendLogToSDCARD(line);
					mp = line;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logException(e);
		} finally {
			if (reader != null) {
				try { 
					reader.close(); 
				} catch (IOException e) {
					logException(e);
				}
			}
		}
		
		return mp;
	}
    
    private String getMsgStringByResult(Integer msgId, Integer result) {
    	String msg_ret = null;
		String msg_str = getResources().getString(msgId);
		String format_str = getResources().getString(R.string.str_rooting_gen_msg);
		String status_str = null;
		
		if (result == FILE_GEN_FAILED) {
			status_str = getResources().getString(R.string.str_rooting_gen_failed);
		} else if (result == FILE_GEN_SUCCESS) {
			status_str = getResources().getString(R.string.str_rooting_gen_success);
		}
		
		msg_ret = String.format(format_str, msg_str, status_str);

		return msg_ret;
    }
    
	private boolean getRawResource(String o_fname, int i_resid) {
		if (i_resid == 0) {
			return false;
		}
		
    	try {
			FileOutputStream fos = openFileOutput(o_fname, Context.MODE_PRIVATE);		    	
	    	InputStream ins = getResources().openRawResource(i_resid);		    	
	        byte[] buffer = new byte[BUFFER_SIZE];
	        int len1 = 0;
	        
	        while ( (len1 = ins.read(buffer)) > 0 ) {
	            fos.write(buffer, 0, len1);
	        }
	        fos.close();
	        ins.close();
	        
	        return true;
		} catch (IOException e) {
			e.printStackTrace();
			logException(e);
		}
		
		return false;
	}
	
	private class prepareExploidTask extends AsyncTask<Boolean, Void, Integer> {
		
		boolean bWantRoot = false;
    	
		@Override
		protected Integer doInBackground(Boolean... params) {
			bWantRoot = params[0];

    		boolean bSuccess = getRawResource(FNAME_EXPLOIT, R.raw.exploid);
    		bSuccess &= getRawResource(FNAME_FSRW_MODULE_TATTOO_V1, R.raw.tattoo_hack_gf922713);
    		bSuccess &= getRawResource(FNAME_FSRW_MODULE_TATTOO_V2, R.raw.tattoo_hack_g6561203);
	    	
			return bSuccess ? FILE_GEN_SUCCESS : FILE_GEN_FAILED;
	    }

		@Override
		protected void onPostExecute(Integer result) {
			sendLogToSDCARD("Preparing Exploit ... :" + result);
			if (tv_gen_exploit_msg != null) {
				tv_gen_exploit_msg.setText(getMsgStringByResult(R.string.str_rooting_gen_exploit, result));
			}
			
			if (bWantRoot) {
				prepareSuBinTask psbt = new prepareSuBinTask();
				psbt.execute();
			} else {
				prepareUnrootScriptTask pust = new prepareUnrootScriptTask();
				pust.execute();
			}
		}
    }

    private class prepareSuBinTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			
    		if (getRawResource(FNAME_SU_BIN, su_bin_resid)) {
    			return FILE_GEN_SUCCESS;
    		}
	    	
			return FILE_GEN_FAILED;
	    }

		@Override
		protected void onPostExecute(Integer result) {
			sendLogToSDCARD("Preparing Su binary ... :" + result);
			if (tv_su_bin_msg != null) {
				tv_su_bin_msg.setText(getMsgStringByResult(R.string.str_rooting_gen_su_bin, result));
			}
			
			prepareSuApkTask psat = new prepareSuApkTask();
			psat.execute();
		}
    }    

    private class prepareSuApkTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
	
			if (su_apk_resid == 0) {
		    	File su_apk = new File(getFilesDir(), FNAME_SU_APK);
		    	su_apk.delete();
	    	} else {
	    		if (getRawResource(FNAME_SU_APK, su_apk_resid)) {
	    			return FILE_GEN_SUCCESS;
	    		}
	    	}
	    	
	    	return FILE_GEN_FAILED;
	    }

		@Override
		protected void onPostExecute(Integer result) {
			sendLogToSDCARD("Preparing Superuser apk ... :" + result + ", resid:" + su_apk_resid);
			if (tv_su_apk_msg != null && su_apk_resid != 0) {
				tv_su_apk_msg.setText(getMsgStringByResult(R.string.str_rooting_gen_su_apk, result));
			}
			
			prepareRootScriptTask prst = new prepareRootScriptTask();
			prst.execute();
		}
    }    
    
    private class prepareRootScriptTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			
			if (getRawResource(FNAME_INSTALL, R.raw.inst_kit)) {
				return FILE_GEN_SUCCESS;
			}
	    	
			return FILE_GEN_FAILED;
	    }

		@Override
		protected void onPostExecute(Integer result) {
			sendLogToSDCARD("Preparing root toolkit script ... :" + result);
			if (tv_script_msg != null) {
				tv_script_msg.setText(getMsgStringByResult(R.string.str_rooting_gen_script, result));
			}
			
			rootTask rt = new rootTask();
			rt.execute(true);
		}
    }    
    
    private class prepareUnrootScriptTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			
			if (getRawResource(FNAME_REMOVE, R.raw.uninst_kit)) {
				return FILE_GEN_SUCCESS;
			}
	    	
			return FILE_GEN_FAILED;
	    }

		@Override
		protected void onPostExecute(Integer result) {
			sendLogToSDCARD("Preparing unroot toolkit script ... :" + result);
			if (tv_script_msg != null) {
				tv_script_msg.setText(getMsgStringByResult(R.string.str_unrooting_gen_script, result));
			}
			
			rootTask rt = new rootTask();
			rt.execute(false);
		}
    }    
    
    private void genScriptFile(String fname, String msg) throws IOException {
		FileOutputStream fos = openFileOutput(fname, Context.MODE_PRIVATE);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		osw.write(msg + "\n");
		osw.flush();
		osw.close();
		sendLogToSDCARD(msg);
    }
    
    private class rootTask extends AsyncTask<Boolean, Void, Boolean> {

    	private boolean bDoInstProcess = false;
    	
		@Override
		protected Boolean doInBackground(Boolean... params) {
			bDoInstProcess = params[0];
			String s_mount_point_data = null;
			String s_fs_type_data = null;
			String s_mount_arg_data = null;
			String s_mount_point_sys = null;
			String s_fs_type_sys = null;
			try {
				Process proc = Runtime.getRuntime().exec(MOUNT_EXEC_PATH);
				if (proc != null) {
					String mp_line = getDataMountPoint(proc.getInputStream());
					String[] tmp = mp_line.split("\\s+");
					s_mount_point_data = tmp[0];
					s_fs_type_data = tmp[2];
					s_mount_arg_data = tmp[3];
				}
					
				proc = Runtime.getRuntime().exec(MOUNT_EXEC_PATH);
				if (proc != null) {
					String mp_line = getSystemMountPoint(proc.getInputStream());
					String[] tmp = mp_line.split("\\s+");
					s_mount_point_sys = tmp[0];
					s_fs_type_sys = tmp[2];
				}
			
			if (s_mount_point_data == null) {
				s_mount_point_data = "/dev/block/mtdblock5";
			}
			
			if (s_mount_point_sys == null) {
				s_mount_point_sys = "/dev/block/mtdblock3";
			}
			
				genScriptFile(FNAME_REMOUNT_SYS_RW, 
						"mount -o remount,rw -t " + s_fs_type_sys + " " + s_mount_point_sys + " /system");

				genScriptFile(FNAME_REMOUNT_SYS_RO, 
						"mount -o remount,ro -t " + s_fs_type_sys + " " + s_mount_point_sys + " /system");

				genScriptFile(FNAME_REMOUNT_DATA_RO, 
						"mount -o remount," + s_mount_arg_data + " -t " + s_fs_type_data + " " + s_mount_point_data + " /data");
						
			} catch (IOException e) {
				e.printStackTrace();
				logException(e);
			}
			
			File Exploit = new File(getFilesDir(), FNAME_EXPLOIT);
			if (Exploit.exists()) {
				try {
					sendLogToSDCARD("Preparing to execute exploit, do chmod");
					String cmd = "chmod 770 " + Exploit.getAbsolutePath();
					Process proc = Runtime.getRuntime().exec(cmd);
					if (proc != null) {
						checkProcErrorMsg(proc.getErrorStream());
					}
					
					sendLogToSDCARD("Executing exploit..");
					cmd = Exploit.getAbsolutePath() + " " + s_mount_point_data + " " + s_fs_type_data;
					sendLogToSDCARD("cmd: " + cmd);
					proc = Runtime.getRuntime().exec(cmd);
					if (proc != null) {
						checkProcErrorMsg(proc.getInputStream());
						checkProcErrorMsg(proc.getErrorStream());
					}
					Thread.sleep(1000);
					return true;
				} catch (IOException e) {
					e.printStackTrace();
					logException(e);
				} catch (InterruptedException e) {
					e.printStackTrace();
					logException(e);
				}
			}
			return false;
		}    	

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
		        int state = wifiManager.getWifiState();
		        if (state == WifiManager.WIFI_STATE_ENABLED) {
					sendLogToSDCARD("Wifi enabled ...");
			        wifiManager.setWifiEnabled(false);
			        bDisableWifi = false;
		        } else if (state == WifiManager.WIFI_STATE_DISABLED) {
					sendLogToSDCARD("Wifi disabled ...");
			        wifiManager.setWifiEnabled(true);
			        bDisableWifi = true;
		        } else {
					sendLogToSDCARD("Wifi in unknown state ...");		        	
		        }
			}

			sendLogToSDCARD("After wifi, do copy files");		        	
			if (bDoInstProcess) {
				if (tv_msg != null) {
					tv_msg.setText(R.string.str_root_installing_kit);
				}
	
				installToolKitTask itkt = new installToolKitTask();
				itkt.execute();
			} else {
				if (tv_msg != null) {
					tv_msg.setText(R.string.str_root_uninstalling_kit);
				}
	
				uninstallToolKitTask utkt = new uninstallToolKitTask();
				utkt.execute();
			}
		}
    }    

    private class installToolKitTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			File RootShell = new File(ROOT_SHELL_PATH);
			File KitInstaller = new File(getFilesDir(), FNAME_INSTALL);
			if (!RootShell.exists()) {
				sendLogToSDCARD("Root shell is missing ..");
				Log.d(TAG, ROOT_SHELL_PATH + " missing..");
				return false;
			}
			
			if (!KitInstaller.exists()) {
				Log.d(TAG, FNAME_INSTALL + " missing..");
				return false;
			}
			
			try {
				String cmd = "chmod 777 " + KitInstaller.getAbsolutePath();
				Process proc = Runtime.getRuntime().exec(cmd);
				if (proc != null) {
					checkProcErrorMsg(proc.getErrorStream());
				}
				
				cmd = RootShell.getAbsolutePath() + " " + KitInstaller.getAbsolutePath();
				proc = Runtime.getRuntime().exec(cmd);
				if (proc != null) {
					checkProcErrorMsg(proc.getInputStream());
					checkProcErrorMsg(proc.getErrorStream());
				}
				
				File su = new File(SU_EXEC_PATH);
				if (!su.exists()) {
					return false;
				}
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				logException(e);
			}

			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			genSysInfoReport();
			sendLogToSDCARD("Install rootkit: " + result);
			
			if (result) {
				if (tv_msg != null) {
					tv_msg.setText(R.string.str_root_success);
				}
			} else {
				if (tv_msg != null) {
					tv_msg.setText(R.string.str_root_failed);
				}
			}
			
			if (bDisableWifi) {
				wifiManager.setWifiEnabled(false);
			} else {
		        wifiManager.setWifiEnabled(true);
			}
			
			removeExploit();
			enableButtons();
			cleanGenMsgs();
		}
    }    

    private class uninstallToolKitTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			File RootShell = new File(ROOT_SHELL_PATH);
			File KitUninstaller = new File(getFilesDir(), FNAME_REMOVE);
			if (!RootShell.exists()) {
				sendLogToSDCARD("Root shell is missing ..");
				Log.d(TAG, ROOT_SHELL_PATH + " missing..");
				return false;
			}
			
			if (!KitUninstaller.exists()) {
				Log.d(TAG, FNAME_REMOVE + " missing..");
				return false;
			}
			
			try {
				String cmd = "chmod 777 " + KitUninstaller.getAbsolutePath();
				Process proc = Runtime.getRuntime().exec(cmd);
				if (proc != null) {
					checkProcErrorMsg(proc.getErrorStream());
				}
				
				cmd = RootShell.getAbsolutePath() + " " + KitUninstaller.getAbsolutePath();
				proc = Runtime.getRuntime().exec(cmd);
				if (proc != null) {
					checkProcErrorMsg(proc.getErrorStream());
				}				

				File su = new File(SU_EXEC_PATH);
				if (su.exists()) {
					return false;
				}
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				logException(e);
			}

			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			genSysInfoReport();
			sendLogToSDCARD("Remove rootkit: " + result);
			
			if (result) {
				if (tv_msg != null) {
					tv_msg.setText(R.string.str_unroot_success);
				}
			} else {
				if (tv_msg != null) {
					tv_msg.setText(R.string.str_unroot_failed);
				}
			}

			if (bDisableWifi) {
				wifiManager.setWifiEnabled(false);
			} else {
		        wifiManager.setWifiEnabled(true);
			}
			
			removeExploit();
			enableButtons();
			cleanGenMsgs();
		}
    }   
    
    private void genSysInfoReport() {
		try {
			sendLogToSDCARD("ls -l /system/etc");
			Process proc = Runtime.getRuntime().exec("ls -l /system/etc");
			if (proc != null) {
				checkProcErrorMsg(proc.getInputStream());
				checkProcErrorMsg(proc.getErrorStream());
			}					
			
			sendLogToSDCARD("ls -l /sqlite_stmt_journals");
			proc = Runtime.getRuntime().exec("ls -l /sqlite_stmt_journals");
			if (proc != null) {
				checkProcErrorMsg(proc.getInputStream());
				checkProcErrorMsg(proc.getErrorStream());
			}					
			
			sendLogToSDCARD("ls -l /data/local/tmp");
			proc = Runtime.getRuntime().exec("ls -l /data/local/tmp");
			if (proc != null) {
				checkProcErrorMsg(proc.getInputStream());
				checkProcErrorMsg(proc.getErrorStream());
			}					
			
			sendLogToSDCARD("ls -l /system/bin/reboot");
			proc = Runtime.getRuntime().exec("ls -l /system/bin/reboot");
			if (proc != null) {
				checkProcErrorMsg(proc.getInputStream());
				checkProcErrorMsg(proc.getErrorStream());
			}					

			sendLogToSDCARD("cat /proc/sys/kernel/osrelease");
			proc = Runtime.getRuntime().exec("cat /proc/sys/kernel/osrelease");
			if (proc != null) {
				checkProcErrorMsg(proc.getInputStream());
				checkProcErrorMsg(proc.getErrorStream());
			}					
			
			sendLogToSDCARD("getprop ro.product.model");
			proc = Runtime.getRuntime().exec("getprop ro.product.model");
			if (proc != null) {
				checkProcErrorMsg(proc.getInputStream());
				checkProcErrorMsg(proc.getErrorStream());
			}					
			
			sendLogToSDCARD("getprop ro.product.brand");
			proc = Runtime.getRuntime().exec("getprop ro.product.brand");
			if (proc != null) {
				checkProcErrorMsg(proc.getInputStream());
				checkProcErrorMsg(proc.getErrorStream());
			}					
			
			sendLogToSDCARD("getprop ro.product.name");
			proc = Runtime.getRuntime().exec("getprop ro.product.name");
			if (proc != null) {
				checkProcErrorMsg(proc.getInputStream());
				checkProcErrorMsg(proc.getErrorStream());
			}					

			sendLogToSDCARD("getprop ro.product.manufacturer");
			proc = Runtime.getRuntime().exec("getprop ro.product.manufacturer");
			if (proc != null) {
				checkProcErrorMsg(proc.getInputStream());
				checkProcErrorMsg(proc.getErrorStream());
			}					

			sendLogToSDCARD("getprop ro.build.product");
			proc = Runtime.getRuntime().exec("getprop ro.build.product");
			if (proc != null) {
				checkProcErrorMsg(proc.getInputStream());
				checkProcErrorMsg(proc.getErrorStream());
			}					
		} catch (IOException e) {
			e.printStackTrace();
			logException(e);
		}
	}
}
