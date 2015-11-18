package com.huawei.te.example;

import android.app.Activity;
import android.util.Log;

import com.huawei.common.Resource;
import com.huawei.common.ResponseCodeHandler.ResponseCode;
import com.huawei.te.example.activity.BaseActivity;
import com.huawei.te.example.activity.LoginActivity;
import com.huawei.utils.StringUtil;

public final class ResponseErrorCodeHandler
{
	private static final String TAG = ResponseErrorCodeHandler.class.getSimpleName();

	public enum UIerrorCode
	{
		/**
		 * 网络连接失败
		 */
		CONNECT_SERVER_ERROR,

	}

	/**
	 * 请求失败
	 */
	public static final int NATIVE_REQUEST_FAIL = 101;
	/**
	 * 请求超时
	 */
	public static final int NATIVE_REQUEST_TIMEOUT = 102;

	/**
	 * 请求错误 请求发送过程中的错误
	 */
	public static final int NATIVE_REQUEST_ERROR = 103;

	/**
	 * Socket连接错误
	 */
	public static final int CONNECT_ERROR = 104;

	/**
	 * UC 2.0 wifi only 禁止 3G 登录
	 */
	public static final int WIFI_ONLY_ERROR = 105;

	/**
	 * 限制登录的服务器
	 */
	public static final int LIMIT_SERVER = 106;

	/**
	 * 鉴权失败
	 */
	public static final int LOGIN_ACCOUNT_ERROR = 107;

	/**
	 * 网络不支持
	 */
	public static final int NETWORK_INVALID = 108;

	/**
	 * 登录用户已达上限
	 */
	public static final int LOGIN_ACCOUNTNUM_OVERLIMIT = 109;

	/**
	 * 证书错误
	 */
	public static final int CERTIFICATE_ERROR = 110;

	/**
	 * 处理请求异常。只是终止请求，给出提示。不做任何数据处理
	 * 
	 * @param errorCode
	 *            错误码
	 * @param Activity
	 *            activity引用
	 * @param laveCount
	 *            只用于登录失败提示
	 */
	public static synchronized void handleRequestError(int errorCode, BaseActivity activity)
	{
		if (activity == null)
		{
			return;
		}
		String msg = null;
		switch (errorCode) {
		// 密码账号鉴权错误
		case LOGIN_ACCOUNT_ERROR:
			msg = activity.getString(R.string.account_error);
			break;
		case Resource.REQUEST_FAIL: // 请求失败0
			msg = activity.getString(R.string.module_error_4);
			break;
		case Resource.REQUEST_TIMEOUT: // 请求超时-1
			msg = activity.getString(R.string.etimeout);
			break;
		case Resource.REQUEST_ERROR: // 请求错误-2
			msg = activity.getString(R.string.module_error_1);
			break;
		case CONNECT_ERROR:// 104
			msg = activity.getString(R.string.connect_error);
			break;
		case WIFI_ONLY_ERROR:// 105
			msg = activity.getString(R.string.viawifi);
			break;
		case LIMIT_SERVER: // 106
			msg = activity.getString(R.string.limit_login_prompt);
			break;
		case NETWORK_INVALID: // 108
			msg = activity.getString(R.string.network_off);
			break;
		case LOGIN_ACCOUNTNUM_OVERLIMIT: // 109
			msg = activity.getString(R.string.accountnum_overlimit);
			break;
		case CERTIFICATE_ERROR: // 110
			msg = activity.getString(R.string.certificate_error);
			break;
		case Resource.REG_ERR_CODE.ERR_LICENSE_APPLY_FAILED:
			msg = "license申请失败";
		default:
			break;
		}
		if (msg != null)
		{
			activity.showAlertDialog(activity.getString(R.string.msg_tip), msg, activity.getString(R.string.ok), null, null, null, null);
		}
	}

	/**
	 * 处理请求错误的统一入口
	 * 
	 * @param errorCode
	 *            返回的错误码
	 * @param desc
	 *            描述
	 * @param baseActivity
	 *            BaseActivity对象
	 * @param needESpaceApp
	 *            true -6 -9情况不需要弹对话框，发送上层广播 到ESpaceApp处理， false 直接弹出提示对话框。
	 *            不做其他操作
	 */
	public static void handleError(ResponseCode errorCode, String desc, Activity baseActivity, boolean needESpaceApp)
	{
		handleError(true, errorCode, desc, baseActivity, needESpaceApp);
	}

	/**
	 * 头像请求的 错误不用提示
	 * 
	 * @param showError
	 *            是否处理错误
	 * @param errorCode
	 *            错误码
	 * @param desc
	 *            错误描述
	 * @param baseActivity
	 *            BaseActivity对象
	 * @param needESpaceApp
	 *            是否需要espaceApp
	 */
	public static synchronized void handleError(boolean showError, ResponseCode errorCode, String desc, Activity baseActivity, boolean needESpaceApp)
	{
		if (baseActivity == null || errorCode == null)
		{
			Log.e(TAG, "Activity is null or errorCode is null");
			return;
		}
		Log.d(TAG, "errorCode:" + errorCode + ",desc:" + desc);
		handleErrorCaseOnepart(showError, errorCode, desc, baseActivity, needESpaceApp);
		// handleErrorCaseTwopart(showError, errorCode, desc, baseActivity,
		// needESpaceApp);
		// handleErrorCaseThreepart(showError, errorCode, desc, baseActivity,
		// needESpaceApp);
	}

	private static synchronized void handleErrorCaseOnepart(boolean showError, ResponseCode errorCode, String desc, Activity baseActivity,
			boolean needESpaceApp)
	{
		String msgTitle = baseActivity.getString(R.string.ok);
		String msg = desc;
		boolean needShow = false;
		// 对话框延时显示，避免对话框显示与Activity finish之间的冲突
		boolean delay = false;
		switch (errorCode) {
		case COMMON_ERROR:
			needShow = true;
			if (!StringUtil.isStringEmpty(desc))
			{
				break;
			}
			if (baseActivity instanceof LoginActivity)
			{
				msg = baseActivity.getString(R.string.module_error_1login);
			} else
			{
				msg = baseActivity.getString(R.string.module_error_1);
			}
			break;
		// case SESSION_OVERDUE:
		// if (needESpaceApp)
		// {
		// Intent intent = new Intent(BROADCAST_PATH.ACTION_LOGOUT);
		// intent.putExtra(Resource.SERVICE_ERROR_DATA,
		// ResponseCode.SESSION_OVERDUE);
		// EspaceApp.getIns().sendBroadcast(intent);
		// break;
		// }
		// needShow = true;
		// delay = true;
		// msg = baseActivity.getString(R.string.module_error_2);
		// break;
		// case DISABLED_ACCOUNT: // 登录 用户名账号 密码错误，或者是账号不可用
		// needShow = true;
		// msg = StringUtil.isStringEmpty(desc) ?
		// baseActivity.getString(R.string.module_error_1) : desc;
		// TESDK.getInstance().stopImServiceIfInactive();
		// break;
		default:
			break;
		}

		doEndShowDialog(needShow, showError, delay, baseActivity, msgTitle, msg);
	}

	private static void doEndShowDialog(boolean needShow, boolean showError, boolean delay, Activity baseActivity, String msgTitle, String msg)
	{
		// if (needShow && showError)
		// {
		// if (delay)
		// {
		// showAlertDialogDelay(baseActivity, msgTitle, msg);
		// return;
		// }
		// String ok = baseActivity.getString(R.string.ok);
		// baseActivity.showAlertDialog(msgTitle, msg, ok, null, null, null,
		// null);
		// }
		// To invoke
		if (baseActivity instanceof LoginActivity)
		{
			((LoginActivity) baseActivity).showAlertDialog(null, msg, null, null, null, null, null);
		}
	}
}
