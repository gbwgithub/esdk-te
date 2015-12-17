package com.huawei.te.example.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.huawei.esdk.te.TESDK;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.util.DeviceUtil;
import com.huawei.te.example.R;
import com.huawei.te.example.activity.BaseActivity;
import com.huawei.utils.ZipUtil;

/**
 * Function: 提供在TEMobile安装目录下的文件操作
 */
public class FileUtil
{
	private static final String TAG = FileUtil.class.getSimpleName();
	/**
	 * 定义压缩文件开头 TEMobile_
	 */
	public static final String ZIP_TITLE = "TEMobile_";

	/**
	 * 定义压缩文件结尾.zip
	 */
	public static final String ZIP_END = ".zip";

	/**
	 * 日志文件标题格式
	 */
	public static final String LOG_FILE_REG = "TEMobile_+[0-9]+.zip";

	/**
	 * 默认日志为 1
	 */
	public static final int LOG_FILE_TYPE = 1;

	/**
	 * 高级日志提供取数据库文件
	 */
	public static final int DATABASE_FILE_TYPE = 2;

	/**
	 * 高级日志 提取属性文件
	 */
	public static final int CONFIG_FILE_TYPE = 3; // 高级日志提取属性文件

	/**
	 * config 文件的配置相对路径
	 */
	public static final String SIPCONFIG_FILE_DIR = "config";

	/**
	 * 文件路径回退一级
	 */
	public static final String FILE_DIR_BACK = "..";

	/**
	 * ECS 日志文件名
	 */
	public static final String ECS_LOG_FILENAME = "ECS.txt";

	public static final String SDCARD_FILE_ROOT = "/TEMobile";

	/**
	 * file目录下的 日志目录
	 */
	public static final String TEMOBILE_LOG_DIR = "/log/";

	/**
	 * file目录下的 日志目录 - 临时目录
	 */
	public static final String TEMOBILE_TEMP_DIR = "/temp/";

	/**
	 * 数据库文件
	 */
	public static final String DATA_BASE_DIR = "/../databases/";

	/**
	 * 属性文件路径
	 */
	public static final String CONFIG_FILE = "/../shared_prefs/";

	/**
	 * 透传个组件的标注资源目录
	 */
	public static final String ANNO_PATH = "/AnnoRes/";

	/**
	 * 标注资源的zip压缩包
	 */
	public static final String ANNO_ZIP_NAME = "AnnoRes.zip";

	/**
	 * 关于里声明的html文件
	 */
	public static final String ABOUT_HTML_NAME = "en-us_topic_0005507084.html";

	/**
	 * FileUtil对象
	 */
	private static FileUtil ins;

	/**
	 * @return FileUtil单例对象
	 * @since 1.1 2014-2-15 v1.0.0 pWX178217 create
	 */
	public static FileUtil getIns()
	{
		if (null == ins)
		{
			ins = new FileUtil();
		}
		return ins;
	}

	/**
	 * 压缩文件到指定路径
	 * 
	 * @param filepaths
	 *            文件路径
	 * @param zipPathString
	 *            压缩文件
	 * @return boolean true/false
	 */
	public static boolean zipMultiFile(String[] filepaths, String zipPathString)
	{
		ZipOutputStream zos = null;
		BufferedOutputStream out = null;
		FileReader fr = null;
		BufferedReader bin = null;
		try
		{
			FileOutputStream f = new FileOutputStream(zipPathString);
			// 输出校验流,采用Adler32更快
			CheckedOutputStream csum = new CheckedOutputStream(f, new Adler32());
			// 创建压缩输出流
			zos = new ZipOutputStream(csum);
			out = new BufferedOutputStream(zos);
			boolean isfile = false;
			File file = null;
			String s = " ";
			for (int i = 0; i < filepaths.length; i++)
			{
				s = filepaths[i];

				file = new File(s);
				isfile = file.isFile();
				if (isfile)
				{
					fr = new FileReader(s);
					// 针对单个文件建立读取流
					bin = new BufferedReader(fr);
					// ZipEntry ZIP 文件条目
					// putNextEntry 写入新条目，并定位到新条目开始处
					zos.putNextEntry(new ZipEntry(s));
					int c = bin.read();
					while (c != -1)
					{
						out.write(c);
						c = bin.read();
					}
					bin.close();
					bin = null;
					fr.close();
					fr = null;
					out.flush();
				}
			}
			out.close();
			out = null;
			zos.close();
			zos = null;
			f.close();
			return true;
		} catch (FileNotFoundException e)
		{
			Log.e(TAG, "zip error.");
			return false;
		} catch (IOException e)
		{
			Log.e(TAG, "zip error.");
			return false;
		} finally
		{
			closeInputStream(bin);
			closeInputStream(fr);
			closeOutputStream(out);
			closeOutputStream(zos);
		}
	}

	/**
	 * UI压缩并且发送日志，删除旧日志
	 * 
	 * @l00186254
	 * @param context
	 *            上下文环境
	 * @param type
	 *            类型
	 */
	public void sendTEMobileLog(BaseActivity context, int type)
	{
		// 拷贝崩溃文件到指定目录下
		if (null == DeviceUtil.getSdcardPath())
		{
			copyFolder("/data/tombstones", "/data/data/com.huawei.te.example/TESDKLog");
			copyFolder(TESDK.getInstance().getLogPath(), "/data/data/com.huawei.te.example/TESDKLog");
		} else
		{
			copyFolder("/data/tombstones", DeviceUtil.getSdcardPath() + "/TESDKLog");
			copyFolder(TESDK.getInstance().getLogPath(), DeviceUtil.getSdcardPath() + "/TESDKLog");
		}
		// end:拷贝崩溃文件到指定目录下
		String[] paths = findLogFile(context, type);
		if (paths == null)
		{
			Toast.makeText(context, context.getString(R.string.err_report_no_log), Toast.LENGTH_LONG).show();
			return;
		}
		File zipfile = FileUtil.getZIPfile();
		if (zipfile == null)
		{
			// 手机无内存卡，单击“通过电子邮件发送故障报告”按钮无提示信息，用户体验不好
			// 无sdcard情况下提示 日志打包失败,无SDCard
			Toast.makeText(context, context.getString(R.string.err_report_zip_false) + " ," + context.getString(R.string.no_sdcard), Toast.LENGTH_LONG)
					.show();
			return;
		}

		String path = ZipUtil.getCanonicalPath(zipfile);
		if (type == DATABASE_FILE_TYPE)
		{
			path = path.replace(".zip", "/");
		}
		LogPaths logPath = new LogPaths();
		logPath.logpathArray = paths;
		logPath.zippath = path;
		logPath.zipPathFile = zipfile;
		Log.d(TAG, "zippath ->" + path);
		Log.d(TAG, "zipfile ->" + zipfile.getName());
		Log.d(TAG, "logpathArray ->");
		for (int i = 0; i < paths.length; i++)
		{
			Log.d(TAG, paths[i]);
		}
		new MyAsynTask(context).execute(logPath);
	}

	/**
	 * 关闭OutputStream 流
	 * 
	 * @yKF55028
	 * @param os
	 *            OutputStream 流实例
	 */
	public static void closeOutputStream(OutputStream os)
	{
		try
		{
			if (os != null)
			{
				os.close();
				os = null;
			}
		} catch (IOException e)
		{
			Log.i(TAG, "closeOutputString()...Exception->exp");
		}
	}

	/**
	 * 关闭InputStream 流
	 * 
	 * @param is
	 *            InputStream 流实例
	 */
	public static void closeInputStream(InputStream is)
	{
		try
		{
			if (is != null)
			{
				is.close();
				is = null;
			}
		} catch (IOException e)
		{
			Log.i(TAG, "closeInputStream()...Exception->exp");
		}
	}

	/**
	 * 关闭Reader 流
	 * 
	 * @yKF55028
	 * @param is
	 *            Reader 流实例
	 */
	public static void closeInputStream(Reader reader)
	{
		try
		{
			if (reader != null)
			{
				reader.close();
				reader = null;
			}
		} catch (IOException e)
		{
			Log.i(TAG, "closeOutputString()...Exception->exp");
		}
	}

	/**
	 * 获得日志文件路径
	 * 
	 * @param context
	 *            上下文环境
	 * @param type
	 *            类型
	 * @return 字符串数组
	 */
	public static String[] findLogFile(Context context, int type)
	{
		// 获取本账号的文件目录
		String dirpath = ZipUtil.getCanonicalPath(context.getFilesDir());
		String path = "";

		if (type == LOG_FILE_TYPE)
		{
			path = dirpath + TEMOBILE_LOG_DIR;
		} else if (type == CONFIG_FILE_TYPE)
		{
			path = dirpath + CONFIG_FILE;
		} else if (type == DATABASE_FILE_TYPE)
		{
			path = dirpath + DATA_BASE_DIR;
		}

		File teMobileLogFile = new File(path);
		ArrayList<String> list = new ArrayList<String>(Constants.LIST_DEFAULT_CAPABILITY);
		// teMobileLogFile.listFiles()可能为空
		if (teMobileLogFile.exists() && teMobileLogFile.isDirectory())
		{
			File[] listFiles = teMobileLogFile.listFiles();
			if (listFiles != null)
			{
				File file = null;
				for (int i = 0; i < listFiles.length; i++)
				{
					file = listFiles[i];

					list.add(ZipUtil.getCanonicalPath(file));
				}
			}
		}
		if (type == LOG_FILE_TYPE)
		{
			if (new File("/data/anr/traces_com.huawei.eSpaceHD.txt").exists())
			{
				list.add("/data/anr/traces_com.huawei.eSpaceHD.txt");
			}
			if (new File("/data/anr/traces.txt").exists())
			{
				list.add("/data/anr/traces.txt");
			}
			list.add(TESDK.getInstance().getLogPath());
		}
		String[] strs = new String[list.size()];
		return list.toArray(strs);
	}

	// /**
	// * 描述：获取会议组件临时目录
	// */
	// public static String getTempDir()
	// {
	// String dir =
	// ZipUtil.getCanonicalPath(Environment.getExternalStorageDirectory()) +
	// SDCARD_FILE_ROOT + TEMOBILE_TEMP_DIR;
	// File file = new File(dir);
	// // 创建SDcard下的日志文件目录
	// if (!file.exists())
	// {
	// boolean isCreateSuccess = file.mkdirs();
	// // begin add by wx183960 2013/11/25
	// if (!isCreateSuccess)
	// {
	// Log.d(TAG, "directory already exists");
	// }
	// // end add by wx183960 2013/11/25
	// }
	// return dir;
	// }
	//
	// /**
	// * 拷贝关于界面中开放源代码的html
	// *
	// * @param asset
	// * Asset对象
	// * @return 返回拷贝的本地路径
	// */
	// public static String copyAboutHtmlFile(AssetManager asset)
	// {
	// String outFile = "";
	// try
	// {
	// InputStream in = asset.open(ABOUT_HTML_NAME);
	// String outDir =
	// ZipUtil.getCanonicalPath(Environment.getExternalStorageDirectory()) +
	// SDCARD_FILE_ROOT;
	//
	// outFile = outDir + '/' + ABOUT_HTML_NAME;
	//
	// File outHandle = new File(outDir);
	// // 创建SDcard下的日志文件目录
	// if (!outHandle.exists())
	// {
	// boolean isCreateSuccess = outHandle.mkdirs();
	// if (!isCreateSuccess)
	// {
	// Log.d(TAG, "directory already exists");
	// }
	// }
	//
	// FileBrowserActivity.copyAssetsFile(in, outFile);
	//
	// } catch (IOException e)
	// {
	// Log.e(TAG, "Progress get an IOException");
	// outFile = "";
	// }
	//
	// return outFile;
	//
	// }

	/**
	 * 创建压缩文件并删除旧压缩文件
	 */
	public static File getZIPfile()
	{
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			String dir = ZipUtil.getCanonicalPath(Environment.getExternalStorageDirectory()) + SDCARD_FILE_ROOT + TEMOBILE_LOG_DIR;
			File root = new File(dir);
			// 创建SDcard下的日志文件目录
			if (!root.exists())
			{
				boolean isCreateSuccess = root.mkdirs();
				if (!isCreateSuccess)
				{
					Log.d(TAG, "directory already exists");
				}
			}
			File[] fileList = root.listFiles();
			String fileName = null;
			if (fileList != null)
			{
				for (int i = fileList.length - 1; i >= 0; i--)
				{
					fileName = fileList[i].getName();
					// 匹配日志文件，将其删除
					if (fileName.matches(LOG_FILE_REG) && (fileName.length() == 30))
					{
						if (!fileList[i].delete())
						{
							Log.d(TAG, fileName + " delete failed!");
						}
					}
				}
			}
			String date = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(Calendar.getInstance().getTime());
			String zipname = ZIP_TITLE + date + ZIP_END;
			File zipfile = new File(dir, zipname);
			try
			{
				if (zipfile.exists())
				{
					if (zipfile.delete())
					{
						if (!zipfile.createNewFile())
						{
							// add failed
							Log.w(TAG, "getZIPfile() zipfile is exists. zipfile:" + ZipUtil.getCanonicalPath(zipfile) + " create failed");
						}
					} else
					{
						Log.w(TAG, "getZIPfile() oldzipfile:" + ZipUtil.getCanonicalPath(zipfile) + " delete failed");
						return null;
					}

				} else
				{
					if (!zipfile.createNewFile())
					{
						// add failed
						Log.w(TAG, "getZIPfile() zipfile:" + zipfile.getCanonicalPath() + " create failed");
					}
				}
				return zipfile;
			} catch (IOException e)
			{
				Log.e(TAG, "zip error.");
				return null;
			} catch (SecurityException e)
			{
				Log.e(TAG, "zip error.");
				return null;
			}
		}
		return null;
	}

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf/ff
	 */
	private static void copyFolder(String oldPath, String newPath)
	{
		FileInputStream input = null;
		FileOutputStream output = null;
		File newFile = null;
		boolean mkSuccess = true;
		try
		{
			newFile = new File(newPath);
			// 如果文件夹不存在 则建立新文件夹
			if (!newFile.exists())
			{
				mkSuccess = newFile.mkdirs();
			}
			if (!mkSuccess)
			{
				Log.e(TAG, "Make Dirs Failed");
				return;
			}
			File a = new File(oldPath);
			String[] file = a.list();
			if (null == file)
			{
				Log.d(TAG, "file[" + oldPath + "] is null.");
				return;
			}
			File temp = null;
			for (int i = 0; i < file.length; i++)
			{
				if (oldPath.endsWith(File.separator))
				{
					temp = new File(oldPath + file[i]);
				} else
				{
					temp = new File(oldPath + File.separator + file[i]);
				}
				if (null != temp)
				{
					if (temp.isFile())
					{
						input = new FileInputStream(temp);
						output = new FileOutputStream(newPath + '/' + (temp.getName()).toString());
						byte[] b = new byte[1024 * 5];
						int len = input.read(b);
						while (len != -1)
						{
							output.write(b, 0, len);
							len = input.read(b);
						}
						output.flush();
						output.close();
						input.close();
					}
					// 如果是子文件夹
					else if (temp.isDirectory())
					{
						copyFolder(oldPath + '/' + file[i], newPath + '/' + file[i]);
					}
				}
			}
		} catch (FileNotFoundException e)
		{
			Log.e(TAG, "zip error -> FileNotFoundException.");
		} catch (IOException e)
		{
			Log.e(TAG, "zip error -> IOException.");
		} finally
		{
			closeOutputStream(output);
			closeInputStream(input);
		}

	}

	// public static boolean moveFile(String srcPath, String destPath)
	// {
	// File file = new File(srcPath);
	//
	// if (!file.exists())
	// {
	// return false;
	// }
	//
	// byte[] buffer = new byte[2 * 1024 * 1024];
	// FileInputStream in = null;
	// FileOutputStream out = null;
	//
	// boolean result = false;
	//
	// try
	// {
	// in = new FileInputStream(srcPath);
	// out = new FileOutputStream(destPath);
	// int ins = 0;
	// while (true)
	// {
	// ins = in.read(buffer);
	// if (-1 == ins)
	// {
	// out.flush();
	// break;
	// } else
	// {
	// out.write(buffer, 0, ins);
	// }
	// }
	//
	// result = true;
	// } catch (FileNotFoundException e)
	// {
	// Log.e(TAG, "zip error.");
	// } catch (IOException e)
	// {
	// Log.e(TAG, "zip error.");
	// } finally
	// {
	// // begin add by wx183960
	// closeIOStream(in);
	// closeOutputStream(out);
	// // end add by wx183960
	// }
	// return result;
	// }

	/**
	 * Function: 日志压缩的异步任务
	 */
	private static class MyAsynTask extends AsyncTask<LogPaths, String, Boolean>
	{
		private BaseActivity context;
		/**
		 * 压缩文件
		 */
		private File zipfile;

		/**
		 * 构造方法
		 */
		MyAsynTask(BaseActivity activity)
		{
			context = activity;
		}

		/**
		 * 重写doInBackground（）
		 * 
		 * @param params
		 *            日志路径
		 * @return Boolean
		 */
		@Override
		protected Boolean doInBackground(LogPaths... params)
		{
			zipfile = params[0].zipPathFile;
			return zipMultiFile(params[0].logpathArray, params[0].zippath);
		}

		/**
		 * 重写onPostExecute（）
		 * 
		 * @param result
		 *            结果是否
		 * 
		 */
		@Override
		protected void onPostExecute(Boolean result)
		{
			if (result != null && zipfile != null)
			{
				if (result)
				{
					// sendMailByIntent(zipfile, context);
					Toast.makeText(context, "zipfile is ok ->" + zipfile.getAbsolutePath(), Toast.LENGTH_LONG).show();
					Log.d(TAG, "zipfile is ok ->" + zipfile.getAbsolutePath());
//					try
//					{
//						copyFolder("/storage/emulated/0/TEMobile/log", "/mnt");
//					} catch (Exception e)
//					{
//						Log.d(TAG, "copy folder failed.");
//					}
//					Log.d(TAG, "copy folder!");
				} else
				{
					context.showAlertDialog(context.getString(R.string.err_report), context.getString(R.string.err_report_zip_false),
							context.getString(R.string.ok), null, null, null, null);
				}
			}
		}
	}

	private static class LogPaths
	{
		/**
		 * 需要压缩的文件路径数组
		 */
		private String[] logpathArray;
		private String zippath;
		private File zipPathFile;

		@Override
		public String toString()
		{
			return "LogPaths [logpathArray=" + Arrays.toString(logpathArray) + ", zippath=" + zippath + ", zipPathFile=" + zipPathFile + ']';
		}
	}

	// /**
	// * 开启邮箱发送页面
	// *
	// * @param file
	// * 文件
	// * @param context
	// * 上下文环境
	// */
	// private static void sendMailByIntent(File file, Context context)
	// //NO_UCD
	// // (use
	// // private)
	// {
	// String[] subject = new String[] { "TEMobileLog" };
	// Intent intent = new Intent(Intent.ACTION_SEND);
	// intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
	// intent.putExtra(Intent.EXTRA_SUBJECT, subject);
	// intent.setType("message/rfc882");
	// // begin modified by pwx178217 reason:修改故障报告压缩完成弹出标题显示
	// context.startActivity(Intent.createChooser(intent, "TEMobile"));
	// // end modified by pwx178217 reason:修改故障报告压缩完成弹出标题显示
	// EventHandler.getApplicationHandler().doNextEvent();
	// }
	//
	// /**
	// * Function: 根据路径删除文件
	// *
	// * @param path
	// * 输入路径
	// */
	// public static synchronized void deletFileByPath(String path)
	// {
	// File dir = new File(path);
	// if (dir.exists())
	// {
	// deleteFiles(dir);
	// }
	// }
	//
	// /**
	// * 删除文件
	// *
	// * @param path
	// * 文件路径
	// */
	// public static synchronized void deletFile(String path)
	// {
	// if (StringUtil.isNotEmpty(path))
	// {
	// File file = new File(path);
	// if (file == null || !file.exists())
	// {
	// return; // 检查参数
	// }
	// if (!file.delete()) // 删除所有文件
	// {
	// Log.e(TAG, file.getName() + " delete failed!");
	// }
	// }
	// }
	//
	// /**
	// * Function: 删除一个目录下的所有文件
	// *
	// * @param dir
	// * 要删除的目录
	// */
	// private static synchronized void deleteFiles(File dir)
	// {
	// if (dir == null || !dir.exists() || !dir.isDirectory())
	// {
	// return; // 检查参数
	// }
	//
	// File[] files = dir.listFiles();
	// if (files != null)
	// {
	// File file = null;
	// for (int i = 0; i < files.length; i++)
	// {
	// file = files[i];
	//
	// if (file.isFile())
	// {
	// if (!file.delete()) // 删除所有文件
	// {
	// Log.d(TAG, file.getName() + " delete failed!");
	// }
	// } else if (file.isDirectory())
	// {
	// deleteFiles(file); // 递规的方式删除文件夹
	// }
	// }
	// }
	// if (!dir.delete())// 删除目录本身
	// {
	// Log.d(TAG, dir.getName() + " delete failed!");
	// }
	// }
	//
	// /**
	// * Function: 关闭IO流
	// *
	// * @param closeAble
	// * InputStream OutPutStream 的上层接口
	// */
	// public static void closeIOStream(Closeable closeAble)
	// {
	// if (closeAble != null)
	// {
	// try
	// {
	// closeAble.close();
	// } catch (IOException e)
	// {
	// Log.e(TAG, "zip error.");
	// }
	// }
	// }
	//
	// /**
	// * Function: 保存UM图片到系统图片目录
	// *
	// * @param fromPath
	// * 拷贝文件的路径
	// * @return String 保存的文件路径
	// */
	// public static String saveBitMapFile(String fromPath)
	// {
	// if (fromPath == null)
	// {
	// return null;
	// }
	// Bitmap bitmap = BitmapFactory.decodeFile(fromPath, new
	// BitmapFactory.Options());
	// ContentResolver cr = EspaceApp.getIns().getContentResolver();
	// String fileName = Uri.parse(fromPath).getLastPathSegment();
	// String url = MediaStore.Images.Media.insertImage(cr, bitmap, fileName,
	// "");
	// return getRealFilePath(Uri.parse(url));
	// }
	//
	// /**
	// * Function: 解析URL path
	// *
	// * @param uriPath
	// * @return String
	// */
	// public static String getRealFilePath(Uri uri)
	// {
	//
	// if (uri == null)
	// {
	// return null;
	// }
	// String realPath = "";
	// String uriPath = uri.toString();
	// if (uriPath.startsWith("content://"))
	// {
	// realPath = getRealPathFromURI(uri);
	// } else if (uriPath.startsWith("file://"))
	// {
	// realPath = uriPath.replace("file://", "");
	// }
	// return realPath;
	// }
	//
	// public static String getRealPathFromURI(Uri contentUri)
	// {
	// String temp = null;
	// String[] proj = { MediaStore.Images.Media.DATA };
	// BaseActivity ba = ActivityStackManager.INSTANCE.getCurrentActivity();
	// if (ba == null)
	// {
	// return temp;
	// }
	// Cursor cursor = ba.managedQuery(contentUri, proj, // Which columns to
	// // return
	// null, // WHERE clause; which rows to return (all rows)
	// null, // WHERE clause selection arguments (none)
	// null); // Order-by clause (ascending by name)
	// if (null != cursor)
	// {
	// int columnIndex =
	// cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	// cursor.moveToFirst();
	// temp = cursor.getString(columnIndex);
	// }
	// return temp;
	// }
	//
	// /**
	// * Function: 将一个路径下的图片文件，压缩到另外一个目录下
	// *
	// * @param picPath
	// * 原始图片路径
	// * @param newPicPath
	// * 新的路径
	// * @param maxSize
	// * 最大的压缩边距
	// * @param isZipCameraPic
	// * 新图片是否需要压缩
	// * @return String
	// */
	// public static boolean writeFileEx(String picPath, String newPicPath, int
	// maxSize, boolean isZipCameraPic)
	// {
	// OutputStream os = null;
	// try
	// {
	// // //获取源图片的大小
	// BitmapFactory.Options opts = new BitmapFactory.Options();
	// //
	// 当opts不为null时，但decodeFile返回空，不为图片分配内存，只获取图片的大小，并保存在opts的outWidth和outHeight
	// opts.inJustDecodeBounds = true;
	// BitmapFactory.decodeFile(picPath, opts);
	// int srcWidth = opts.outWidth;
	// int srcHeight = opts.outHeight;
	// int destWidth = 0;
	// int destHeight = 0;
	// // 缩放的比例
	// double ratio = 0;
	// // 按比例计算缩放后的图片大小，maxLength是长或宽允许的最大长度
	// if (srcWidth > srcHeight)
	// {
	// ratio = Double.valueOf(srcWidth) / maxSize;
	// destWidth = maxSize;
	// destHeight = parseDoubleToInt(srcHeight / ratio);
	// } else
	// {
	// ratio = Double.valueOf(srcHeight) / maxSize;
	// destHeight = maxSize;
	// destWidth = parseDoubleToInt(srcWidth / ratio);
	// }
	// // 对图片进行压缩，是在读取的过程中进行压缩，而不是把图片读进了内存再进行压缩
	// BitmapFactory.Options newOpts = new BitmapFactory.Options();
	// //
	// 缩放的比例，缩放是很难按准备的比例进行缩放的，目前我只发现只能通过inSampleSize来进行缩放，其值表明缩放的倍数，SDK中建议其值是2的指数值
	// newOpts.inSampleSize = parseDoubleToInt(ratio) + 1;
	// // inJustDecodeBounds设为false表示把图片读进内存中
	// newOpts.inJustDecodeBounds = false;
	// // 设置大小，这个一般是不准确的，是以inSampleSize的为准，但是如果不设置却不能缩放
	// newOpts.outHeight = destHeight;
	// newOpts.outWidth = destWidth;
	// // 添加尺寸信息，
	// // 获取缩放后图片
	// Bitmap destBm = BitmapFactory.decodeFile(picPath, newOpts);
	//
	// if (destBm != null)
	// {
	// if (isZipCameraPic)
	// {
	// android.graphics.Matrix matrix = new android.graphics.Matrix();
	// if (destBm.getWidth() > destBm.getHeight())
	// {
	// matrix.setRotate(90);
	// }
	// destBm = Bitmap.createBitmap(destBm, 0, 0, destBm.getWidth(),
	// destBm.getHeight(), matrix, true);
	// }
	// // 文件命名，通过GUID可避免命名的重复
	// File destFile = new File(newPicPath);
	// // 创建文件输出流
	// os = new FileOutputStream(destFile);
	// // 存储
	// destBm.compress(CompressFormat.JPEG, 50, os);
	// // 关闭流
	// os.close();
	//
	// }
	// } catch (FileNotFoundException e)
	// {
	// return false;
	// } catch (IOException e)
	// {
	// return false;
	// }
	// // begin add by wx183960 2013/11/25
	// finally
	// {
	// closeOutputStream(os);
	// }
	// // end add by wx183960 2013/11/25
	// return true;
	// }
	//
	// /**
	// * @param values
	// * @return
	// */
	// private static int parseDoubleToInt(double values)
	// {
	// return Double.valueOf(values).intValue();
	// }
	//
	// /**
	// * Function: 判断当前路径文件是否存在
	// *
	// * @param filePath
	// * 文件路径
	// * @return boolean
	// */
	// public static boolean isFileExit(String filePath)
	// {
	// File file = new File(filePath);
	// if (file.exists())
	// {
	// file = null;
	// return true;
	// }
	// return false;
	// }
	//
	// public static String formatTime(int millis)
	// {
	// Date date = new Date(millis);
	// SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
	// return sdf.format(date);
	// }
}
